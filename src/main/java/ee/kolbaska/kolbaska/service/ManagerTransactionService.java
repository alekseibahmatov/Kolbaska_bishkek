package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.config.UserConfiguration;
import ee.kolbaska.kolbaska.exception.CertificateInsufficientFundsException;
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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ManagerTransactionService {

    private final UserConfiguration userConfiguration;

    private final CertificateRepository certificateRepository;

    private final TransactionRepository transactionRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagerRestaurantService.class);

    @Transactional
    public CertificateActivationResponse activateCertificate(CertificateActivationRequest request) throws AccessDeniedException, CertificateNotFoundException, CertificateInsufficientFundsException, CertificateIsDisabledException, CertificateIsOutDatedException {
        User worker = userConfiguration.getRequestUser();
        LOGGER.info("Attempting to activate certificate with unique code: {} for restaurant: {}", request.getUniqueCode(), worker.getRestaurant().getName());

        if (worker.getRestaurant() == null) throw new AccessDeniedException("You are not allowed to make this action");

        Optional<Certificate> isCertificate = certificateRepository.findById(request.getUniqueCode());

        if (isCertificate.isEmpty()) throw new CertificateNotFoundException("Certificate with unique code you provided is not registered in the database");

        Certificate certificate = isCertificate.get();

        if (certificate.getRemainingValue() < request.getAmount()) throw new CertificateInsufficientFundsException("This certificate does not have that much funds to use");
        if (!certificate.getActive()) throw new CertificateIsDisabledException("This certificate is disabled");
        if (certificate.getValidUntil().before(new Date())) throw new CertificateIsOutDatedException("This certificate is no longer active");

        Transaction newTransaction = Transaction.builder()
                .certificate(certificate)
                .value(request.getAmount())
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

        return CertificateActivationResponse.builder()
                .message("QR Code was successfully activated!")
                .build();
    }
}
