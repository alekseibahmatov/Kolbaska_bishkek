package ee.kolbaska.kolbaska.service;

import com.google.zxing.WriterException;
import ee.kolbaska.kolbaska.config.UserConfiguration;
import ee.kolbaska.kolbaska.exception.CertificateIsDisabledException;
import ee.kolbaska.kolbaska.exception.CertificateIsOutDatedException;
import ee.kolbaska.kolbaska.exception.CertificateNotFoundException;
import ee.kolbaska.kolbaska.model.certificate.Certificate;
import ee.kolbaska.kolbaska.model.transaction.Transaction;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.CertificateRepository;
import ee.kolbaska.kolbaska.repository.TransactionRepository;
import ee.kolbaska.kolbaska.request.CertificateActivationRequest;
import ee.kolbaska.kolbaska.response.CertificateActivationResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.QrCodeService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    public CertificateActivationResponse activateCertificate(CertificateActivationRequest request) throws IOException, CertificateNotFoundException, CertificateIsDisabledException, CertificateIsOutDatedException, WriterException, MessagingException, TemplateException {
        User worker = userConfiguration.getRequestUser();
        LOGGER.info("Attempting to activate certificate with unique code: {} for restaurant: {}", request.getUniqueCode(), worker.getRestaurant().getName());

        if (worker.getRestaurant() == null) throw new AccessDeniedException("You are not allowed to make this action");

        Optional<Certificate> isCertificate = certificateRepository.findById(request.getUniqueCode());

        if (isCertificate.isEmpty()) throw new CertificateNotFoundException("Certificate with unique code you provided is not registered in the database");

        Certificate certificate = isCertificate.get();

        if (certificate.getValidUntil().before(new Date())) throw new CertificateIsOutDatedException("This certificate is no longer active");
        if (!certificate.getActive()) throw new CertificateIsDisabledException("This certificate is disabled");

        Double transactionValue;

        if (certificate.getRemainingValue() < request.getAmount()) transactionValue = certificate.getRemainingValue();
        else transactionValue = request.getAmount();

        Transaction newTransaction = Transaction.builder()
                .certificate(certificate)
                .value(transactionValue)
                .restaurant(worker.getRestaurant())
                .waiter(worker)
                .certificate(certificate)
                .build();

        certificate.setRemainingValue(certificate.getRemainingValue() - request.getAmount());
        certificate.setActivatedAt(new Date());

        newTransaction = transactionRepository.save(newTransaction);
        if (certificate.getTransactions() == null) certificate.setTransactions(List.of(newTransaction));
        else {
            List<Transaction> transactions = new ArrayList<>(certificate.getTransactions());
            transactions.add(newTransaction);
            certificate.setTransactions(transactions);
        }

        certificateRepository.save(certificate);

        LOGGER.info("Certificate with unique code: {} has been successfully activated for restaurant: {}", request.getUniqueCode(), worker.getRestaurant().getName());

        Map<String, String> payload = new HashMap<>();

        payload.put("certificate_id", certificate.getId());
        payload.put("name", certificate.getHolder().getFullName());
        payload.put("remainingValue", certificate.getRemainingValue().toString());

        byte[] qrCodeImage = qrCodeService.createQrCode(payload.toString());

        Map<String, Object> content = new HashMap<>();

        SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");

        content.put("qrCode", qrCodeImage);
        content.put("value", "%d€".formatted(certificate.getValue()));
        content.put("valid_until", sf.format(certificate.getValidUntil()));
        content.put("from", certificate.getSender().getFullName());
        content.put("to", certificate.getHolder().getFullName());
        content.put("description", certificate.getDescription());

        emailService.sendHTMLEmail(
                certificate.getHolder().getEmail(),
                "Congratulations you received restaurant certificate",
                "successfulCertificatePayment",
                content
        );

        return CertificateActivationResponse.builder()
                .message("QR Code was successfully activated!")
                .build();
    }
}
