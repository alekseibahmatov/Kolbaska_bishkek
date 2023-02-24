package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.config.UserConfiguration;
import ee.kolbaska.kolbaska.exception.CertificateInsufficientFundsException;
import ee.kolbaska.kolbaska.exception.CertificateIsDisabledException;
import ee.kolbaska.kolbaska.exception.CertificateIsOutDatedException;
import ee.kolbaska.kolbaska.exception.CertificateNotFoundException;
import ee.kolbaska.kolbaska.model.certificate.Certificate;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.transaction.Transaction;
import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.CertificateRepository;
import ee.kolbaska.kolbaska.repository.TransactionRepository;
import ee.kolbaska.kolbaska.request.CertificateActivationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.AccessDeniedException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private UserConfiguration userConfiguration;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User workerNormal, workerWithoutRestaurant, customer;

    private Certificate certificateNormal, certificateOutDated, certificateInsufficientFunds, certificateDisabled;

    private Restaurant restaurant;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        transactionService = new TransactionService(userConfiguration, certificateRepository, transactionRepository);

        Role roleWaiter = Role.builder()
                .id(1L)
                .roleName("ROLE_WAITER")
                .build();

        Role roleCustomer = Role.builder()
                .id(1L)
                .roleName("ROLE_CUSTOMER")
                .build();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2099, Calendar.DECEMBER, 31);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(2000, Calendar.DECEMBER, 31);

        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("R14");
        restaurant.setRestaurantCode("R12L23");

        workerNormal = new User();
        workerNormal.setId(1L);
        workerNormal.setFullName("Vasja Pupkin");
        workerNormal.setRestaurant(restaurant);
        workerNormal.setRole(roleWaiter);

        workerWithoutRestaurant = new User();
        workerWithoutRestaurant.setId(2L);
        workerWithoutRestaurant.setFullName("Irina");
        workerWithoutRestaurant.setRole(roleWaiter);

        restaurant.setWaiters(List.of(workerNormal));

        certificateNormal = new Certificate();
        certificateNormal.setId("123");
        certificateNormal.setActive(true);
        certificateNormal.setValidUntil(calendar.getTime());
        certificateNormal.setValue(200);
        certificateNormal.setRemainingValue(200.0);

        certificateDisabled = new Certificate();
        certificateDisabled.setId("321");
        certificateDisabled.setActive(false);
        certificateDisabled.setValidUntil(calendar.getTime());
        certificateDisabled.setValue(200);
        certificateDisabled.setRemainingValue(200.0);

        certificateInsufficientFunds = new Certificate();
        certificateInsufficientFunds.setId("3212");
        certificateInsufficientFunds.setActive(true);
        certificateInsufficientFunds.setValidUntil(calendar.getTime());
        certificateInsufficientFunds.setValue(200);
        certificateInsufficientFunds.setRemainingValue(0.0);

        certificateOutDated = new Certificate();
        certificateOutDated.setId("32122");
        certificateOutDated.setActive(true);
        certificateOutDated.setValidUntil(calendar2.getTime());
        certificateOutDated.setValue(200);
        certificateOutDated.setRemainingValue(200.0);

        customer = new User();
        customer.setId(3L);
        customer.setFullName("Galina");
        customer.setRole(roleCustomer);
        customer.setReceivedCertificates(List.of(certificateNormal, certificateDisabled, certificateInsufficientFunds, certificateOutDated));

        certificateNormal.setHolder(customer);
        certificateDisabled.setHolder(customer);
        certificateOutDated.setHolder(customer);
        certificateInsufficientFunds.setHolder(customer);
    }

    @Test
    void testActivateCertificate() throws AccessDeniedException, CertificateIsDisabledException, CertificateNotFoundException, CertificateInsufficientFundsException, CertificateIsOutDatedException {
        String certificateId = "123";
        Double amountToActivate = 12.0;
        Double expectedRemainingValue = certificateNormal.getRemainingValue() - amountToActivate;

        Transaction transaction = Transaction.builder()
                .id("1233333")
                .certificate(certificateNormal)
                .waiter(workerNormal)
                .restaurant(restaurant)
                .value(12.0)
                .build();

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificateNormal));
        when(userConfiguration.getRequestUser()).thenReturn(workerNormal);
        when(transactionRepository.save(any())).thenReturn(transaction);

        transactionService.activateCertificate(CertificateActivationRequest.builder()
                .uniqueCode(certificateId)
                .amount(amountToActivate)
                .build());

        assertEquals(expectedRemainingValue, certificateNormal.getRemainingValue());
        assertEquals(1, certificateNormal.getTransactions().size());
        assertEquals(certificateNormal, certificateNormal.getTransactions().get(0).getCertificate());
        assertEquals(workerNormal.getRestaurant(), certificateNormal.getTransactions().get(0).getRestaurant());
    }

    @Test
    void testActivateCertificateWithInsufficientFunds() {
        String certificateId = "3212";
        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificateInsufficientFunds));
        when(userConfiguration.getRequestUser()).thenReturn(workerNormal);
        assertThrows(CertificateInsufficientFundsException.class, () -> transactionService.activateCertificate(CertificateActivationRequest.builder()
                .uniqueCode(certificateId)
                .amount(12.0)
                .build()));
    }

    @Test
    void testActivateCertificateWithOutdated() {
        String certificateId = "32122";
        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificateOutDated));
        when(userConfiguration.getRequestUser()).thenReturn(workerNormal);
        assertThrows(CertificateIsOutDatedException.class, () -> transactionService.activateCertificate(CertificateActivationRequest.builder()
                .uniqueCode(certificateId)
                .amount(12.0)
                .build()));
    }

    @Test
    void testActivateCertificateByWorkerWithoutRestaurant() {
        String certificateId = "123";
        when(userConfiguration.getRequestUser()).thenReturn(workerWithoutRestaurant);
        assertThrows(AccessDeniedException.class, () -> transactionService.activateCertificate(CertificateActivationRequest.builder()
                .uniqueCode(certificateId)
                .amount(12.0)
                .build()));
    }

    @Test
    void testActivateCertificateWithDisabled() {
        String certificateId = "321";
        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificateDisabled));
        when(userConfiguration.getRequestUser()).thenReturn(workerNormal);
        assertThrows(CertificateIsDisabledException.class, () -> transactionService.activateCertificate(CertificateActivationRequest.builder()
                .uniqueCode(certificateId)
                .amount(12.0)
                .build()));
    }

    @Test
    void testActivateCertificateByCustomer() {
        String certificateId = "123";
        when(userConfiguration.getRequestUser()).thenReturn(customer);
        assertThrows(AccessDeniedException.class, () -> transactionService.activateCertificate(CertificateActivationRequest.builder()
                .uniqueCode(certificateId)
                .amount(12.0)
                .build()));
    }

}