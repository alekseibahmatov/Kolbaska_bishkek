package ee.maitsetuur.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.WriterException;
import ee.maitsetuur.config.UserConfiguration;
import ee.maitsetuur.exception.CertificateNotFoundException;
import ee.maitsetuur.mapper.TransactionMapper;
import ee.maitsetuur.model.certificate.Certificate;
import ee.maitsetuur.model.user.User;
import ee.maitsetuur.repository.CertificateRepository;
import ee.maitsetuur.repository.UserRepository;
import ee.maitsetuur.request.AdminCertificateCreationRequest;
import ee.maitsetuur.request.AdminUpdateCertificateInformationRequest;
import ee.maitsetuur.response.AdminCertificateCreationResponse;
import ee.maitsetuur.response.AdminCertificateInformationResponse;
import ee.maitsetuur.response.AdminCertificateResponse;
import ee.maitsetuur.response.AdminUpdateCertificateInformationResponse;
import ee.maitsetuur.service.miscellaneous.EmailService;
import ee.maitsetuur.service.miscellaneous.QrCodeService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
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
    public AdminCertificateCreationResponse createCertificate(AdminCertificateCreationRequest request) throws MessagingException {
        LOGGER.info("Creating a new certificate for the holder with ID {}", request.getHolderUserId());

        User admin = userConfiguration.getRequestUser();

        User holder = userRepository.findById(request.getHolderUserId()).orElseThrow(
                () -> new UsernameNotFoundException("Holder not found!")
        );

        Certificate newCertificate = Certificate.builder()
                .value(request.getValue())
                .remainingValue(request.getValue())
                .validUntil(request.getValidUntil())
                .active(true)
                .sender(admin)
                .holder(holder)
                .greeting(holder.getFullName())
                .greetingText(request.getDescription())
                .createdByAdmin(true)
                .build();

        newCertificate = certificateRepository.save(newCertificate);

        Map<String, String> payload = new HashMap<>();

        DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter();

        payload.put("certificate_id", newCertificate.getId().toString());
        payload.put("name", newCertificate.getGreeting());
        payload.put("remainingValue", "%.2f".formatted(newCertificate.getValue()));
        payload.put("value", "%.2f€".formatted(newCertificate.getValue()));
        payload.put("valid_until", newCertificate.getValidUntil().format(dtf));
        payload.put("from", "Support Team");
        payload.put("to", newCertificate.getGreeting());
        payload.put("description", newCertificate.getGreetingText());

        OkHttpClient client = new OkHttpClient();

        Gson gson = new Gson();
        String jsonData = gson.toJson(payload);

        RequestBody body = RequestBody.create(
                jsonData,
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        Request certificateRequest = new Request.Builder()
                .url("http://node-app:3030/certificate")
                .post(body)
                .build();

        byte[] pdfBytes = null;

        try (Response response = client.newCall(certificateRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            assert response.body() != null;

            JsonObject json = new Gson().fromJson(response.body().string(), JsonObject.class);

            String base64Pdf = json.get("pdf").getAsString();

            pdfBytes = Base64.getDecoder().decode(base64Pdf);
        } catch (IOException e) {
            e.printStackTrace();
        }

        emailService.sendHTMLEmail(
                holder.getEmail(),
                "Congratulations you received restaurant certificate",
                "email/successfulCertificatePayment",
                new HashMap<>(),
                pdfBytes
        );

        LOGGER.info("Successfully created a new certificate with ID {}", newCertificate.getId());

        return AdminCertificateCreationResponse.builder().message("Certificate was successfully created").build();
    }

    public List<AdminCertificateResponse> getCertificates() {
        LOGGER.info("Retrieving all certificates");

        List<Certificate> certificateList = certificateRepository.findAll();

        if (certificateList.isEmpty()) return List.of();

        List<AdminCertificateResponse> response = new ArrayList<>();

        DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter();

        for (Certificate certificate : certificateList) {
            AdminCertificateResponse tempCertificate = AdminCertificateResponse.builder()
                    .id(certificate.getId().toString())
                    .remainingValue(certificate.getRemainingValue())
                    .holder(certificate.getHolder().getFullName())
                    .sender(certificate.getSender().getFullName())
                    .value(certificate.getValue())
                    .validUntil(certificate.getValidUntil().format(dtf))
                    .build();

            response.add(tempCertificate);
        }

        LOGGER.info("Successfully retrieved {} certificates", response.size());

        return response;
    }

    public AdminCertificateInformationResponse getCertificate(UUID id) throws CertificateNotFoundException {
        LOGGER.info("Retrieving certificate with ID {}", id);

        Optional<Certificate> ifCertificate = certificateRepository.findById(id);

        if (ifCertificate.isEmpty()) throw new CertificateNotFoundException("Certificate can not be found");

        Certificate certificate = ifCertificate.get();

        AdminCertificateInformationResponse response = AdminCertificateInformationResponse.builder()
                .toId(certificate.getHolder().getId())
                .remainingValue(certificate.getRemainingValue())
                .description(certificate.getGreetingText())
                .createdAt(certificate.getCreatedAt())
                .value(certificate.getValue())
                .validUntil(certificate.getValidUntil())
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
        certificate.setGreetingText(request.getDescription());
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

    public AdminUpdateCertificateInformationResponse disableCertificate(UUID id) throws CertificateNotFoundException {
        LOGGER.info("Disabling certificate with ID {}", id);

        Certificate certificate = certificateRepository.findById(id).orElseThrow(
                () -> new CertificateNotFoundException("Certificate with given id wasn't found")
        );

        certificate.setActive(false);
        certificate.setDeleted(true);

        certificateRepository.save(certificate);

        LOGGER.info("Successfully disabled certificate with ID {}", id);

        return AdminUpdateCertificateInformationResponse.builder()
                .message("Certificate was successfully disabled")
                .build();
    }
}
