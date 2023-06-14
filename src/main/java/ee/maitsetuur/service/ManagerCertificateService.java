package ee.maitsetuur.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.WriterException;
import ee.maitsetuur.config.UserConfiguration;
import ee.maitsetuur.exception.CertificateInsufficientFundsException;
import ee.maitsetuur.exception.CertificateIsDisabledException;
import ee.maitsetuur.exception.CertificateIsOutDatedException;
import ee.maitsetuur.exception.CertificateNotFoundException;
import ee.maitsetuur.model.certificate.Certificate;
import ee.maitsetuur.model.transaction.Transaction;
import ee.maitsetuur.model.user.User;
import ee.maitsetuur.repository.CertificateRepository;
import ee.maitsetuur.repository.TransactionRepository;
import ee.maitsetuur.request.CertificateActivationRequest;
import ee.maitsetuur.response.CertificateActivationResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ManagerCertificateService {

    private final QrCodeService qrCodeService;

    private final EmailService emailService;

    private final UserConfiguration userConfiguration;

    private final CertificateRepository certificateRepository;

    private final TransactionRepository transactionRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagerRestaurantService.class);

    @Transactional
    public CertificateActivationResponse activateCertificate(CertificateActivationRequest request) throws IOException, CertificateNotFoundException, CertificateIsDisabledException, CertificateIsOutDatedException, WriterException, MessagingException, CertificateInsufficientFundsException {
        User worker = userConfiguration.getRequestUser();
        LOGGER.info("Attempting to activate certificate with unique code: {} for restaurant: {}", request.getUniqueCode(), worker.getRestaurant() == null ? worker.getManagedRestaurant().getName() : worker.getRestaurant().getName());

        if (worker.getRestaurant() == null && worker.getManagedRestaurant() == null) throw new AccessDeniedException("You are not allowed to make this action");

        Optional<Certificate> isCertificate = certificateRepository.findById(request.getUniqueCode());

        if (isCertificate.isEmpty()) throw new CertificateNotFoundException("Certificate with unique code you provided is not registered in the database");

        Certificate certificate = isCertificate.get();

        if (certificate.getValidUntil().isBefore(LocalDate.now())) throw new CertificateIsOutDatedException("This certificate is no longer active");
        if (!certificate.getActive()) throw new CertificateIsDisabledException("This certificate is disabled");
        if (certificate.getRemainingValue() == 0.0) throw new CertificateInsufficientFundsException("Certificate is already empty");

        Double transactionValue;

        if (certificate.getRemainingValue() < request.getAmount()) transactionValue = certificate.getRemainingValue();
        else transactionValue = request.getAmount();

        Transaction newTransaction = Transaction.builder()
                .certificate(certificate)
                .value(transactionValue)
                .restaurant(worker.getRestaurant() == null ? worker.getManagedRestaurant(): worker.getRestaurant())
                .waiter(worker)
                .certificate(certificate)
                .build();

        certificate.setRemainingValue(certificate.getRemainingValue() - request.getAmount());

        transactionRepository.save(newTransaction);
        certificateRepository.save(certificate);

        Map<String, String> payload = new HashMap<>();

        DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter();

        payload.put("certificate_id", certificate.getId().toString());
        payload.put("name", certificate.getSender().getFullName());
        payload.put("remainingValue", "%.2f".formatted(certificate.getRemainingValue()));
        payload.put("value", "%.2f€".formatted(certificate.getValue()));
        payload.put("valid_until", certificate.getValidUntil().format(dtf));
        payload.put("from", certificate.getSender().getFullName());
        payload.put("to", certificate.getHolder().getFullName());
        payload.put("description", certificate.getGreetingText());

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

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

        HashMap<String, Object> emailContent = new HashMap<>();

        emailContent.put("bill_total", "%.2f€".formatted(transactionValue));

        Locale estonianLocale = new Locale("et", "ee");

        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, d. MMM yyyy", estonianLocale);

        emailContent.put("payment_date", formatter.format(new Date()));
        emailContent.put("full_name", certificate.getSender().getFullName());
        emailContent.put("remaining_total", "%.2f€".formatted(certificate.getRemainingValue()));
        emailContent.put("transaction_total", "%.2f€".formatted(transactionValue));
        emailContent.put("certificate_id", certificate.getId());

        emailService.sendHTMLEmail(
                certificate.getHolder().getEmail(),
                "Congratulations you received restaurant certificate",
                "email/activationBill",
                emailContent,
                pdfBytes
        );

        LOGGER.info("Certificate with unique code: {} has been successfully activated for restaurant: {}", request.getUniqueCode(), worker.getRestaurant() == null ? worker.getManagedRestaurant().getName() : worker.getRestaurant().getName());

        return CertificateActivationResponse.builder()
                .message("QR Code was successfully activated!")
                .build();
    }
}
