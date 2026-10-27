package edu.uclm.reper.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmailApiRequestDTO {
    private Map<String, String> sender;
    private List<Map<String, String>> to;
    private String subject;
    private String htmlContent;

    public EmailApiRequestDTO() {
    }

    public EmailApiRequestDTO(String senderName, String senderEmail, String recipientEmail, String subject, String htmlBody) {
        this.sender = Map.of("name", senderName, "email", senderEmail);
        this.to = new ArrayList<>();
        this.to.add(Map.of("email", recipientEmail));
        this.subject = subject;
        this.htmlContent = htmlBody;
    }

    public Map<String, String> getSender() {
        return sender;
    }

    public void setSender(Map<String, String> sender) {
        this.sender = sender;
    }

    public List<Map<String, String>> getTo() {
        return to;
    }

    public void setTo(List<Map<String, String>> to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }
}
