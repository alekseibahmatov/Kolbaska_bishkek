package ee.maitsetuur.service.miscellaneous;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;

    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void sendHTMLEmail(String to, String subject, String templateName, Map<String, Object> data) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        Context context = new Context();
        context.setVariables(data);
        helper.setText(templateEngine.process(templateName, context), true);

        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);

        emailSender.send(message);
    }

    public void sendHTMLEmail(String to, String subject, String templateName, Map<String, Object> data, byte[] pdfBytes) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        Context context = new Context();
        context.setVariables(data);
        helper.setText(templateEngine.process(templateName, context), true);

        // convert byte array to DataSource
        ByteArrayDataSource pdfDataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");

        // add pdf as attachment
        helper.addAttachment("certificate.pdf", pdfDataSource);

        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);

        emailSender.send(message);
    }

}