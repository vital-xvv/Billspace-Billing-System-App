package io.vital.billspace.service.implementation;

import io.vital.billspace.service.EmailService;
import io.vital.billspace.utils.EmailUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final EmailUtils emailUtils;
    private final JavaMailSender emailSender;
    @Value("${spring.mail.verify.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String fromMail;
    @Value("${application.title}")
    private String appTitle;

    @Override
    public void sendSimpleMailMessage(String name, String to, String url) {
        try{
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setSubject(appTitle + ". New User Account Verification");
            simpleMailMessage.setFrom(fromMail);
            simpleMailMessage.setTo(to);
            simpleMailMessage.setText(emailUtils.getAccountVerificationMessage(name, url));
            emailSender.send(simpleMailMessage);

        }catch (Exception e){
            System.out.println(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public void sendMimeMessageWithAttachment(String name, String to, String token) {

    }

    @Override
    public void sendMimeMessageWithEmbeddedImages(String name, String to, String token) {

    }

    @Override
    public void sendMimeMessageWithEmbeddedFiles(String name, String to, String token) {

    }

    @Override
    public void sendHtmlEmail(String name, String to, String token) {

    }

    @Override
    public void sendHtmlEmailWithEmbeddedFiles(String name, String to, String token) {

    }
}
