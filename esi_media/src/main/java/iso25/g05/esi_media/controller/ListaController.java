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
import iso25.g05.esi_media.model.Lista;
import iso25.g05.esi_media.service.ListaService;

/**
 * Controlador REST para gestión de listas de contenido
 * 
 * POLÍTICAS:
 * - Todas las rutas requieren token válido (Authorization: Bearer <token>)
 * - 401 UNAUTHORIZED si el token falta o está expirado
 * - 403 FORBIDDEN si el usuario no es el creador (verificación de autoría)
 * - 404 NOT FOUND si el recurso no existe
 * - Devuelve PlaylistDto en respuestas exitosas
 * - Solo métodos HTTP semánticos (no GET para modificar estado)
 */
@RestController
@RequestMapping("/api/listas")
@CrossOrigin(origins = "*")
public class ListaController {
    
    private static final Logger logger = LoggerFactory.getLogger(ListaController.class);
    
    @Autowired
    private ListaService listaService;
    
    /**
     * Crea una nueva lista de contenido
     * POST /listas
     * 
     * @param lista Datos de la lista a crear
     * @param authHeader Token de autorización JWT en formato "Bearer {token}"
     * @return ResponseEntity con la lista creada o mensaje de error
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearLista(
            @RequestBody Lista lista,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar que el token esté presente
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de crear lista sin token de autorización");
                response.put("success", false);
                response.put("mensaje", "Token de autorización requerido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Extraer el token del header (eliminar "Bearer " si está presente)
            String token = extraerToken(authHeader);
            
            // Crear la lista usando el servicio
            PlaylistDto listaCreada = listaService.createLista(lista, token);
            
            response.put("success", true);
            response.put("mensaje", "Lista creada exitosamente");
            response.put("lista", listaCreada);
            
            logger.info("Lista creada exitosamente: {} (ID: {})", listaCreada.getNombre(), listaCreada.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al crear lista: {}", e.getMessage());
            return manejarExcepcion(e, "Error al crear la lista");
        } catch (Exception e) {
            logger.error("Error inesperado al crear lista", e);
            response.put("success", false);
            response.put("mensaje", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Edita una lista existente
     * PUT /listas/{id}
     * 
     * @param id ID de la lista a editar
     * @param updatedLista Datos actualizados de la lista
     * @param authHeader Token de autorización JWT
     * @return ResponseEntity con la lista actualizada o mensaje de error
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> editarLista(
            @PathVariable String id,
            @RequestBody Lista updatedLista,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar que el token esté presente
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de editar lista {} sin token de autorización", id);
                response.put("success", false);
                response.put("mensaje", "Token de autorización requerido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Extraer el token del header
            String token = extraerToken(authHeader);
            
            // Editar la lista usando el servicio
            PlaylistDto listaEditada = listaService.updateLista(id, updatedLista, token);
            
            response.put("success", true);
            response.put("mensaje", "Lista actualizada exitosamente");
            response.put("lista", listaEditada);
            
            logger.info("Lista editada exitosamente: {} (ID: {})", listaEditada.getNombre(), listaEditada.getId());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al editar lista {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al editar la lista");
        } catch (Exception e) {
            logger.error("Error inesperado al editar lista {}", id, e);
            response.put("success", false);
            response.put("mensaje", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Elimina una lista
     * DELETE /listas/{id}
     * 
     * @param id ID de la lista a eliminar
     * @param authHeader Token de autorización JWT
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarLista(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar que el token esté presente
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de eliminar lista {} sin token de autorización", id);
                response.put("success", false);
                response.put("mensaje", "Token de autorización requerido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Extraer el token del header
            String token = extraerToken(authHeader);
            
            // Eliminar la lista usando el servicio
            listaService.deleteLista(id, token);
            
            response.put("success", true);
            response.put("mensaje", "Lista eliminada exitosamente");
            
            logger.info("Lista eliminada exitosamente: ID {}", id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al eliminar lista {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al eliminar la lista");
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar lista {}", id, e);
            response.put("success", false);
            response.put("mensaje", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtiene todas las listas del usuario autenticado
     * GET /api/listas/mias
     * 
     * @param authHeader Token de autorización JWT (requerido)
     * @return ResponseEntity con la lista de listas o mensaje de error
     */
    @GetMapping("/mias")
    public ResponseEntity<Map<String, Object>> obtenerListasUsuario(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar que el token esté presente
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de obtener listas sin token de autorización");
                response.put("success", false);
                response.put("mensaje", "Token de autorización requerido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Extraer el token del header
            String token = extraerToken(authHeader);
            
            // Obtener las listas del usuario
            List<PlaylistDto> listas = listaService.findListasPropias(token);
            
            response.put("success", true);
            response.put("mensaje", "Listas obtenidas exitosamente");
            response.put("listas", listas);
            response.put("total", listas.size());
            
            logger.info("Listas obtenidas exitosamente: {} lista(s)", listas.size());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener listas: {}", e.getMessage());
            return manejarExcepcion(e, "Error al obtener las listas");
        } catch (Exception e) {
            logger.error("Error inesperado al obtener listas", e);
            response.put("success", false);
            response.put("mensaje", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Extrae el token del header de autorización
     * Maneja tanto el formato "Bearer {token}" como el token directo
     * 
     * @param authHeader Header de autorización
     * @return Token extraído
     */
    private String extraerToken(String authHeader) {
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return authHeader.trim();
    }
    
    /**
     * Maneja las excepciones y retorna la respuesta HTTP apropiada
     * 
     * @param e Excepción capturada
     * @param mensajeGenerico Mensaje genérico a mostrar
     * @return ResponseEntity con el código HTTP y mensaje apropiados
     */
    private ResponseEntity<Map<String, Object>> manejarExcepcion(RuntimeException e, String mensajeGenerico) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        
        String mensajeError = e.getMessage();
        
        // Determinar el código HTTP basado en el mensaje de error
        if (mensajeError.contains("Token") && 
            (mensajeError.contains("inválido") || mensajeError.contains("expirado") || 
             mensajeError.contains("no proporcionado"))) {
            response.put("mensaje", mensajeError);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        if (mensajeError.contains("no encontrada") || mensajeError.contains("no encontrado")) {
            response.put("mensaje", mensajeError);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        if (mensajeError.contains("permisos") || mensajeError.contains("autorizado")) {
            response.put("mensaje", mensajeError);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        // Error de validación genérico
        response.put("mensaje", mensajeError != null ? mensajeError : mensajeGenerico);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Añade un contenido existente a una lista
     * POST /api/listas/{id}/contenidos/{contenidoId}
     * 
     * @param id ID de la lista
     * @param contenidoId ID del contenido a añadir
     * @param authHeader Token de autorización JWT (requerido)
     * @return ResponseEntity con la lista actualizada o mensaje de error
     */
    @PostMapping("/{id}/contenidos/{contenidoId}")
    public ResponseEntity<Map<String, Object>> agregarContenido(
            @PathVariable String id,
            @PathVariable String contenidoId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar que el token esté presente
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de añadir contenido a lista {} sin token de autorización", id);
                response.put("success", false);
                response.put("mensaje", "Token de autorización requerido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Extraer el token del header
            String token = extraerToken(authHeader);
            
            // Añadir el contenido a la lista usando el servicio
            PlaylistDto listaActualizada = listaService.addContenido(id, contenidoId, token);
            
            response.put("success", true);
            response.put("mensaje", "Contenido añadido a la lista exitosamente");
            response.put("lista", listaActualizada);
            response.put("totalContenidos", listaActualizada.getContenidosIds().size());
            
            logger.info("Contenido {} añadido a lista {} exitosamente", contenidoId, id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al añadir contenido a lista {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al añadir contenido a la lista");
        } catch (Exception e) {
            logger.error("Error inesperado al añadir contenido a lista {}", id, e);
            response.put("success", false);
            response.put("mensaje", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Elimina un contenido de una lista
     * DELETE /api/listas/{id}/contenidos/{contenidoId}
     * 
     * @param id ID de la lista
     * @param contenidoId ID del contenido a eliminar
     * @param authHeader Token de autorización JWT (requerido)
     * @return ResponseEntity con la lista actualizada o mensaje de error
     */
    @DeleteMapping("/{id}/contenidos/{contenidoId}")
    public ResponseEntity<Map<String, Object>> eliminarContenido(
            @PathVariable String id,
            @PathVariable String contenidoId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar que el token esté presente
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Intento de eliminar contenido de lista {} sin token de autorización", id);
                response.put("success", false);
                response.put("mensaje", "Token de autorización requerido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Extraer el token del header
            String token = extraerToken(authHeader);
            
            // Eliminar el contenido de la lista usando el servicio
            PlaylistDto listaActualizada = listaService.removeContenido(id, contenidoId, token);
            
            response.put("success", true);
            response.put("mensaje", "Contenido eliminado de la lista exitosamente");
            response.put("lista", listaActualizada);
            response.put("totalContenidos", listaActualizada.getContenidosIds().size());
            
            logger.info("Contenido {} eliminado de lista {} exitosamente", contenidoId, id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al eliminar contenido de lista {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al eliminar contenido de la lista");
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar contenido de lista {}", id, e);
            response.put("success", false);
            response.put("mensaje", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
