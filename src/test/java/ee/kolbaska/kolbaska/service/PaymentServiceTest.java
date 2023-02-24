package ee.kolbaska.kolbaska.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.google.zxing.WriterException;
import ee.kolbaska.kolbaska.exception.PaymentException;
import ee.kolbaska.kolbaska.exception.PaymentNotFoundException;
import ee.kolbaska.kolbaska.model.certificate.Certificate;
import ee.kolbaska.kolbaska.model.payment.Payment;
import ee.kolbaska.kolbaska.model.payment.Status;
import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.repository.CertificateRepository;
import ee.kolbaska.kolbaska.repository.PaymentRepository;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.CertificateVerificationRequest;
import ee.kolbaska.kolbaska.response.CertificateVerificationResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.QrCodeService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource("/tests.properties")
public class PaymentServiceTest {

    private String JWT_SECRET = "supersecret";


    @MockitoSettings(strictness = Strictness.LENIENT)
    @Nested
    @DisplayName("verificationCreation method tests")
    @TestPropertySource("/tests.properties")
    class VerificationCreation {

        private PaymentRepository paymentRepository;
        private UserRepository userRepository;
        private CertificateRepository certificateRepository;
        private RoleRepository roleRepository;
        private QrCodeService qrCodeService;
        private EmailService emailService;
        private PaymentService paymentService;

        private final String paymentId = UUID.randomUUID().toString();
        private final String paymentStatus = "PAID";
        private final String accessKey = "someaccesskey";

        @BeforeEach
        void setUp() {
            paymentRepository = Mockito.mock(PaymentRepository.class);
            userRepository = Mockito.mock(UserRepository.class);
            certificateRepository = Mockito.mock(CertificateRepository.class);
            roleRepository = Mockito.mock(RoleRepository.class);
            qrCodeService = Mockito.mock(QrCodeService.class);
            emailService = Mockito.mock(EmailService.class);
            paymentService = new PaymentService(certificateRepository, userRepository, paymentRepository, qrCodeService, emailService, roleRepository);
        }

        private String createToken(String paymentId, String accessKey, String status) {
            return JWT.create()
                    .withClaim("uuid", paymentId)
                    .withClaim("paymentStatus", status)
                    .withClaim("accessKey", accessKey)
                    .sign(Algorithm.HMAC256(JWT_SECRET));
        }

        private Payment createPayment() {
            return Payment.builder()
                    .id(paymentId)
                    .value(50)
                    .toEmail("jane@example.com")
                    .fromEmail("john@example.com")
                    .toFullName("Jane")
                    .fromFullName("John")
                    .phone("555-5555")
                    .description("Certificate description")
                    .status(Status.PENDING)
                    .build();
        }

        @Test
        @DisplayName("Should successfully verify a payment with a valid token")
        void testVerificationCreationValidToken() throws PaymentNotFoundException, PaymentException, IOException, MessagingException, WriterException, TemplateException {
            // Arrange
            Payment payment = createPayment();
            String orderToken = createToken(paymentId, accessKey, "PAID");
            Map<String, Claim> claims = JWT.decode(orderToken).getClaims();
            String uuid = claims.get("uuid").asString();

            Role customerRole = Role.builder()
                    .id(1L)
                    .roleName("ROLE_CUSTOMER")
                    .build();

            // Act
            when(paymentRepository.findById(uuid)).thenReturn(Optional.of(payment));
            when(roleRepository.findRoleByRoleName("ROLE_CUSTOMER")).thenReturn(Optional.of(customerRole));
            CertificateVerificationRequest request = CertificateVerificationRequest.builder().orderToken(orderToken).build();
            CertificateVerificationResponse response = paymentService.verificationCreation(request);

            // Assert
            assertNotNull(response);
            assertEquals("Certificate was successful created and sent to the receiver", response.getMessage());
            assertEquals(Status.PAID, payment.getStatus());
            verify(certificateRepository, times(1)).save(any(Certificate.class));
            verify(paymentRepository, times(1)).save(payment);
            verify(emailService, times(1)).sendHTMLEmail(any(String.class), any(String.class), any(String.class), any(Map.class));
        }

        @Test
        @DisplayName("Should throw PaymentException when payment status is not PAID")
        void testVerificationCreationInvalidPaymentStatus() throws MessagingException, TemplateException, IOException {
            // Arrange
            Payment payment = createPayment();
            payment.setStatus(Status.PENDING);
            String orderToken = createToken(paymentId, accessKey, "PENDING");
            Map<String, Claim> claims = JWT.decode(orderToken).getClaims();
            String uuid = claims.get("uuid").asString();

            Role customerRole = Role.builder()
                    .id(1L)
                    .roleName("ROLE_CUSTOMER")
                    .build();

            // Act & Assert
            when(paymentRepository.findById(uuid)).thenReturn(Optional.of(payment));
            when(roleRepository.findRoleByRoleName("ROLE_CUSTOMER")).thenReturn(Optional.of(customerRole));
            CertificateVerificationRequest request = CertificateVerificationRequest.builder().orderToken(orderToken).build();
            assertThrows(PaymentException.class, () -> paymentService.verificationCreation(request));

            verify(certificateRepository, times(0)).save(any(Certificate.class));
            verify(paymentRepository, times(0)).save(payment);
            verify(emailService, times(0)).sendHTMLEmail(any(String.class), any(String.class), any(String.class), any(Map.class));
        }

        @Test
        @DisplayName("Should throw PaymentException when access key is invalid")
        void testVerificationCreationInvalidAccessKey() throws MessagingException, TemplateException, IOException {
            // Arrange
            Payment payment = createPayment();
            String orderToken = createToken(paymentId, "invalidaccesskey", "PAID");
            Map<String, Claim> claims = JWT.decode(orderToken).getClaims();
            String uuid = claims.get("uuid").asString();

            // Act & Assert
            when(paymentRepository.findById(uuid)).thenReturn(Optional.of(payment));
            CertificateVerificationRequest request = CertificateVerificationRequest.builder().orderToken(orderToken).build();
            assertThrows(PaymentException.class, () -> paymentService.verificationCreation(request));

            verify(certificateRepository, times(0)).save(any(Certificate.class));
            verify(paymentRepository, times(0)).save(payment);
            verify(emailService, times(0)).sendHTMLEmail(any(String.class), any(String.class), any(String.class), any(Map.class));
        }

        @Test
        @DisplayName("Should throw PaymentNotFoundException when payment is not found")
        void testVerificationCreationInvalidPaymentNotFound() throws MessagingException, TemplateException, IOException {
            // Arrange
            String orderToken = createToken(paymentId, accessKey, "PAID");
            Map<String, Claim> claims = JWT.decode(orderToken).getClaims();
            String uuid = claims.get("uuid").asString();

            // Act & Assert
            when(paymentRepository.findById(uuid)).thenReturn(Optional.empty());
            CertificateVerificationRequest request = CertificateVerificationRequest.builder().orderToken(orderToken).build();
            assertThrows(PaymentNotFoundException.class, () -> paymentService.verificationCreation(request));

            verify(certificateRepository, times(0)).save(any(Certificate.class));
            verify(paymentRepository, times(0)).save(any(Payment.class));
            verify(emailService, times(0)).sendHTMLEmail(any(String.class), any(String.class), any(String.class), any(Map.class));
        }
    }
}
