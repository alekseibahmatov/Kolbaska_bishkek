package ee.maitsetuur.service;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

        LOGGER.info("Certificate with unique code: {} has been successfully activated for restaurant: {}", request.getUniqueCode(), worker.getRestaurant() == null ? worker.getManagedRestaurant().getName() : worker.getRestaurant().getName());

        Map<String, String> payload = new HashMap<>();

        payload.put("certificate_id", certificate.getId().toString());
        payload.put("name", certificate.getHolder().getFullName());
        payload.put("remainingValue", certificate.getRemainingValue().toString());

        byte[] qrCodeImage = qrCodeService.createQrCode(payload.toString());

        Map<String, Object> content = new HashMap<>();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        content.put("qrCode", qrCodeImage);
        content.put("value", "%.2f€".formatted(certificate.getValue()));
        content.put("valid_until", certificate.getValidUntil().format(dateTimeFormatter));
        content.put("from", certificate.getSender().getFullName());
        content.put("to", certificate.getHolder().getFullName());
        content.put("description", certificate.getGreetingText());

        emailService.sendHTMLEmail(
                certificate.getHolder().getEmail(),
                "Congratulations you received restaurant certificate",
                "email/successfulCertificatePayment",
                content
        );

        return CertificateActivationResponse.builder()
                .message("QR Code was successfully activated!")
                .build();
    }
}
