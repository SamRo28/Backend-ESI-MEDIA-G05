package iso25.g05.esi_media.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Clase base abstracta para controladores de listas.
 * Contiene métodos comunes y constantes compartidas para reducir duplicación.
 */
public abstract class BaseListaController {
    
    // Constantes para respuestas
    protected static final String SUCCESS = "success";
    protected static final String MENSAJE = "mensaje";
    protected static final String LISTA = "lista";
    protected static final String LISTAS = "listas";
    protected static final String TOTAL = "total";
    protected static final String CONTENIDOS = "contenidos";
    protected static final String TOTAL_CONTENIDOS = "totalContenidos";
    
    // Constantes para mensajes comunes
    protected static final String TOKEN_REQUERIDO = "Token de autorización requerido";
    protected static final String ERROR_INTERNO = "Error interno del servidor";
    
    /**
     * Extrae el token del header de autorización
     */
    protected String extraerToken(String authHeader) {
        if (authHeader == null) {
            return null;
        }
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return authHeader.trim();
    }
    
    /**
     * Valida que el token de autorización esté presente
     */
    protected ResponseEntity<Map<String, Object>> validarToken(String authHeader, String accion, Logger logger) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            logger.warn("Intento de {} sin token de autorización", accion);
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MENSAJE, TOKEN_REQUERIDO);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return null;
    }
    
    /**
     * Crea una respuesta de éxito estándar
     */
    protected Map<String, Object> crearRespuestaExito(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put(MENSAJE, mensaje);
        return response;
    }
    
    /**
     * Crea una respuesta de error estándar
     */
    protected Map<String, Object> crearRespuestaError(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MENSAJE, mensaje);
        return response;
    }
    
    /**
     * Maneja las excepciones y retorna la respuesta HTTP apropiada
     */
    protected ResponseEntity<Map<String, Object>> manejarExcepcion(RuntimeException e, String mensajeGenerico) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        
        String mensajeError = e.getMessage();
        
        if (esErrorAutorizacion(mensajeError)) {
            response.put(MENSAJE, mensajeError);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        if (esErrorNoEncontrado(mensajeError)) {
            response.put(MENSAJE, mensajeError);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        if (esErrorPermisos(mensajeError)) {
            response.put(MENSAJE, mensajeError);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        response.put(MENSAJE, mensajeError != null ? mensajeError : mensajeGenerico);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Maneja excepciones genéricas no esperadas
     */
    protected ResponseEntity<Map<String, Object>> manejarExcepcionGenerica(Exception e, Logger logger) {
        logger.error("Error inesperado", e);
        Map<String, Object> response = crearRespuestaError(ERROR_INTERNO);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Verifica si el error es de autorización
     */
    private boolean esErrorAutorizacion(String mensajeError) {
        return mensajeError != null && 
               mensajeError.contains("Token") && 
               (mensajeError.contains("inválido") || 
                mensajeError.contains("expirado") || 
                mensajeError.contains("no proporcionado"));
    }
    
    /**
     * Verifica si el error es de recurso no encontrado
     */
    private boolean esErrorNoEncontrado(String mensajeError) {
        return mensajeError != null && 
               (mensajeError.contains("no encontrada") || 
                mensajeError.contains("no encontrado"));
    }
    
    /**
     * Verifica si el error es de permisos
     */
    private boolean esErrorPermisos(String mensajeError) {
        return mensajeError != null && 
               (mensajeError.contains("permisos") || 
                mensajeError.contains("autorizado"));
    }
    
    /**
     * Ejecuta una operación con manejo de excepciones estándar
     */
    protected <T> ResponseEntity<Map<String, Object>> ejecutarOperacion(
            String authHeader,
            String accion,
            Logger logger,
            OperacionConToken<T> operacion,
            String mensajeExito) {
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, accion, logger);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            operacion.ejecutar(token);
            
            Map<String, Object> response = crearRespuestaExito(mensajeExito);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error en {}: {}", accion, e.getMessage());
            return manejarExcepcion(e, "Error en " + accion);
        } catch (Exception e) {
            return manejarExcepcionGenerica(e, logger);
        }
    }
    
    /**
     * Interfaz funcional para operaciones que requieren token
     */
    @FunctionalInterface
    protected interface OperacionConToken<T> {
        T ejecutar(String token);
    }
}
