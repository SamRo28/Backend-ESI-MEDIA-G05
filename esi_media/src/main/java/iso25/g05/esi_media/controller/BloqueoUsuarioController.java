package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.service.LogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para bloqueo y desbloqueo de usuarios
 * Historia de Usuario: Como administrador, quiero poder bloquear/desbloquear usuarios
 * 
 * Permite a los administradores bloquear usuarios para impedir su acceso al sistema
 * y desbloquearlos para restaurar su acceso. Todas las acciones quedan registradas.
 */
@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class BloqueoUsuarioController {
    
    private final UsuarioRepository usuarioRepository;
    private final LogService logService;
    
    public BloqueoUsuarioController(UsuarioRepository usuarioRepository, LogService logService) {
        this.usuarioRepository = usuarioRepository;
        this.logService = logService;
    }
    
    /**
     * Bloquea un usuario impidiendo su acceso al sistema
     * Solo accesible por administradores autenticados
     * 
     * @param usuarioId ID del usuario a bloquear
     * @param adminId ID del administrador que realiza la acción (header)
     * @return Respuesta con mensaje de éxito o error
     */
    @PutMapping("/{usuarioId}/bloquear")
    public ResponseEntity<Map<String, String>> bloquearUsuario(
            @PathVariable String usuarioId,
            @RequestHeader(value = "Admin-ID", required = false) String adminId) {
        
        try {
            // Validar autenticación del administrador
            if (adminId == null || adminId.isEmpty()) {
                return crearRespuestaError(
                    HttpStatus.UNAUTHORIZED,
                    "Se requiere autenticación de administrador"
                );
            }
            
            // Verificar que el administrador existe
            Optional<Usuario> adminOpt = usuarioRepository.findById(adminId);
            if (adminOpt.isEmpty() || !(adminOpt.get() instanceof Administrador)) {
                return crearRespuestaError(
                    HttpStatus.FORBIDDEN,
                    "Solo administradores pueden bloquear usuarios"
                );
            }
            
            Administrador admin = (Administrador) adminOpt.get();
            
            // Buscar el usuario a bloquear
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                return crearRespuestaError(
                    HttpStatus.NOT_FOUND,
                    "Usuario no encontrado"
                );
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar si ya está bloqueado
            if (usuario.isBloqueado()) {
                return crearRespuestaError(
                    HttpStatus.BAD_REQUEST,
                    "El usuario ya está bloqueado"
                );
            }
            
            // Bloquear usuario
            usuario.setBloqueado(true);
            usuarioRepository.save(usuario);
            
            // Registrar auditoría
            logService.registrarBloqueoUsuario(
                adminId,
                admin.getEmail(),
                usuarioId,
                usuario.getEmail()
            );
            
            return crearRespuestaExito("Usuario bloqueado correctamente");
            
        } catch (Exception e) {
            return crearRespuestaError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al bloquear usuario: " + e.getMessage()
            );
        }
    }
    
    /**
     * Desbloquea un usuario restaurando su acceso al sistema
     * Solo accesible por administradores autenticados
     * 
     * @param usuarioId ID del usuario a desbloquear
     * @param adminId ID del administrador que realiza la acción (header)
     * @return Respuesta con mensaje de éxito o error
     */
    @PutMapping("/{usuarioId}/desbloquear")
    public ResponseEntity<Map<String, String>> desbloquearUsuario(
            @PathVariable String usuarioId,
            @RequestHeader(value = "Admin-ID", required = false) String adminId) {
        
        try {
            // Validar autenticación del administrador
            if (adminId == null || adminId.isEmpty()) {
                return crearRespuestaError(
                    HttpStatus.UNAUTHORIZED,
                    "Se requiere autenticación de administrador"
                );
            }
            
            // Verificar que el administrador existe
            Optional<Usuario> adminOpt = usuarioRepository.findById(adminId);
            if (adminOpt.isEmpty() || !(adminOpt.get() instanceof Administrador)) {
                return crearRespuestaError(
                    HttpStatus.FORBIDDEN,
                    "Solo administradores pueden desbloquear usuarios"
                );
            }
            
            Administrador admin = (Administrador) adminOpt.get();
            
            // Buscar el usuario a desbloquear
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                return crearRespuestaError(
                    HttpStatus.NOT_FOUND,
                    "Usuario no encontrado"
                );
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar si está bloqueado
            if (!usuario.isBloqueado()) {
                return crearRespuestaError(
                    HttpStatus.BAD_REQUEST,
                    "El usuario no está bloqueado"
                );
            }
            
            // Desbloquear usuario
            usuario.setBloqueado(false);
            usuarioRepository.save(usuario);
            
            // Registrar auditoría
            logService.registrarDesbloqueoUsuario(
                adminId,
                admin.getEmail(),
                usuarioId,
                usuario.getEmail()
            );
            
            return crearRespuestaExito("Usuario desbloqueado correctamente");
            
        } catch (Exception e) {
            return crearRespuestaError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al desbloquear usuario: " + e.getMessage()
            );
        }
    }
    
    /**
     * Crea una respuesta de éxito
     */
    private ResponseEntity<Map<String, String>> crearRespuestaExito(String mensaje) {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", mensaje);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Crea una respuesta de error
     */
    private ResponseEntity<Map<String, String>> crearRespuestaError(HttpStatus status, String error) {
        Map<String, String> response = new HashMap<>();
        response.put("error", error);
        return ResponseEntity.status(status).body(response);
    }
}
