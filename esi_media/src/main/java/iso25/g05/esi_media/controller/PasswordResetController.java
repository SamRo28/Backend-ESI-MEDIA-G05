package iso25.g05.esi_media.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iso25.g05.esi_media.model.Codigorecuperacion;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.CodigoRecuperacionRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.service.EmailService;
import iso25.g05.esi_media.service.UserService;

@RestController
@RequestMapping("/users/password-reset")
@CrossOrigin(origins = "*")
public class PasswordResetController {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UsuarioRepository usuarioRepository;
    private final CodigoRecuperacionRepository codigoRepo;
    private final EmailService emailService;
    private final UserService userService;

    public PasswordResetController(
        UsuarioRepository usuarioRepository,
        CodigoRecuperacionRepository codigoRepo,
        EmailService emailService,
        UserService userService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.codigoRepo = codigoRepo;
        this.emailService = emailService;
        this.userService = userService;
    }

    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestReset(@RequestBody Map<String, String> body) {
        String email = body != null ? body.getOrDefault("email", "").trim() : "";
        // Mensaje genÃ©rico para no revelar informaciÃ³n sensible
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "Si el correo existe, enviaremos instrucciones para restablecer la contraseÃ±a.");

        if (email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resp);
        }

        try {
            Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                Usuario user = userOpt.get();
                // Reutilizamos Codigorecuperacion como token de restablecimiento (15 min)
                Codigorecuperacion cr = new Codigorecuperacion(user);
                codigoRepo.save(cr);
                // Enviar email con enlace seguro
                emailService.sendPasswordResetEmail(email, cr.getcodigo());
                log.info("Solicitud de restablecimiento enviada para usuario {}", email);
            } else {
                // PolÃ­tica solicitada: avisar si el correo no existe
                log.info("Solicitud de restablecimiento para email no registrado: {}", email);
                Map<String, String> noFound = new HashMap<>();
                noFound.put("message", "No existe una cuenta con ese correo");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(noFound);
            }
        } catch (Exception e) {
            log.warn("Error procesando solicitud de restablecimiento: {}", e.getMessage());
            // No revelamos detalles al cliente
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resp);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestParam("token") String token) {
        Map<String, Object> resp = new HashMap<>();
        Optional<Codigorecuperacion> crOpt = codigoRepo.findByCodigo(token);
        if (crOpt.isEmpty()) {
            resp.put("valid", false);
            return ResponseEntity.status(HttpStatus.OK).body(resp);
        }
        Codigorecuperacion cr = crOpt.get();
        try {
            LocalDateTime exp = LocalDateTime.parse(cr.getfechaexpiracion(), FORMATTER);
            boolean valid = LocalDateTime.now().isBefore(exp);
            resp.put("valid", valid);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("valid", false);
            return ResponseEntity.ok(resp);
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirm(@RequestBody Map<String, String> body) {
        Map<String, String> resp = new HashMap<>();
        String token = body != null ? body.getOrDefault("token", "").trim() : "";
        String newPassword = body != null ? body.getOrDefault("newPassword", "").trim() : "";

        if (token.isEmpty() || newPassword.isEmpty()) {
            resp.put("message", "Solicitud inválida");
            return ResponseEntity.badRequest().body(resp);
        }

        Optional<Codigorecuperacion> crOpt = codigoRepo.findByCodigo(token);
        if (crOpt.isEmpty()) {
            resp.put("message", "El enlace no es válido o ha caducado");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }

        Codigorecuperacion cr = crOpt.get();
        // Comprobar expiración (15 min)
        try {
            LocalDateTime exp = LocalDateTime.parse(cr.getfechaexpiracion(), FORMATTER);
            if (!LocalDateTime.now().isBefore(exp)) {
                codigoRepo.deleteById(cr.getId()); // invalidar token
                resp.put("message", "El enlace no es válido o ha caducado");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }
        } catch (Exception e) {
            resp.put("message", "El enlace no es válido o ha caducado");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }

        try {
            Usuario user = cr.getunnamedUsuario();
            if (user == null || user.getEmail() == null) {
                resp.put("message", "El enlace no es válido o ha caducado");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }
            // Delegar validaciones de contraseÃ±as a la lógica existente
            boolean ok = userService.cambiarContrasenia(user.getEmail(), newPassword);
            if (ok) { codigoRepo.deleteById(cr.getId()); try { emailService.sendPasswordChangedEmail(user.getEmail()); } catch (Exception ex) { System.err.println("[PasswordReset] No se pudo enviar email de confirmación: " + ex.getMessage()); } // invalidar token (uso único)
                resp.put("message", "Contraseña actualizada correctamente");
                return ResponseEntity.ok(resp);
            } else {
                resp.put("message", "No se pudo actualizar la contraseÃ±a");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }
        } catch (RuntimeException ex) {
            // Mensajes genéricos para no filtrar detalles
            log.warn("Error al cambiar contraseña por recuperación: {}", ex.getMessage());
            resp.put("message", "No se pudo establecer la contraseña. Verifique la polí­tica de seguridad.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        } catch (Exception e) {
            log.error("Fallo inesperado en reset de contraseña", e);
            resp.put("message", "No se pudo completar la operación");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
}

