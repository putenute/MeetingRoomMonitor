package com.rewe.digital.calendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

@Component
public class NotificationService {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "calendar-reader";

    public void notifyOrganizerClean(final Meeting lastEventInRoom, final Credential cred,
            final HttpTransport googleNetHttpTransport) throws Exception {
        final MimeMessage email =
                createEmail("tim.dauer@rewe-digital.com", "meetingroommonitor@appspot.gserviceaccount.com",
                        ":) Der Raum deines Meetings '" + (lastEventInRoom != null ? ("'" + lastEventInRoom.getTitle()
                                + "'") : "") + " wurde als sauber bezeichnet!",
                        "Bitte achte das nächste mal auf den Zustand des Meetingraums - danke! Dein " +
                                "Super - weiter so! Dein MeetingRoomMonitorService");

        sendMessage(getGmailService(cred, googleNetHttpTransport), "me", email);
        System.out.println("Clean-Message gesendet!");
    }

    public void notifyOrganizerDirty(final Meeting lastEventInRoom, final Credential cred,
            final HttpTransport googleNetHttpTransport) throws Exception {
        final MimeMessage email = createEmail("tim.dauer@rewe-digital.com", "meetingroommonitor@appspot" +
                        ".gserviceaccount.com",
                ":/ Der Raum deines Meetings '" + (lastEventInRoom != null ? ("'" + lastEventInRoom.getTitle()
                        + "'") : "") + " wurde als schmutzig oder unaufgeräumt bewertet!",
                "Bitte achte das nächste mal auf den Zustand des Meetingraums - danke! Dein " +
                        "MeetingRoomMonitorService");

        sendMessage(getGmailService(cred, googleNetHttpTransport), "me", email);
        System.out.println("Dirty-Message gesendet!");
    }

    private MimeMessage createBaseMessage() {
        final Properties props = new Properties();
        final Session session = Session.getDefaultInstance(props, null);
        final MimeMessage email = new MimeMessage(session);

        try {
            email.setFrom(new InternetAddress("meetingroommonitor@appspot.gserviceaccount.com"));
        } catch (final javax.mail.MessagingException e) {
            e.printStackTrace();
        }
        try {
            email.addRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress("tim.dauer@rewe-digital.com"));
        } catch (final javax.mail.MessagingException e) {
            e.printStackTrace();
        }

        return email;
    }

    /**
     * Build and return an authorized Gmail client service.
     *
     * @return an authorized Gmail client service
     */
    public Gmail getGmailService(final Credential credential, final HttpTransport googleNetHttpTransport)
            throws IOException {

        return new Gmail.Builder(googleNetHttpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Send an email from the user's mailbox to its recipient.
     *
     * @param service
     *         Authorized Gmail API instance.
     * @param userId
     *         User's email address. The special value "me" can be used to indicate the authenticated user.
     * @param emailContent
     *         Email to be sent.
     *
     * @return The sent message
     */
    private static Message sendMessage(final Gmail service,
            final String userId,
            final MimeMessage emailContent)
            throws MessagingException, IOException {
        Message message = createMessageWithEmail(emailContent);
        message = service.users().messages().send(userId, message).execute();

        return message;
    }


    /**
     * Create a message from an email.
     *
     * @param emailContent
     *         Email to be set to raw of message
     *
     * @return a message containing a base64url encoded email
     */
    public static Message createMessageWithEmail(final MimeMessage emailContent)
            throws MessagingException, IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            emailContent.writeTo(buffer);
        } catch (final javax.mail.MessagingException e) {
            e.printStackTrace();
        }
        final byte[] bytes = buffer.toByteArray();
        final String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        final Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to
     *         email address of the receiver
     * @param from
     *         email address of the sender, the mailbox account
     * @param subject
     *         subject of the email
     * @param bodyText
     *         body text of the email
     *
     * @return the MimeMessage to be used to send email
     */
    public static MimeMessage createEmail(final String to,
            final String from,
            final String subject,
            final String bodyText)
            throws MessagingException {
        final Properties props = new Properties();
        final Session session = Session.getDefaultInstance(props, null);

        final MimeMessage email = new MimeMessage(session);

        try {
            email.setFrom(new InternetAddress(from));
            email.addRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress(to));
            email.setSubject(subject);
            email.setText(bodyText);
        } catch (final javax.mail.MessagingException e) {
            e.printStackTrace();
        }

        return email;
    }

    public void postTweet(final String text) {
        // The factory instance is re-useable and thread safe.
        final Twitter twitter = TwitterFactory.getSingleton();
        Status status = null;
        try {
            status = twitter.updateStatus(text);
            System.out.println("Successfully updated the status to [" + status.getText() + "].");
        } catch (final TwitterException e) {
            e.printStackTrace();
        }
    }
}
