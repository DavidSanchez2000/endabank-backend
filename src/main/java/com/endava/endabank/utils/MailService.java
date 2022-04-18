package com.endava.endabank.utils;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MailService {

    public static Response sendEmail(String emailTo, String name, String link, String templateId, String asName) throws IOException {
        Mail mail = new Mail();
        Email fromEmail = new Email();
        fromEmail.setName(asName);
        fromEmail.setEmail(System.getenv("SENDGRID_MAIL_FROM"));
        mail.setFrom(fromEmail);
        mail.setTemplateId(templateId);
        Personalization personalization = new Personalization();
        personalization.addDynamicTemplateData("name", name);
        personalization.addDynamicTemplateData("link", link);
        personalization.addTo(new Email(emailTo));
        mail.addPersonalization(personalization);
        SendGrid sg = new SendGrid(
                System.getenv("SENDGRID_API_KEY")
        );
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        return sg.api(request);


    }
}
