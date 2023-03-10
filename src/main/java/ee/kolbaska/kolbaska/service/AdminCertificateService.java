package ee.kolbaska.kolbaska.service;

import com.google.zxing.WriterException;
import ee.kolbaska.kolbaska.config.UserConfiguration;
import ee.kolbaska.kolbaska.exception.CertificateNotFoundException;
import ee.kolbaska.kolbaska.mapper.TransactionMapper;
import ee.kolbaska.kolbaska.model.certificate.Certificate;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.CertificateRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.AdminCertificateCreationRequest;
import ee.kolbaska.kolbaska.request.AdminUpdateCertificateInformationRequest;
import ee.kolbaska.kolbaska.response.AdminCertificateCreationResponse;
import ee.kolbaska.kolbaska.response.AdminCertificateInformationResponse;
import ee.kolbaska.kolbaska.response.AdminCertificateResponse;
import ee.kolbaska.kolbaska.response.AdminUpdateCertificateInformationResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.QrCodeService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminCertificateService {

    @Value("${api.baseurl}")
    private String API_BASEURL = "http://localhost:8080";

    private final UserConfiguration userConfiguration;

    private final QrCodeService qrCodeService;

    private final EmailService emailService;

    private final UserRepository userRepository;

    private final CertificateRepository certificateRepository;

    @Transactional
    public AdminCertificateCreationResponse createCertificate(AdminCertificateCreationRequest request) throws IOException, WriterException, MessagingException, TemplateException {
        User admin = userConfiguration.getRequestUser();

        User holder = userRepository.findById(request.getHolderUserId()).orElseThrow(
                () -> new UsernameNotFoundException("Holder not found!")
        );

        String certificateId = UUID.randomUUID().toString();

        Certificate newCertificate = Certificate.builder()
                .value(request.getValue())
                .validUntil(request.getValidUntil())
                .active(true)
                .sender(admin)
                .holder(holder)
                .description(request.getDescription())
                .id(certificateId)
                .build();

        certificateRepository.save(newCertificate);

        String qrCodeUrl = "%s/api/v1/certificate/%s".formatted(API_BASEURL, certificateId);

        byte[] qrCodeImage = qrCodeService.createQrCode(qrCodeUrl);

        Map<String, Object> content = new HashMap<>();

        SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");

        content.put("qrCode", qrCodeImage);
        content.put("value", "%d€".formatted(request.getValue()));
        content.put("valid_until", sf.format(request.getValidUntil()));
        content.put("from", "Support Team");
        content.put("to", holder.getFullName());
        content.put("description", request.getDescription());

        emailService.sendHTMLEmail(
                holder.getEmail(),
                "Congratulations you received restaurant certificate",
                "successfulCertificatePayment",
                content
        );

        return AdminCertificateCreationResponse.builder().message("Certificate was successfully created").build();
    }

    public List<AdminCertificateResponse> getCertificates() {
        List<Certificate> certificateList = certificateRepository.findAll();

        if (certificateList.isEmpty()) return List.of();

        List<AdminCertificateResponse> response = new ArrayList<>();

        SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");

        for (Certificate certificate : certificateList) {
            AdminCertificateResponse tempCertificate = AdminCertificateResponse.builder()
                    .id(certificate.getId())
                    .remainingValue(certificate.getRemainingValue())
                    .holder(certificate.getHolder().getFullName())
                    .sender(certificate.getSender().getFullName())
                    .value(certificate.getValue())
                    .validUntil(sf.format(certificate.getValidUntil()))
                    .build();

            response.add(tempCertificate);
        }

        return response;
    }

    public AdminCertificateInformationResponse getCertificate(String id) throws CertificateNotFoundException {
        Optional<Certificate> ifCertificate = certificateRepository.findById(id);

        if (ifCertificate.isEmpty()) throw new CertificateNotFoundException("Certificate do not found");

        Certificate certificate = ifCertificate.get();

        SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");

        AdminCertificateInformationResponse response = AdminCertificateInformationResponse.builder()
                .toFullName(certificate.getHolder().getFullName())
                .toEmail(certificate.getHolder().getEmail())
                .toPhone(certificate.getHolder().getPhone())
                .remainingValue(certificate.getRemainingValue())
                .description(certificate.getDescription())
                .createdAt(certificate.getCreatedAt())
                .value(certificate.getValue())
                .validUntil(sf.format(certificate.getValidUntil()))
                .transactions(TransactionMapper.INSTANCE.toTransactionResponseList(certificate.getTransactions()))
                .build();

        if (certificate.getCreatedByAdmin()) {}
        else {
            response.setFromFullName(certificate.getSender().getFullName());
            response.setFromEmail(certificate.getSender().getEmail());
            response.setFromPhone(certificate.getSender().getPhone());
        }
        return response;
    }

    public AdminUpdateCertificateInformationResponse updateCertificate(AdminUpdateCertificateInformationRequest request) throws CertificateNotFoundException {
        Optional<Certificate> ifCertificate = certificateRepository.findById(request.getId());

        if (ifCertificate.isEmpty()) throw new CertificateNotFoundException("Certificate with given id not found!");

        Certificate certificate = ifCertificate.get();

        certificate.setValidUntil(request.getValidUntil());
        certificate.setValue(request.getValue());
        certificate.setDescription(request.getDescription());
        certificate.setRemainingValue(request.getRemainingValue());

        User sender = userRepository.findById(request.getSenderUserId()).orElseThrow(
                () -> new UsernameNotFoundException("Sender not found!")
        );

        certificate.setSender(sender);

        User holder = userRepository.findById(request.getHolderUserId()).orElseThrow(
                () -> new UsernameNotFoundException("Holder not found!")
        );

        certificate.setHolder(holder);

        certificateRepository.save(certificate);

        return AdminUpdateCertificateInformationResponse.builder()
                .message("Certificate was successfully updated")
                .build();
    }

    public AdminUpdateCertificateInformationResponse disableCertificate(String id) throws CertificateNotFoundException {
        Certificate certificate = certificateRepository.findById(id).orElseThrow(
                () -> new CertificateNotFoundException("Certificate with given id wasn't found")
        );

        certificate.setActive(false);
        certificate.setDeletedAt(new Date());

        certificateRepository.save(certificate);

        return AdminUpdateCertificateInformationResponse.builder()
                .message("Certificate was successfully disabled")
                .build();
    }
}
