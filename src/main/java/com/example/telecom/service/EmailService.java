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
            helper.setText(contenuHtml, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur envoi email à " + destinataire + " : " + e.getMessage());
        }
    }

    // ── Envoi credentials lors de la création du compte ───────
    public void envoyerCredentials(String destinataire, String username, String motDePasseBrut) {
        String sujet  = "Vos identifiants de connexion — Portail Télécom";
        String contenu = """
            <html>
            <body style="font-family:Arial,sans-serif;background:#f4f4f4;padding:30px;">
              <div style="max-width:600px;margin:auto;background:#fff;
                          border-radius:8px;padding:40px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                <h2 style="color:#1a73e8;">Bienvenue sur le Portail Télécom 👋</h2>
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Votre compte a été créé par l'administrateur. Voici vos identifiants :</p>
                <div style="background:#f0f4ff;border-left:4px solid #1a73e8;
                            padding:20px;border-radius:4px;margin:20px 0;">
                  <p style="margin:5px 0;"><strong>📧 Email :</strong> %s</p>
                  <p style="margin:5px 0;"><strong>🔑 Mot de passe :</strong>
                    <span style="font-size:18px;font-weight:bold;color:#1a73e8;
                                 letter-spacing:2px;">%s</span>
                  </p>
                </div>
                <p style="color:#e53935;font-weight:bold;">
                  ⚠️ Vous devez changer votre mot de passe lors de votre première connexion.
                </p>
                <div style="text-align:center;margin:30px 0;">
                  <a href="http://localhost:5173/login"
                     style="background:#1a73e8;color:white;padding:14px 28px;
                            text-decoration:none;border-radius:5px;font-size:16px;">
                    Se connecter
                  </a>
                </div>
                <hr style="border:none;border-top:1px solid #eee;margin:30px 0;">
                <p style="color:#aaa;font-size:12px;text-align:center;">
                  Portail Télécom — Ne pas répondre à cet email
                </p>
              </div>
            </body>
            </html>
            """.formatted(username, destinataire, motDePasseBrut);

        envoyerEmail(destinataire, sujet, contenu);
    }

    // ── Notification première connexion ───────────────────────
    public void envoyerNotificationPremiereConnexion(String destinataire, String username) {
        String sujet  = "Première connexion détectée — Portail Télécom";
        String contenu = """
            <html>
            <body style="font-family:Arial,sans-serif;background:#f4f4f4;padding:30px;">
              <div style="max-width:600px;margin:auto;background:#fff;
                          border-radius:8px;padding:40px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                <h2 style="color:#2e7d32;">Première connexion réussie ✅</h2>
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Nous avons détecté votre <strong>première connexion</strong>
                   au Portail Télécom.</p>
                <div style="background:#e8f5e9;border-left:4px solid #2e7d32;
                            padding:15px;border-radius:4px;margin:20px 0;">
                  <p style="margin:0;">
                    🔐 Pour votre sécurité, nous vous recommandons de
                    <strong>changer votre mot de passe</strong> dès maintenant.
                  </p>
                </div>
                <div style="text-align:center;margin:30px 0;">
                  <a href="http://localhost:5173/change-password"
                     style="background:#2e7d32;color:white;padding:14px 28px;
                            text-decoration:none;border-radius:5px;font-size:16px;">
                    Changer mon mot de passe
                  </a>
                </div>
                <p style="color:#888;font-size:13px;">
                  Si ce n'était pas vous, contactez immédiatement votre administrateur DSI.
                </p>
                <hr style="border:none;border-top:1px solid #eee;margin:30px 0;">
                <p style="color:#aaa;font-size:12px;text-align:center;">
                  Portail Télécom — Ne pas répondre à cet email
                </p>
              </div>
            </body>
            </html>
            """.formatted(username);

        envoyerEmail(destinataire, sujet, contenu);
    }

    // ── Reset password ─────────────────────────────────────────
    public void envoyerResetPassword(String destinataire, String resetToken) {
        String lien   = "http://localhost:5173/reset-password?token=" + resetToken;
        String sujet  = "Réinitialisation de votre mot de passe";
        String contenu = """
            <html>
            <body style="font-family:Arial,sans-serif;background:#f4f4f4;padding:30px;">
              <div style="max-width:600px;margin:auto;background:#fff;
                          border-radius:8px;padding:40px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                <h2 style="color:#1a73e8;">Réinitialisation du mot de passe</h2>
                <p>Cliquez sur le bouton ci-dessous pour définir un nouveau mot de passe :</p>
                <div style="text-align:center;margin:30px 0;">
                  <a href="%s"
                     style="background:#1a73e8;color:white;padding:14px 28px;
                            text-decoration:none;border-radius:5px;font-size:16px;">
                    Réinitialiser mon mot de passe
                  </a>
                </div>
                <p style="color:#888;font-size:13px;">
                  Ce lien est valable <strong>30 minutes</strong>.<br>
                  Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.
                </p>
                <hr style="border:none;border-top:1px solid #eee;margin:30px 0;">
                <p style="color:#aaa;font-size:12px;text-align:center;">
                  Portail Télécom — Ne pas répondre à cet email
                </p>
              </div>
            </body>
            </html>
            """.formatted(lien);

        envoyerEmail(destinataire, sujet, contenu);
    }

    // ── Activation / Désactivation compte ─────────────────────
    public void envoyerNotificationCompte(String destinataire, String username, boolean actif) {
        String statut  = actif ? "activé"  : "désactivé";
        String couleur = actif ? "#2e7d32" : "#c62828";
        String emoji   = actif ? "✅"       : "🚫";
        String sujet   = "Votre compte a été " + statut;
        String contenu = """
            <html>
            <body style="font-family:Arial,sans-serif;background:#f4f4f4;padding:30px;">
              <div style="max-width:600px;margin:auto;background:#fff;
                          border-radius:8px;padding:40px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                <h2 style="color:%s;">Compte %s %s</h2>
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Votre compte a été <strong>%s</strong> par un administrateur.</p>
                %s
                <hr style="border:none;border-top:1px solid #eee;margin:30px 0;">
                <p style="color:#aaa;font-size:12px;text-align:center;">
                  Portail Télécom — Ne pas répondre à cet email
                </p>
              </div>
            </body>
            </html>
            """.formatted(
                couleur, statut, emoji, username, statut,
                actif
                        ? "<p>Vous pouvez maintenant vous connecter normalement.</p>"
                        : "<p>Contactez votre administrateur pour plus d'informations.</p>"
        );

        envoyerEmail(destinataire, sujet, contenu);
    }

    // ── Notification promotion souscrite ──────────────────────
    public void envoyerNotificationPromotion(String destinataire, String nomClient,
                                             String nomPromotion, String reduction) {
        String sujet  = "Promotion activée sur votre contrat 🎁";
        String contenu = """
            <html>
            <body style="font-family:Arial,sans-serif;background:#f4f4f4;padding:30px;">
              <div style="max-width:600px;margin:auto;background:#fff;
                          border-radius:8px;padding:40px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                <h2 style="color:#1a73e8;">Nouvelle promotion activée 🎁</h2>
                <p>Bonjour <strong>%s</strong>,</p>
                <p>La promotion <strong>%s</strong> a été appliquée à votre contrat.</p>
                <div style="background:#e8f5e9;border-left:4px solid #2e7d32;
                            padding:15px;border-radius:4px;margin:20px 0;">
                  <strong>Réduction accordée : %s</strong>
                </div>
                <hr style="border:none;border-top:1px solid #eee;margin:30px 0;">
                <p style="color:#aaa;font-size:12px;text-align:center;">
                  Portail Télécom — Ne pas répondre à cet email
                </p>
              </div>
            </body>
            </html>
            """.formatted(nomClient, nomPromotion, reduction);

        envoyerEmail(destinataire, sujet, contenu);
    }
}