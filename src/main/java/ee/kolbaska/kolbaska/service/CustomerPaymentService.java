package ee.kolbaska.kolbaska.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import ee.kolbaska.kolbaska.exception.PaymentException;
import ee.kolbaska.kolbaska.exception.PaymentNotFoundException;
import ee.kolbaska.kolbaska.jsonModel.paymentData.PaymentData;
import ee.kolbaska.kolbaska.jsonModel.paymentMethods.Country;
import ee.kolbaska.kolbaska.jsonModel.paymentMethods.MainPaymentResponse;
import ee.kolbaska.kolbaska.jsonModel.paymentMethods.PaymentInitiation;
import ee.kolbaska.kolbaska.jsonModel.paymentMethods.PaymentMethod;
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
import ee.kolbaska.kolbaska.request.PaymentValidationRequest;
import ee.kolbaska.kolbaska.response.CertificateCreationResponse;
import ee.kolbaska.kolbaska.response.CertificateVerificationResponse;
import ee.kolbaska.kolbaska.response.PaymentMethodResponse;
import ee.kolbaska.kolbaska.response.PaymentValidationResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.QrCodeService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private RestTemplate restTemplate;

    @Value("${montonio.api.url}")
    private String MONTONIO_API_URL;

    @Value("${montonio.accessKey}")
    private String MONTONIO_ACCESS_KEY;

    @Value("${montonio.secretKey}")
    private String MONTONIO_SECRET_KEY;

    @Value("${jwt.payment.secret}")
    private String JWT_SECRET = "supersecret";

    @Value("${front.baseurl}")
    private String WEBSITE_BASE_URL;

    @Value("${api.baseurl}")
    private String API_BASEURL;

    @Value("${api.basepath}")
    private String API_BASEPATH;

    @Transactional
    public CertificateCreationResponse initiateCreation(CertificateCreationRequest request) throws JsonProcessingException {

        Map<String, Object> payload = new HashMap<>();

        String uuid = UUID.randomUUID().toString();

        payload.put("accessKey", MONTONIO_ACCESS_KEY);
        payload.put("merchantReference", uuid);
        payload.put("returnUrl", "%s/personal-coupon-order/order-details".formatted(WEBSITE_BASE_URL)); //TODO change this to variable
        payload.put("notificationUrl", "%s%s/payment/verificationCreation".formatted(API_BASEURL, API_BASEPATH)); //TODO change this to variable
        payload.put("grandTotal", request.getValue());
        payload.put("currency", "EUR");

        Map<String, Object> paymentMethod = new HashMap<>();
        paymentMethod.put("method", "paymentInitiation"); //TODO in the future change this so it choose method automatically
        paymentMethod.put("currency", "EUR");
        paymentMethod.put("amount", request.getValue());

        payload.put("payment", paymentMethod);

        Map<String, String> billingAddress = new HashMap<>();
        billingAddress.put("firstName", request.getFromFullName());
        billingAddress.put("email", request.getFromEmail());
        billingAddress.put("phoneNumber", request.getFromPhone());
        billingAddress.put("addressLine1", request.getBillingAddress().getStreet());
        billingAddress.put("locality", request.getBillingAddress().getCity());
        billingAddress.put("region", request.getBillingAddress().getState());
        billingAddress.put("postalCode", request.getBillingAddress().getZipCode());
        billingAddress.put("country", "EE"); //TODO fix this somehow

        payload.put("billingAddress", billingAddress);
        payload.put("locale", "et"); //TODO fix this somehow

        String jwtToken = JWT
                .create()
                .withPayload(payload)
                .withExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .withIssuedAt(Instant.now())
                .sign(Algorithm.HMAC256(MONTONIO_SECRET_KEY));

        String requestUrl = MONTONIO_API_URL + "/orders";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        String requestBody = "{\"data\": \""+ jwtToken +"\"}";

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.POST, entity, String.class);

        String jsonResponse = response.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        PaymentData payments = objectMapper.readValue(jsonResponse, PaymentData.class);

        Payment newPayment = Payment.builder()
                .value(request.getValue())
                .toEmail(request.getToEmail())
                .fromEmail(request.getFromEmail())
                .toFullName(request.getToFullName())
                .fromFullName(request.getFromFullName())
                .phone(request.getToPhone())
                .description(request.getCongratsText())
                .status(Status.PENDING)
                .build();

        paymentRepository.save(newPayment);

        return CertificateCreationResponse.builder()
                .redirectUrl(payments.getPaymentUrl())
                .build();
    }

    @Transactional
    public CertificateVerificationResponse verificationCreation(CertificateVerificationRequest request) throws PaymentNotFoundException, PaymentException, IOException, WriterException, MessagingException, TemplateException {

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(JWT_SECRET)).build().verify(request.getOrderToken());

        Map<String, Claim> claims = decodedJWT.getClaims();

        Optional<Payment> ifPayment = paymentRepository.findById(claims.get("merchantReference").asString());

        if(ifPayment.isEmpty()) throw new PaymentNotFoundException("Payment wasn't found");
        Payment payment = ifPayment.get();

        if (
                claims.get("paymentStatus").asString().equals("PAID") &&
                claims.get("accessKey").asString().equals(MONTONIO_ACCESS_KEY)
        ) {
            Optional<User> ifHolder = userRepository.findByEmail(payment.getToEmail());
            Optional<User> ifSender = userRepository.findByEmail(payment.getFromEmail());

            Role customerRole = roleRepository.findRoleByRoleName("ROLE_CUSTOMER").get();

            User holder = ifHolder.orElseGet(() -> userRepository.save(User.builder()
                    .fullName(payment.getToFullName())
                    .phone(payment.getPhone())
                    .email(payment.getToEmail())
                    .activated(false)
                    .roles(List.of(customerRole))
                    .build()));

            User sender = ifSender.orElseGet(() -> userRepository.save(User.builder()
                    .email(payment.getFromEmail())
                    .fullName(payment.getToFullName())
                    .activated(false)
                    .roles(List.of(customerRole))
                    .build()));

            LocalDate validUntilDate = LocalDate.now().minusYears(1);

            Certificate newCertificate = Certificate.builder()
                    .holder(holder)
                    .sender(sender)
                    .description(payment.getDescription())
                    .value(payment.getValue())
                    .validUntil(validUntilDate)
                    .active(true)
                    .createdByAdmin(false)
                    .build();

            newCertificate = certificateRepository.save(newCertificate);
            payment.setStatus(Status.PAID);
            paymentRepository.save(payment);

            Map<String, String> payload = new HashMap<>();

            payload.put("certificate_id", newCertificate.getId().toString());
            payload.put("name", holder.getFullName());
            payload.put("remainingValue", payment.getValue().toString());

            byte[] qrCodeImage = qrCodeService.createQrCode(payload.toString());

            Map<String, Object> content = new HashMap<>();

            DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter();

            content.put("qrCode", qrCodeImage);
            content.put("value", "%f€".formatted(payment.getValue()));
            content.put("valid_until", validUntilDate.format(dtf));
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

    public Map<String, List<PaymentMethodResponse>> methods() throws JsonProcessingException {

        Map<String, Object> payload = new HashMap<>();
        payload.put("accessKey", MONTONIO_ACCESS_KEY);

        String jwtToken = JWT
                .create()
                .withPayload(payload)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .sign(Algorithm.HMAC256(MONTONIO_SECRET_KEY));

        String requestUrl = MONTONIO_API_URL + "/stores/payment-methods";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setBearerAuth(jwtToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, entity, String.class);
        String jsonResponse = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        MainPaymentResponse payments = objectMapper.readValue(jsonResponse, MainPaymentResponse.class);

        PaymentInitiation paymentInitiation = payments.getPaymentMethods().getPaymentInitiation();

        Map<String, Country> setup = paymentInitiation.getSetup();

        Map<String, List<PaymentMethodResponse>> finalResponse = new HashMap<>();

        for (Map.Entry<String, Country> country : setup.entrySet()) {
            List<PaymentMethodResponse> countryMethods = new ArrayList<>();
            for (PaymentMethod method : country.getValue().getPaymentMethods()) {
                PaymentMethodResponse newMethod = PaymentMethodResponse.builder()
                        .paymentName(method.getName())
                        .paymentCode(method.getCode())
                        .logoUrl(method.getLogoUrl())
                        .build();

                countryMethods.add(newMethod);
            }
            finalResponse.put(country.getKey(), countryMethods);
        }

        return finalResponse;
    }

    public PaymentValidationResponse validatePayment(PaymentValidationRequest request) {
        try {
           JWT.require(Algorithm.HMAC256(JWT_SECRET)).build().verify(request.getOrderToken());
        } catch (JWTVerificationException e) {
            return PaymentValidationResponse.builder().success(false).build();
        }
        return PaymentValidationResponse.builder().success(true).build();
    }
}
