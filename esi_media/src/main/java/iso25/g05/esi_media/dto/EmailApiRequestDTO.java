package iso25.g05.esi_media.dto;

import java.util.Collections;
import java.util.List;

public class EmailApiRequestDTO {
    private Sender sender;
    private List<Recipient> to;
    private String subject;
    private String htmlContent;

    public EmailApiRequestDTO(String senderName, String senderEmail, String toEmail, String subject, String htmlContent) {
        this.sender = new Sender(senderName, senderEmail);
        this.to = Collections.singletonList(new Recipient(toEmail));
        this.subject = subject;
        this.htmlContent = htmlContent;
    }

    // Getters necesarios para la serialización JSON
    public Sender getSender() { return sender; }
    public List<Recipient> getTo() { return to; }
    public String getSubject() { return subject; }
    public String getHtmlContent() { return htmlContent; }

    public static class Sender {
        private String name;
        private String email;
        public Sender(String name, String email) { this.name = name; this.email = email; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    public static class Recipient {
        private String email;
        public Recipient(String email) { this.email = email; }
        public String getEmail() { return email; }
    }
}