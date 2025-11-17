package iso25.g05.esi_media.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import iso25.g05.esi_media.dto.EmailApiRequestDTO;
import iso25.g05.esi_media.model.Codigorecuperacion;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.CodigoRecuperacionRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;

@Service
public class EmailService {

    // Eliminamos JavaMailSender
    // @Autowired
    // private JavaMailSender mailSender;

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private CodigoRecuperacionRepository codigoRecuperacionRepository;

    // Inyectamos valores de configuración
    @Value("${email.api.url}")
    private String apiUrl;

    @Value("${email.api.key}")
    private String apiKey;

    @Value("${email.sender.name}")
    private String senderName;

    @Value("${email.sender.address}")
    private String senderAddress;

    @Value("${app.backend.url}")
    private String backendUrl;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // Cliente HTTP para hacer la petición
    private final RestTemplate restTemplate = new RestTemplate();

    public String generateConfirmationToken() {
        return UUID.randomUUID().toString();
    }

    public String sendActivationEmail(Usuario user) {
        try {
            String token = generateConfirmationToken();
            user.setActivationToken(token);
            user.setHasActivated(false);
            userRepository.save(user);

            String link = backendUrl + "/api/visualizador/activar-web?token=" + token;
            String html = loadEmailTemplate("email-templates/verify-email.html");
            html = html.replace("{{CONFIRM_LINK}}", link)
                       .replace("{{USER_NAME}}", user.getNombre() != null ? user.getNombre() : "");

            // Cambio: Llamada a método HTTP en lugar de SMTP
            sendHttpEmail(user.getEmail(), "Confirmación de Registro en ESIMedia", html);
            
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el correo de verificación vía API", e);
        }
    }

    public Codigorecuperacion send3FAemail(String email, Usuario user) {
        try {
            Codigorecuperacion codigoRecuperacion = new Codigorecuperacion(user);
            codigoRecuperacionRepository.save(codigoRecuperacion);
            
            String emailContent = loadEmailTemplate("email-templates/3FA-email.html");
            emailContent = emailContent.replace("{{CODIGO}}", codigoRecuperacion.getcodigo());

            // Cambio: Llamada a método HTTP
            sendHttpEmail(email, "Confirmación de Inicio de Sesión - Código de Seguridad", emailContent);

            return codigoRecuperacion;

        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el correo de confirmación vía API", e);
        }
    }

    public void sendPasswordResetEmail(String email, String token) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            String html = loadEmailTemplate("email-templates/password-reset.html");
            html = html.replace("{{RESET_LINK}}", resetLink)
                       .replace("{{BRAND}}", "ESIMedia");

            // Cambio: Llamada a método HTTP
            sendHttpEmail(email, "Solicitud de cambio de contraseña en ESIMedia", html);

        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el correo de restablecimiento vía API", e);
        }
    }

    public void sendPasswordChangedEmail(String email) {
        try {
            String html = loadEmailTemplate("email-templates/password-changed.html");
            html = html.replace("{{BRAND}}", "ESIMedia");

            // Cambio: Llamada a método HTTP
            sendHttpEmail(email, "Tu contraseña se ha actualizado correctamente", html);

        } catch (Exception e) {
            System.err.println("[EmailService] Error enviando confirmacion de cambio de contrasena: " + e.getMessage());
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

    /**
     * Método privado para realizar la petición POST a la API de correo externa.
     */
    private void sendHttpEmail(String to, String subject, String htmlBody) {
        // 1. Crear el cuerpo de la petición (DTO)
        EmailApiRequestDTO requestBody = new EmailApiRequestDTO(
            senderName, 
            senderAddress,
            to, 
            subject, 
            htmlBody
        );

        // 2. Configurar cabeceras (api-key y Content-Type)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Brevo/Sendinblue usa 'api-key' en lugar de Authorization Bearer
        headers.set("api-key", apiKey);

        // 3. Crear la entidad HTTP
        HttpEntity<EmailApiRequestDTO> requestEntity = new HttpEntity<>(requestBody, headers);

        // 4. Enviar la petición POST
        // Nota: Esto es síncrono. Si la API tarda, el usuario espera. 
        // Considera usar @Async si necesitas que sea no bloqueante, aunque tu app ya tiene @EnableAsync.
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Fallo al enviar email vía API. Status: " + response.getStatusCode());
        }
    }
}