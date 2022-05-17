package com.endava.endabank.utils;

import com.endava.endabank.configuration.MailProperties;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Assert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    private final String templateId = "test";
    private final String emailTo = "test";
    private final String asName = "test";
    private final String name = "testUser";
    private final String link = "http://test.com";
    private final MailProperties mailProperties = new MailProperties("test","test", "test", "test");

    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailService = new MailService(mailProperties);
    }

    @Test
    void testSendEmailShouldSuccessWhenDataCorrect() throws IOException {
            MailService mailService1 = Mockito.spy(mailService);
            Mail mail = mailService.configureMail(templateId, asName, emailTo, name, link);
            SendGrid sg = Mockito.mock(SendGrid.class);
            when(mailService1.getSendGrid()).thenReturn(sg);
            when(mailService1.configureMail(templateId, asName, emailTo,name, link)).thenReturn(mail);
            when(sg.api(any(Request.class))).thenReturn(TestUtils.getSendGridResponse());
            Response response =  mailService1.sendEmail(emailTo, name, link, templateId, asName);
            assertEquals(202, response.getStatusCode());
            assertEquals("Success", response.getBody());
    }

    @Test
    void testSendEmailShouldFailWhenNullParameters() {
        assertThrows(IllegalArgumentException.class, () ->
                mailService.sendEmail(null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () ->
                mailService.sendEmail(null, name, link, templateId, asName));
        assertThrows(IllegalArgumentException.class, () ->
                mailService.sendEmail(emailTo, null, link, templateId, asName));
        assertThrows(IllegalArgumentException.class, () ->
                mailService.sendEmail(emailTo, name, null, templateId, asName));
        assertThrows(IllegalArgumentException.class, () ->
                mailService.sendEmail(emailTo, name, link, null, asName));
        assertThrows(IllegalArgumentException.class, () ->
                mailService.sendEmail(emailTo, name, link, templateId, null));
    }

    @Test
    void testSendEmailShouldFailWhenEmptyParameters() {
        assertThrows(IllegalArgumentException.class, () ->
                mailService.sendEmail("", "", "", "", ""));
        assertThrows(IllegalArgumentException.class, () ->
                mailService.sendEmail("", name, link, templateId, asName));
        assertThrows(IllegalArgumentException.class, () ->
                mailService.sendEmail(emailTo, "", link, templateId, asName));
        assertThrows(IllegalArgumentException.class, () ->
                mailService.sendEmail(emailTo, name, "", templateId, asName));
        assertThrows(IllegalArgumentException.class, () ->
                mailService.sendEmail(emailTo, name, link, "", asName));
        assertThrows(IllegalArgumentException.class, () ->
                mailService.sendEmail(emailTo, name, link, templateId, ""));
    }

    @Test
    void testGetPersonalizationTestShouldSuccessWhenDataCorrect() {
        Personalization personalization = mailService.getPersonalization(emailTo, name, link);
        assertEquals(emailTo, personalization.getTos().get(0).getEmail());
        assertEquals(name, personalization.getDynamicTemplateData().get("name"));
        assertEquals(link, personalization.getDynamicTemplateData().get("link"));
    }

    @Test
    void testConfigureMailTestShouldSuccessWhenDataCorrect() {
        Mail mail = mailService.configureMail(templateId, asName, emailTo, name, link);
        assertEquals(mailProperties.getFromEmail(), mail.getFrom().getEmail());
        assertEquals(asName, mail.getFrom().getName());
        assertEquals(templateId, mail.getTemplateId());
    }

    @Test
    void testInvokeServiceEmailShouldSuccessWhenDataCorrect() throws IOException {
        SendGrid sg = Mockito.mock(SendGrid.class);
        Mail mail = mailService.configureMail(templateId, asName, emailTo, name, link);
        MailService mailService1 = Mockito.spy(mailService);
        when(mailService1.getSendGrid()).thenReturn(sg);
        mailService1.invokeServiceEmail(mail);
        ArgumentCaptor<Request> argumentCaptor
                = ArgumentCaptor.forClass(Request.class);
        verify(sg, Mockito.times(1))
                .api(argumentCaptor.capture());
        Request request = argumentCaptor.getValue();
        assertEquals(mail.build(), request.getBody());
        assertEquals(Method.POST, request.getMethod());
        assertEquals("mail/send", request.getEndpoint());
    }
}