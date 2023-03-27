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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminCertificateService.class);

    @Transactional
    public AdminCertificateCreationResponse createCertificate(AdminCertificateCreationRequest request) throws IOException, WriterException, MessagingException, TemplateException {
        LOGGER.info("Creating a new certificate for the holder with ID {}", request.getHolderUserId());

        User admin = userConfiguration.getRequestUser();

        User holder = userRepository.findById(request.getHolderUserId()).orElseThrow(
                () -> new UsernameNotFoundException("Holder not found!")
        );

        Certificate newCertificate = Certificate.builder()
                .value(request.getValue())
                .validUntil(request.getValidUntil())
                .active(true)
                .sender(admin)
                .holder(holder)
                .description(request.getDescription())
                .createdByAdmin(true)
                .build();

        newCertificate = certificateRepository.save(newCertificate);

        String qrCodeUrl = "%s/api/v1/certificate/%s".formatted(API_BASEURL, newCertificate.getId());

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

        LOGGER.info("Successfully created a new certificate with ID {}", newCertificate.getId());

        return AdminCertificateCreationResponse.builder().message("Certificate was successfully created").build();
    }

    public List<AdminCertificateResponse> getCertificates() {
        LOGGER.info("Retrieving all certificates");

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

        LOGGER.info("Successfully retrieved {} certificates", response.size());

        return response;
    }

    public AdminCertificateInformationResponse getCertificate(String id) throws CertificateNotFoundException {
        LOGGER.info("Retrieving certificate with ID {}", id);

        Optional<Certificate> ifCertificate = certificateRepository.findById(id);

        if (ifCertificate.isEmpty()) throw new CertificateNotFoundException("Certificate can not be found");

        Certificate certificate = ifCertificate.get();

        SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");

        AdminCertificateInformationResponse response = AdminCertificateInformationResponse.builder()
                .toId(certificate.getHolder().getId())
                .remainingValue(certificate.getRemainingValue())
                .description(certificate.getDescription())
                .createdAt(certificate.getCreatedAt())
                .value(certificate.getValue())
                .validUntil(sf.format(certificate.getValidUntil()))
                .transactions(TransactionMapper.INSTANCE.toTransactionResponseList(certificate.getTransactions()))
                .build();

        if (!certificate.getCreatedByAdmin()) {
            response.setFromId(certificate.getSender().getId());
        }

        LOGGER.info("Successfully retrieved certificate with ID {}", id);

        return response;
    }

    public AdminUpdateCertificateInformationResponse updateCertificate(AdminUpdateCertificateInformationRequest request) throws CertificateNotFoundException {
        LOGGER.info("Updating certificate with ID {}", request.getId());

        Optional<Certificate> ifCertificate = certificateRepository.findById(request.getId());

        if (ifCertificate.isEmpty()) throw new CertificateNotFoundException("Certificate with given id not found!");

        Certificate certificate = ifCertificate.get();

        certificate.setValidUntil(request.getValidUntil());
        certificate.setValue(request.getValue());
        certificate.setDescription(request.getDescription());
        certificate.setRemainingValue(request.getRemainingValue());

        if (!certificate.getCreatedByAdmin()) {
            User sender = userRepository.findById(request.getSenderUserId()).orElseThrow(
                    () -> new UsernameNotFoundException("Sender not found!")
            );

            certificate.setSender(sender);
        }

        User holder = userRepository.findById(request.getHolderUserId()).orElseThrow(
                () -> new UsernameNotFoundException("Holder not found!")
        );

        certificate.setHolder(holder);

        certificateRepository.save(certificate);

        LOGGER.info("Successfully updated certificate with ID {}", request.getId());

        return AdminUpdateCertificateInformationResponse.builder()
                .message("Certificate was successfully updated")
                .build();
    }

    public AdminUpdateCertificateInformationResponse disableCertificate(String id) throws CertificateNotFoundException {
        LOGGER.info("Disabling certificate with ID {}", id);

        Certificate certificate = certificateRepository.findById(id).orElseThrow(
                () -> new CertificateNotFoundException("Certificate with given id wasn't found")
        );

        certificate.setActive(false);
        certificate.setDeletedAt(new Date());

        certificateRepository.save(certificate);

        LOGGER.info("Successfully disabled certificate with ID {}", id);

        return AdminUpdateCertificateInformationResponse.builder()
                .message("Certificate was successfully disabled")
                .build();
    }
}
