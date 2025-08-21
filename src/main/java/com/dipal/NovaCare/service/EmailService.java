package com.dipal.NovaCare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

        @Autowired
        private JavaMailSender mailSender;

        public void sendPasswordResetEmail(String to, String resetLink) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Password Reset Request");
            message.setText("To reset your password, click the link below:\n\n" + resetLink +
                    "\n\nThis link will expire in 1 hour.");
            mailSender.send(message);
        }

    public void sendVideoLink(String to, String sessionId, String token) {
        String videoLink = "https://your-video-app-url/session/" + sessionId + "?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Video Consultation Link");
        message.setText("Please join your video consultation using the following link:\n" + videoLink);
        mailSender.send(message);
    }
}
