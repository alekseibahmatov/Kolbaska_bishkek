package ee.kolbaska.kolbaska.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.zxing.WriterException;
import ee.kolbaska.kolbaska.exception.PaymentException;
import ee.kolbaska.kolbaska.exception.PaymentNotFoundException;
import ee.kolbaska.kolbaska.model.certificate.Certificate;
import ee.kolbaska.kolbaska.model.payment.Payment;
import ee.kolbaska.kolbaska.model.payment.Status;
import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.CertificateRepository;
import ee.kolbaska.kolbaska.repository.PaymentRepository;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.CertificateCreationRequest;
import ee.kolbaska.kolbaska.request.CertificateVerificationRequest;
import ee.kolbaska.kolbaska.response.CertificateCreationResponse;
import ee.kolbaska.kolbaska.response.CertificateVerificationResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.QrCodeService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerPaymentService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final QrCodeService qrCodeService;
    private final EmailService emailService;
    private final RoleRepository roleRepository;


    @Value("${jwt.payment.secret}")
    private String JWT_SECRET = "supersecret";

    @Value("${api.baseurl}")
    private String API_BASEURL = "http://localhost:8080";

    @Transactional
    public CertificateCreationResponse initiateCreation(CertificateCreationRequest request) {

        Payment.PaymentBuilder newPayment = Payment.builder()
                .value(request.getValue())
                .toEmail(request.getToEmail())
                .fromEmail(request.getFromEmail())
                .toFullName(request.getToFullName())
                .fromFullName(request.getFromFullName())
                .phone(request.getToPhone())
                .description(request.getCongratsText())
                .status(Status.PENDING);


        //TODO here add implementation of montonio payment system.
        // Has to be payload creation method, jwt signing method and method that will send request to montonio servers to register new order

        newPayment.id(UUID.randomUUID().toString()); //TODO when montonio is connected change this to its UUID from response

        paymentRepository.save(newPayment.build());

        return CertificateCreationResponse.builder()
                .redirectUrl("https://dummymontonio.com/someshittyoreder")
                .build();
    }

    @Transactional
    public CertificateVerificationResponse verificationCreation(CertificateVerificationRequest request) throws PaymentNotFoundException, PaymentException, IOException, WriterException, MessagingException, TemplateException {

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(JWT_SECRET)).build().verify(request.getOrderToken());

        Map<String, Claim> claims = decodedJWT.getClaims();

        Optional<Payment> ifPayment = paymentRepository.findById(claims.get("uuid").asString());

        if(ifPayment.isEmpty()) throw new PaymentNotFoundException("Payment wasn't found");
        Payment payment = ifPayment.get();

        if (
                claims.get("paymentStatus").asString().equals("PAID") &&
                claims.get("accessKey").asString().equals("someaccesskey") //TODO change this when montonio payment when we will integrate it
        ) {
            Optional<User> ifHolder = userRepository.findByEmail(payment.getToEmail());
            Optional<User> ifSender = userRepository.findByEmail(payment.getFromEmail());

            Role customerRole = roleRepository.findRoleByRoleName("ROLE_CUSTOMER").get();

            User holder = ifHolder.orElseGet(() -> userRepository.save(User.builder()
                    .fullName(payment.getToFullName())
                    .phone(payment.getPhone())
                    .email(payment.getToEmail())
                    .deleted(false)
                    .activated(false)
                    .roles(List.of(customerRole))
                    .build()));

            User sender = ifSender.orElseGet(() -> userRepository.save(User.builder()
                    .email(payment.getFromEmail())
                    .fullName(payment.getToFullName())
                    .deleted(false)
                    .activated(false)
                    .roles(List.of(customerRole))
                    .build()));

            Calendar cal = Calendar.getInstance();

            cal.add(Calendar.YEAR, 1);

            String id = UUID.randomUUID().toString();

            Certificate newCertificate = Certificate.builder()
                    .id(id)
                    .holder(holder)
                    .sender(sender)
                    .description(payment.getDescription())
                    .value(payment.getValue())
                    .validUntil(cal.getTime())
                    .active(true)
                    .createdByAdmin(false)
                    .build();

            certificateRepository.save(newCertificate);
            payment.setStatus(Status.PAID);
            paymentRepository.save(payment);

            String qrCodeUrl = "%s/api/v1/certificate/%s".formatted(API_BASEURL, id);

            byte[] qrCodeImage = qrCodeService.createQrCode(qrCodeUrl);

            Map<String, Object> content = new HashMap<>();

            SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");

            content.put("qrCode", qrCodeImage);
            content.put("value", "%d€".formatted(payment.getValue()));
            content.put("valid_until", sf.format(cal.getTime()));
            content.put("from", payment.getFromFullName());
            content.put("to", payment.getToFullName());
            content.put("description", payment.getDescription());

            emailService.sendHTMLEmail(
                    payment.getToEmail(),
                    "Congratulations you received restaurant certificate",
                    "successfulCertificatePayment",
                    content
            );

            return CertificateVerificationResponse.builder()
                    .message("Certificate was successful created and sent to the receiver")
                    .build();
        }
        throw new PaymentException("Something with your access key or payment status");
    }
}
