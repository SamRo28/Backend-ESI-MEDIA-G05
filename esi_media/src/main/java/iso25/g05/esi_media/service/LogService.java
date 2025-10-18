package iso25.g05.esi_media.service;


import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio para registrar eventos de auditoría y trazabilidad
 * Cumple con el requisito de auditoría para consultas de perfil
 * según los criterios de aceptación de la historia de usuario.
 */
@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Registra una consulta de perfil para trazabilidad
     * @param adminId ID del administrador que consulta
     * @param adminEmail Email del administrador
     * @param usuarioConsultadoId ID del usuario consultado
     * @param usuarioConsultadoEmail Email del usuario consultado
     * @param tipoUsuario Tipo de usuario consultado (Administrador, Gestor, Visualizador)
     */
    public void registrarConsultaPerfil(String adminId, String adminEmail, 
                                       String usuarioConsultadoId, String usuarioConsultadoEmail,
                                       String tipoUsuario) {
        String timestamp = LocalDateTime.now().format(formatter);
        
        String logMessage = String.format(
            "[AUDITORÍA PERFIL] %s | Admin: %s (%s) consultó perfil de %s: %s (%s)",
            timestamp,
            adminEmail,
            adminId,
            tipoUsuario,
            usuarioConsultadoEmail,
            usuarioConsultadoId
        );
        
    // Por ahora lo registramos en log
    logger.info(logMessage);
    }
    
    /**
     * Registra un intento de acceso no autorizado
     * @param usuarioId ID del usuario que intenta acceder
     * @param recurso Recurso al que intenta acceder
     */
    public void registrarAccesoNoAutorizado(String usuarioId, String recurso) {
        String timestamp = LocalDateTime.now().format(formatter);
        
        String logMessage = String.format(
            "[SEGURIDAD] %s | Acceso NO AUTORIZADO - Usuario: %s intentó acceder a: %s",
            timestamp,
            usuarioId,
            recurso
        );
        
    logger.warn(logMessage);
    }
    
    /**
     * Registra un error en la consulta de perfil
     * @param adminId ID del administrador
     * @param usuarioId ID del usuario que se intentó consultar
     * @param error Mensaje de error
     */
    public void registrarErrorConsulta(String adminId, String usuarioId, String error) {
        String timestamp = LocalDateTime.now().format(formatter);
        
        String logMessage = String.format(
            "[ERROR CONSULTA] %s | Admin: %s intentó consultar usuario: %s - Error: %s",
            timestamp,
            adminId,
            usuarioId,
            error
        );
        
    logger.error(logMessage);
    }
    
    /**
     * Registra acciones generales del sistema
     * @param accion Descripción de la acción
     * @param usuario Usuario que realiza la acción
     */
    public void registrarAccion(String accion, String usuario) {
        String timestamp = LocalDateTime.now().format(formatter);
        
        String logMessage = String.format(
            "[LOG] %s | %s - Usuario: %s",
            timestamp,
            accion,
            usuario
        );
        
    logger.info(logMessage);
    }
    
    /**
     * Registra el bloqueo de un usuario por parte de un administrador
     * @param adminId ID del administrador que bloquea
     * @param adminEmail Email del administrador
     * @param usuarioBloqueadoId ID del usuario bloqueado
     * @param usuarioBloqueadoEmail Email del usuario bloqueado
     */
    public void registrarBloqueoUsuario(String adminId, String adminEmail,
                                       String usuarioBloqueadoId, String usuarioBloqueadoEmail) {
        String timestamp = LocalDateTime.now().format(formatter);
        
        String logMessage = String.format(
            "[AUDITORÍA BLOQUEO] %s | Admin: %s (%s) bloqueó al usuario: %s (%s)",
            timestamp,
            adminEmail,
            adminId,
            usuarioBloqueadoEmail,
            usuarioBloqueadoId
        );
        
    logger.info(logMessage);
    }
    
    /**
     * Registra el desbloqueo de un usuario por parte de un administrador
     * @param adminId ID del administrador que desbloquea
     * @param adminEmail Email del administrador
     * @param usuarioDesbloqueadoId ID del usuario desbloqueado
     * @param usuarioDesbloqueadoEmail Email del usuario desbloqueado
     */
    public void registrarDesbloqueoUsuario(String adminId, String adminEmail,
                                          String usuarioDesbloqueadoId, String usuarioDesbloqueadoEmail) {
        String timestamp = LocalDateTime.now().format(formatter);
        
        String logMessage = String.format(
            "[AUDITORÍA DESBLOQUEO] %s | Admin: %s (%s) desbloqueó al usuario: %s (%s)",
            timestamp,
            adminEmail,
            adminId,
            usuarioDesbloqueadoEmail,
            usuarioDesbloqueadoId
        );
        
    logger.info(logMessage);
    }
}
