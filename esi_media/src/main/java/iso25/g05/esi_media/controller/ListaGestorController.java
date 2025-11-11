package iso25.g05.esi_media.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iso25.g05.esi_media.dto.PlaylistDto;

import iso25.g05.esi_media.service.ListaService;

/**
 * Controlador REST específico para gestores en gestión de listas
 * 
 * FUNCIONALIDAD:
 * - Los gestores pueden crear listas VISIBLES (públicas) o NO VISIBLES (privadas)
 * - Las listas VISIBLES tienen nombres únicos globalmente
 * - Solo el creador original puede eliminar completamente una lista
 * - Se pueden gestionar audios y videos mezclados en las listas
 * - Sin límite de tamaño, pero al menos 1 contenido obligatorio
 * 
 * RESTRICCIONES IMPLEMENTADAS:
 * - Nombre y descripción obligatorios
 * - Al menos 1 contenido (no permitir borrar el último)
 * - Sin elementos repetidos (se eliminan duplicados automáticamente)
 * - Nombres únicos para listas VISIBLES
 * - Solo el creador puede eliminar la lista completa
 * 
 * RUTAS:
 * - POST /listas/gestor - Crear nueva lista (visible/no visible)
 * - GET /listas/gestor/mias - Obtener listas propias del gestor
 * - GET /listas/gestor/todas - Obtener todas las listas visibles de gestores
 * - PUT /listas/gestor/{id} - Editar lista propia
 * - DELETE /listas/gestor/{id} - Eliminar lista propia (solo creador)
 * - POST /listas/gestor/{id}/contenidos/{contenidoId} - Añadir contenido a lista
 * - DELETE /listas/gestor/{id}/contenidos/{contenidoId} - Eliminar contenido de lista
 */
@RestController
@RequestMapping("/listas/gestor")
@CrossOrigin(origins = "*")
public class ListaGestorController {
    
    private static final Logger logger = LoggerFactory.getLogger(ListaGestorController.class);
    
    // Constantes para respuestas
    private static final String SUCCESS = "success";
    private static final String MENSAJE = "mensaje";
    private static final String LISTA = "lista";
    private static final String LISTAS = "listas";
    private static final String TOTAL = "total";
    private static final String CONTENIDOS = "contenidos";
    private static final String TOTAL_CONTENIDOS = "totalContenidos";
    
    // Constantes para mensajes comunes
    private static final String TOKEN_REQUERIDO = "Token de autorización requerido";
    private static final String ERROR_INTERNO = "Error interno del servidor";
    
    @Autowired
    private ListaService listaService;
    
    /**
     * Crea una nueva lista para el gestor autenticado
     * POST /listas/gestor
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearLista(
            @RequestBody PlaylistDto listaDto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de crear lista de gestor sin token de autorización");
                response.put(SUCCESS, false);
                response.put(MENSAJE, TOKEN_REQUERIDO);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String token = extraerToken(authHeader);
            PlaylistDto listaCreada = listaService.crearListaDesdeDto(listaDto, token);
            
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Lista creada correctamente y asociada al usuario.");
            response.put(LISTA, listaCreada);
            
            logger.info("Lista de gestor creada exitosamente: {} (ID: {})", listaCreada.getNombre(), listaCreada.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al crear lista de gestor: {}", e.getMessage());
            return manejarExcepcion(e, "Error al crear la lista");
        } catch (Exception e) {
            logger.error("Error inesperado al crear lista de gestor", e);
            response.put(SUCCESS, false);
            response.put(MENSAJE, ERROR_INTERNO);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtiene las listas propias del gestor autenticado
     * GET /listas/gestor/mias
     */
    @GetMapping("/mias")
    public ResponseEntity<Map<String, Object>> obtenerListasPropiasGestor(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de obtener listas propias de gestor sin token de autorización");
                response.put(SUCCESS, false);
                response.put(MENSAJE, TOKEN_REQUERIDO);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String token = extraerToken(authHeader);
            List<PlaylistDto> listas = listaService.findListasPropias(token);
            
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Listas propias obtenidas exitosamente");
            response.put(LISTAS, listas);
            response.put(TOTAL, listas.size());
            
            logger.info("Listas propias de gestor obtenidas exitosamente: {} lista(s)", listas.size());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener listas propias de gestor: {}", e.getMessage());
            return manejarExcepcion(e, "Error al obtener las listas propias");
        } catch (Exception e) {
            logger.error("Error inesperado al obtener listas propias de gestor", e);
            response.put(SUCCESS, false);
            response.put(MENSAJE, ERROR_INTERNO);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtiene todas las listas disponibles para gestores (públicas + de otros gestores)
     * GET /listas/gestor/todas
     */
    @GetMapping("/todas")
    public ResponseEntity<Map<String, Object>> obtenerTodasLasListas(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de obtener todas las listas sin token de gestor");
                response.put(SUCCESS, false);
                response.put(MENSAJE, TOKEN_REQUERIDO);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String token = extraerToken(authHeader);
            
            // Obtener listas visibles de gestores (propias + de otros gestores)
            List<PlaylistDto> listas = listaService.findListasVisiblesGestores(token);
            
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Todas las listas visibles obtenidas exitosamente");
            response.put(LISTAS, listas);
            response.put(TOTAL, listas.size());
            
            logger.info("Todas las listas obtenidas por gestor exitosamente: {} lista(s)", listas.size());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener todas las listas para gestor: {}", e.getMessage());
            return manejarExcepcion(e, "Error al obtener todas las listas");
        } catch (Exception e) {
            logger.error("Error inesperado al obtener todas las listas para gestor", e);
            response.put(SUCCESS, false);
            response.put(MENSAJE, ERROR_INTERNO);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Obtiene una lista específica por ID del gestor
     * GET /listas/gestor/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerListaPorId(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de obtener lista {} sin token de autorización", id);
                response.put(SUCCESS, false);
                response.put(MENSAJE, TOKEN_REQUERIDO);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String token = extraerToken(authHeader);
            PlaylistDto lista = listaService.findListaById(id, token);
            
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Lista obtenida exitosamente");
            response.put(LISTA, lista);
            
            logger.info("Lista de gestor obtenida exitosamente: {} (ID: {})", lista.getNombre(), lista.getId());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener lista de gestor {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al obtener la lista");
        } catch (Exception e) {
            logger.error("Error inesperado al obtener lista de gestor {}", id, e);
            response.put(SUCCESS, false);
            response.put(MENSAJE, ERROR_INTERNO);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Obtiene los contenidos de una lista específica del gestor
     * GET /listas/gestor/{id}/contenidos
     */
    @GetMapping("/{id}/contenidos")
    public ResponseEntity<Map<String, Object>> obtenerContenidosLista(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de obtener contenidos de lista {} sin token de autorización", id);
                response.put(SUCCESS, false);
                response.put(MENSAJE, TOKEN_REQUERIDO);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String token = extraerToken(authHeader);
            List<iso25.g05.esi_media.dto.ContenidoResumenDTO> contenidos = listaService.findContenidosLista(id, token);
            
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Contenidos obtenidos exitosamente");
            response.put(CONTENIDOS, contenidos);
            response.put(TOTAL, contenidos.size());
            
            logger.info("Contenidos de lista {} obtenidos exitosamente: {} contenido(s)", id, contenidos.size());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener contenidos de lista {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al obtener los contenidos de la lista");
        } catch (Exception e) {
            logger.error("Error inesperado al obtener contenidos de lista {}", id, e);
            response.put(SUCCESS, false);
            response.put(MENSAJE, ERROR_INTERNO);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Edita una lista existente del gestor
     * PUT /listas/gestor/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> editarLista(
            @PathVariable String id,
            @RequestBody PlaylistDto updatedListaDto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de editar lista de gestor {} sin token de autorización", id);
                response.put(SUCCESS, false);
                response.put(MENSAJE, TOKEN_REQUERIDO);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String token = extraerToken(authHeader);
            
            // Usar el nuevo método que maneja DTOs completos incluyendo contenidosIds
            PlaylistDto listaEditada = listaService.updateListaDesdeDto(id, updatedListaDto, token);
            
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Lista actualizada exitosamente");
            response.put(LISTA, listaEditada);
            
            logger.info("Lista de gestor editada exitosamente: {} (ID: {})", listaEditada.getNombre(), listaEditada.getId());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al editar lista de gestor {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al editar la lista");
        } catch (Exception e) {
            logger.error("Error inesperado al editar lista de gestor {}", id, e);
            response.put(SUCCESS, false);
            response.put(MENSAJE, ERROR_INTERNO);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Elimina una lista del gestor
     * DELETE /listas/gestor/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarLista(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de eliminar lista de gestor {} sin token de autorización", id);
                response.put(SUCCESS, false);
                response.put(MENSAJE, TOKEN_REQUERIDO);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String token = extraerToken(authHeader);
            listaService.deleteLista(id, token);
            
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Lista eliminada exitosamente");
            
            logger.info("Lista de gestor eliminada exitosamente: ID {}", id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al eliminar lista de gestor {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al eliminar la lista");
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar lista de gestor {}", id, e);
            response.put(SUCCESS, false);
            response.put(MENSAJE, ERROR_INTERNO);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Añade un contenido a una lista del gestor
     * POST /listas/gestor/{id}/contenidos/{contenidoId}
     */
    @PostMapping("/{id}/contenidos/{contenidoId}")
    public ResponseEntity<Map<String, Object>> agregarContenido(
            @PathVariable String id,
            @PathVariable String contenidoId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de añadir contenido a lista de gestor {} sin token de autorización", id);
                response.put(SUCCESS, false);
                response.put(MENSAJE, TOKEN_REQUERIDO);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String token = extraerToken(authHeader);
            PlaylistDto listaActualizada = listaService.addContenido(id, contenidoId, token);
            
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Contenido añadido a la lista exitosamente");
            response.put(LISTA, listaActualizada);
            response.put(TOTAL_CONTENIDOS, listaActualizada.getContenidosIds().size());
            
            logger.info("Contenido {} añadido a lista de gestor {} exitosamente", contenidoId, id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al añadir contenido a lista de gestor {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al añadir contenido a la lista");
        } catch (Exception e) {
            logger.error("Error inesperado al añadir contenido a lista de gestor {}", id, e);
            response.put(SUCCESS, false);
            response.put(MENSAJE, ERROR_INTERNO);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Elimina un contenido de una lista del gestor
     * DELETE /listas/gestor/{id}/contenidos/{contenidoId}
     */
    @DeleteMapping("/{id}/contenidos/{contenidoId}")
    public ResponseEntity<Map<String, Object>> eliminarContenido(
            @PathVariable String id,
            @PathVariable String contenidoId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de eliminar contenido de lista de gestor {} sin token de autorización", id);
                response.put(SUCCESS, false);
                response.put(MENSAJE, TOKEN_REQUERIDO);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String token = extraerToken(authHeader);
            PlaylistDto listaActualizada = listaService.removeContenido(id, contenidoId, token);
            
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Contenido eliminado de la lista exitosamente");
            response.put(LISTA, listaActualizada);
            response.put(TOTAL_CONTENIDOS, listaActualizada.getContenidosIds().size());
            
            logger.info("Contenido {} eliminado de lista de gestor {} exitosamente", contenidoId, id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al eliminar contenido de lista de gestor {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al eliminar contenido de la lista");
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar contenido de lista de gestor {}", id, e);
            response.put(SUCCESS, false);
            response.put(MENSAJE, ERROR_INTERNO);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    


    /**
     * Extrae el token del header de autorización
     */
    private String extraerToken(String authHeader) {
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return authHeader.trim();
    }
    
    /**
     * Maneja las excepciones y retorna la respuesta HTTP apropiada
     */
    private ResponseEntity<Map<String, Object>> manejarExcepcion(RuntimeException e, String mensajeGenerico) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        
        String mensajeError = e.getMessage();
        
        if (mensajeError.contains("Token") && 
            (mensajeError.contains("inválido") || mensajeError.contains("expirado") || 
             mensajeError.contains("no proporcionado"))) {
            response.put(MENSAJE, mensajeError);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        if (mensajeError.contains("no encontrada") || mensajeError.contains("no encontrado")) {
            response.put(MENSAJE, mensajeError);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        if (mensajeError.contains("permisos") || mensajeError.contains("autorizado")) {
            response.put(MENSAJE, mensajeError);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        response.put(MENSAJE, mensajeError != null ? mensajeError : mensajeGenerico);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}