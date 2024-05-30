package io.vital.billspace.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class EmailUtils {
    @Value("${application.title}")
    private String appTitle;

    public String getVerificationUrl(String token, String type){
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/user/verify/" + type + "/" + token).toUriString();
    }

    public String getAccountVerificationMessage(String name, String url){
        //String verificationUrl = EmailUtils.getVerificationUrl(token, VerificationType.ACCOUNT.getType());
        return String.format("Hello, %s! \n\nYou have successfully created your %s account. \nPlease follow the link to verify your newly created account: \n%s",
                name, appTitle, url);
    }
}
