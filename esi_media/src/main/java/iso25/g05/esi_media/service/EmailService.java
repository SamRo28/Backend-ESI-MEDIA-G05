package iso25.g05.esi_media.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import iso25.g05.esi_media.model.Codigorecuperacion;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.CodigoRecuperacionRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private CodigoRecuperacionRepository codigoRecuperacionRepository;

    public String generateConfirmationToken() {
        return UUID.randomUUID().toString();
    }

    public Codigorecuperacion send3FAemail(String email, Usuario user) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(email);
            helper.setSubject("Confirmación de Registro");

            Codigorecuperacion codigoRecuperacion = new Codigorecuperacion(user);
            codigoRecuperacionRepository.save(codigoRecuperacion);
            
            String emailContent = loadEmailTemplate("email-templates/3FA-email.html");
            emailContent = emailContent.replace("{{CODIGO}}", codigoRecuperacion.getcodigo());

            helper.setText(emailContent, true);

            mailSender.send(mimeMessage);
            return codigoRecuperacion;

        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el correo de confirmación", e);
        }
    }

    private String loadEmailTemplate(String templatePath) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            try (InputStream is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar la plantilla de email: " + templatePath, e);
        }
    }

    public void sendPasswordResetEmail(String email, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(email);
            helper.setSubject("Solicitud de cambio de contraseña en ESIMedia");

            String resetLink = "http://localhost:4200/reset-password?token=" + token;

            String html = loadEmailTemplate("email-templates/password-reset.html");
            html = html.replace("{{RESET_LINK}}", resetLink)
                       .replace("{{BRAND}}", "ESIMedia");

            helper.setText(html, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el correo de restablecimiento", e);
        }
    }

    public void sendPasswordChangedEmail(String email) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(email);
            helper.setSubject("Tu contraseña se ha actualizado correctamente");

            String html = loadEmailTemplate("email-templates/password-changed.html");
            html = html.replace("{{BRAND}}", "ESIMedia");

            helper.setText(html, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("[EmailService] Error enviando confirmación de cambio de contraseña: " + e.getMessage());
        }
    }
}