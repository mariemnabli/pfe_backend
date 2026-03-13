package com.example.telecom.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // ── Email générique HTML ───────────────────────────────────
    public void envoyerEmail(String destinataire, String sujet, String contenuHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(contenuHtml, true); // true = HTML
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email à " + destinataire + " : " + e.getMessage());
        }
    }

    // ── Reset password ─────────────────────────────────────────
    public void envoyerResetPassword(String destinataire, String resetToken) {
        String lien = "http://localhost:5173/reset-password?token=" + resetToken;
        String sujet = "Réinitialisation de votre mot de passe";
        String contenu = """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 30px;">
                  <div style="max-width: 600px; margin: auto; background: #fff;
                              border-radius: 8px; padding: 40px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #1a73e8;">Réinitialisation du mot de passe</h2>
                    <p>Bonjour,</p>
                    <p>Vous avez demandé la réinitialisation de votre mot de passe.</p>
                    <p>Cliquez sur le bouton ci-dessous pour définir un nouveau mot de passe :</p>
                    <div style="text-align: center; margin: 30px 0;">
                      <a href="%s"
                         style="background-color: #1a73e8; color: white; padding: 14px 28px;
                                text-decoration: none; border-radius: 5px; font-size: 16px;">
                        Réinitialiser mon mot de passe
                      </a>
                    </div>
                    <p style="color: #888; font-size: 13px;">
                      Ce lien est valable pendant <strong>30 minutes</strong>.<br>
                      Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    <p style="color: #aaa; font-size: 12px; text-align: center;">
                      Portail Télécom — Ne pas répondre à cet email
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(lien);

        envoyerEmail(destinataire, sujet, contenu);
    }

    // ── Confirmation d'inscription ─────────────────────────────
    public void envoyerConfirmationInscription(String destinataire, String username) {
        String sujet = "Bienvenue sur le Portail Télécom";
        String contenu = """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 30px;">
                  <div style="max-width: 600px; margin: auto; background: #fff;
                              border-radius: 8px; padding: 40px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #1a73e8;">Compte créé avec succès ✅</h2>
                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>Votre compte a été créé sur le <strong>Portail de Gestion Télécom</strong>.</p>
                    <p>Vous pouvez maintenant vous connecter avec vos identifiants.</p>
                    <div style="text-align: center; margin: 30px 0;">
                      <a href="http://localhost:5173/login"
                         style="background-color: #1a73e8; color: white; padding: 14px 28px;
                                text-decoration: none; border-radius: 5px; font-size: 16px;">
                        Se connecter
                      </a>
                    </div>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    <p style="color: #aaa; font-size: 12px; text-align: center;">
                      Portail Télécom — Ne pas répondre à cet email
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(username);

        envoyerEmail(destinataire, sujet, contenu);
    }

    // ── Notification activation/désactivation compte ───────────
    public void envoyerNotificationCompte(String destinataire, String username, boolean actif) {
        String statut  = actif ? "activé"    : "désactivé";
        String couleur = actif ? "#2e7d32"   : "#c62828";
        String emoji   = actif ? "✅"         : "🚫";
        String sujet   = "Votre compte a été " + statut;
        String contenu = """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 30px;">
                  <div style="max-width: 600px; margin: auto; background: #fff;
                              border-radius: 8px; padding: 40px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: %s;">Compte %s %s</h2>
                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>Votre compte sur le Portail Télécom a été <strong>%s</strong>
                       par un administrateur.</p>
                    %s
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    <p style="color: #aaa; font-size: 12px; text-align: center;">
                      Portail Télécom — Ne pas répondre à cet email
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(
                couleur, statut, emoji, username, statut,
                actif
                        ? "<p>Vous pouvez maintenant vous connecter normalement.</p>"
                        : "<p>Veuillez contacter votre administrateur pour plus d'informations.</p>"
        );

        envoyerEmail(destinataire, sujet, contenu);
    }

    // ── Notification promotion souscrite ──────────────────────
    public void envoyerNotificationPromotion(String destinataire,
                                             String nomClient,
                                             String nomPromotion,
                                             String reduction) {
        String sujet = "Promotion activée sur votre contrat";
        String contenu = """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 30px;">
                  <div style="max-width: 600px; margin: auto; background: #fff;
                              border-radius: 8px; padding: 40px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #1a73e8;">Nouvelle promotion activée 🎁</h2>
                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>La promotion <strong>%s</strong> a été appliquée à votre contrat.</p>
                    <div style="background: #e8f5e9; border-left: 4px solid #2e7d32;
                                padding: 15px; border-radius: 4px; margin: 20px 0;">
                      <strong>Réduction accordée : %s</strong>
                    </div>
                    <p>Pour toute question, contactez votre conseiller commercial.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    <p style="color: #aaa; font-size: 12px; text-align: center;">
                      Portail Télécom — Ne pas répondre à cet email
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(nomClient, nomPromotion, reduction);

        envoyerEmail(destinataire, sujet, contenu);
    }
}