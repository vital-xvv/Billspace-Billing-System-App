package io.vital.billspace.utils;

import io.vital.billspace.enumeration.VerificationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class EmailUtils {
    @Value("${application.title}")
    private static String appTitle;

    public static String getVerificationUrl(String token, String type){
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/user/verify/" + type + "/" + token).toUriString();
    }

    public static String getAccountVerificationMessage(String name, String token){
        String verificationUrl = EmailUtils.getVerificationUrl(token, VerificationType.ACCOUNT.getType());
        return String.format("Hello, %s! \n\nYou have successfully created your %s account. \nPlease follow the link to verify your newly created account: \n%s",
                name, appTitle, verificationUrl);
    }
}
