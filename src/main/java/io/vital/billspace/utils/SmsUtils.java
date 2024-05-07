package io.vital.billspace.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import static com.twilio.rest.api.v2010.account.Message.creator;

public class SmsUtils {
    public static final String FROM_NUMBER = "";
    public static final String SID = "";
    public static final String TOKEN_KEY = "";

    public static void sendSMS(String toNumber, String messageBody){
        Twilio.init(SID, TOKEN_KEY);
        Message message = creator(new PhoneNumber(toNumber), new PhoneNumber(FROM_NUMBER), messageBody).create();
    }

}
