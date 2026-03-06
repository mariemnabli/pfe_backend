package com.example.telecom.service;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    // Méthode pour envoyer un email de reset password
    public void sendResetPasswordEmail(String to, String token) {
        // Pour l'instant juste un print pour tester
        System.out.println("Envoi email à : " + to);
        System.out.println("Lien reset password : http://localhost:8080/reset?token=" + token);
    }
}
