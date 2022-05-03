package com.endava.endabank.utils;

import com.endava.endabank.configuration.MailProperties;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class MailService {

    private MailProperties mailProperties;

    public  Response sendEmail(String emailTo, String name, String link,
                                     String templateId, String asName) throws IOException {
        if (emailTo == null || name == null || link == null || templateId == null || asName == null) {
            throw new IllegalArgumentException("All parameters must be not null");
        }
        if (emailTo.isEmpty() || name.isEmpty() || link.isEmpty() || templateId.isEmpty() || asName.isEmpty()) {
            throw new IllegalArgumentException("All parameters must be not empty");
        }
        return invokeServiceEmail(configureMail(templateId, asName, emailTo, name, link));
    }

    public SendGrid getSendGrid() {
        return new SendGrid(mailProperties.getApiKey());
    }


    public Personalization getPersonalization(String emailTo, String name, String link) {
        Personalization personalization = new Personalization();
        personalization.addDynamicTemplateData("name", name);
        personalization.addDynamicTemplateData("link", link);
        personalization.addTo(new Email(emailTo));
        return personalization;
    }

    public  Mail configureMail(String templateId, String asName,
                                     String emailTo, String name, String link) {
        Mail mail = new Mail();
        Email fromEmail = new Email();
        fromEmail.setName(asName);
        fromEmail.setEmail(mailProperties.getFromEmail());
        mail.setFrom(fromEmail);
        mail.setTemplateId(templateId);
        mail.addPersonalization(getPersonalization(emailTo, name, link));
        return mail;
    }

    public  Response invokeServiceEmail(Mail mail) throws IOException {
        SendGrid sg = getSendGrid();
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        return sg.api(request);
    }
}
