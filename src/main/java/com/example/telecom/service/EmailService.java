package com.example.telecom.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordEmail(String toEmail, String resetToken) {
        String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText(
                "Bonjour,\n\n" +
                        "Cliquez sur le lien suivant pour réinitialiser votre mot de passe :\n" +
                        resetLink + "\n\n" +
                        "Ce lien expire dans 30 minutes.\n\n" +
                        "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email."
        );

        mailSender.send(message);
    }
}