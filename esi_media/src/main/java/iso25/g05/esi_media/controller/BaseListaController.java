package iso25.g05.esi_media.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import iso25.g05.esi_media.service.ListaService;

/**
 * Controlador base común para ListaUsuarioController y ListaGestorController
 * Contiene funcionalidad compartida para evitar duplicación de código.
 * 
 * IMPORTANTE: Esta clase NO modifica la API pública de los controladores,
 * solo factoriza código común para resolver problemas de duplicación reportados por SonarQube.
 */
public abstract class BaseListaController {
    
    // Constantes para respuestas - mantenidas exactamente igual que los controladores originales
    protected static final String SUCCESS = "success";
    protected static final String MENSAJE = "mensaje";
    protected static final String LISTA = "lista";
    protected static final String LISTAS = "listas";
    protected static final String TOTAL = "total";
    protected static final String CONTENIDOS = "contenidos";
    protected static final String TOTAL_CONTENIDOS = "totalContenidos";
    
    // Constantes para mensajes comunes - mantenidas exactamente igual
    protected static final String TOKEN_REQUERIDO = "Token de autorización requerido";
    protected static final String ERROR_INTERNO = "Error interno del servidor";
    
    @Autowired
    protected ListaService listaService;
    
    /**
     * Obtiene el logger específico de cada controlador hijo
     * Cada controlador debe implementar este método para mantener logs diferenciados
     */
    protected abstract Logger getLogger();
    
    /**
     * Extrae el token del header de autorización
     * Método común mantenido exactamente igual que en ambos controladores
     */
    protected String extraerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return authHeader != null ? authHeader.trim() : null;
    }
    
    /**
     * Valida que el token de autorización esté presente
     * Retorna ResponseEntity con error si no hay token, null si está presente
     */
    protected ResponseEntity<Map<String, Object>> validarToken(String authHeader, String operacion) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            getLogger().warn("Intento de {} sin token de autorización", operacion);
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MENSAJE, TOKEN_REQUERIDO);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return null; // Token válido
    }
    
    /**
     * Maneja las excepciones y retorna la respuesta HTTP apropiada
     * Lógica mantenida exactamente igual que en ambos controladores originales
     */
    protected ResponseEntity<Map<String, Object>> manejarExcepcion(RuntimeException e, String mensajeGenerico) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        
        String mensajeError = e.getMessage();
        
        if (mensajeError != null && mensajeError.contains("Token") && 
            (mensajeError.contains("inválido") || mensajeError.contains("expirado") || 
             mensajeError.contains("no proporcionado"))) {
            response.put(MENSAJE, mensajeError);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        if (mensajeError != null && (mensajeError.contains("no encontrada") || mensajeError.contains("no encontrado"))) {
            response.put(MENSAJE, mensajeError);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        if (mensajeError != null && (mensajeError.contains("permisos") || mensajeError.contains("autorizado"))) {
            response.put(MENSAJE, mensajeError);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        // Mantener el comportamiento original: usar mensaje de error específico si existe
        response.put(MENSAJE, mensajeError != null ? mensajeError : mensajeGenerico);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Maneja excepciones generales no capturadas (Exception)
     * Comportamiento mantenido exactamente igual que en controladores originales
     */
    protected ResponseEntity<Map<String, Object>> manejarExcepcionGeneral(Exception e, String operacion) {
        getLogger().error("Error inesperado en {}", operacion, e);
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MENSAJE, ERROR_INTERNO);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Crea una respuesta de éxito estándar para una sola lista
     * Simplifica creación de respuestas manteniendo estructura exacta
     */
    protected ResponseEntity<Map<String, Object>> crearRespuestaExitoLista(String mensaje, Object lista) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put(MENSAJE, mensaje);
        response.put(LISTA, lista);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Crea una respuesta de éxito para múltiples listas con conteo
     * Mantiene estructura exacta de respuestas originales
     */
    protected ResponseEntity<Map<String, Object>> crearRespuestaExitoListas(String mensaje, java.util.List<?> listas) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put(MENSAJE, mensaje);
        response.put(LISTAS, listas);
        response.put(TOTAL, listas.size());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Crea una respuesta de éxito simple (solo mensaje)
     * Para endpoints como DELETE que no retornan datos específicos
     */
    protected ResponseEntity<Map<String, Object>> crearRespuestaExitoSimple(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put(MENSAJE, mensaje);
        return ResponseEntity.ok(response);
    }
}