package iso25.g05.esi_media.service;

import java.nio.file.Files;
import java.nio.file.Paths;
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
            
            // Cargar la plantilla HTML desde un archivo externo
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
            return new String(Files.readAllBytes(Paths.get(resource.getURI())));
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar la plantilla de email: " + templatePath, e);
        }
    }

}
