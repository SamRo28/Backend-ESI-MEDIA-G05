package iso25.g05.esi_media.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ListaGestorController extends BaseListaController {
    
    private static final Logger logger = LoggerFactory.getLogger(ListaGestorController.class);
    
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
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "crear lista de gestor", logger);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            PlaylistDto listaCreada = listaService.crearListaDesdeDto(listaDto, token);
            
            Map<String, Object> response = crearRespuestaExito("Lista creada correctamente y asociada al usuario.");
            response.put(LISTA, listaCreada);
            
            logger.info("Lista de gestor creada exitosamente: {} (ID: {})", listaCreada.getNombre(), listaCreada.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al crear lista de gestor: {}", e.getMessage());
            return manejarExcepcion(e, "Error al crear la lista");
        } catch (Exception e) {
            return manejarExcepcionGenerica(e, logger);
        }
    }
    
    /**
     * Obtiene las listas propias del gestor autenticado
     * GET /listas/gestor/mias
     */
    @GetMapping("/mias")
    public ResponseEntity<Map<String, Object>> obtenerListasPropiasGestor(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "obtener listas propias de gestor", logger);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            List<PlaylistDto> listas = listaService.findListasPropias(token);
            
            Map<String, Object> response = crearRespuestaExito("Listas propias obtenidas exitosamente");
            response.put(LISTAS, listas);
            response.put(TOTAL, listas.size());
            
            logger.info("Listas propias de gestor obtenidas exitosamente: {} lista(s)", listas.size());
            return crearRespuestaExitoListas("Listas propias obtenidas exitosamente", listas);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener listas propias de gestor: {}", e.getMessage());
            return manejarExcepcion(e, "Error al obtener las listas propias");
        } catch (Exception e) {
            return manejarExcepcionGenerica(e, logger);
        }
    }
    
    /**
     * Obtiene todas las listas disponibles para gestores (públicas + de otros gestores)
     * GET /listas/gestor/todas
     */
    @GetMapping("/todas")
    public ResponseEntity<Map<String, Object>> obtenerTodasLasListas(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "obtener todas las listas", logger);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            List<PlaylistDto> listas = listaService.findListasVisiblesGestores(token);
            
            Map<String, Object> response = crearRespuestaExito("Todas las listas visibles obtenidas exitosamente");
            response.put(LISTAS, listas);
            response.put(TOTAL, listas.size());
            
            logger.info("Todas las listas obtenidas por gestor exitosamente: {} lista(s)", listas.size());
            return crearRespuestaExitoListas("Todas las listas visibles obtenidas exitosamente", listas);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener todas las listas para gestor: {}", e.getMessage());
            return manejarExcepcion(e, "Error al obtener todas las listas");
        } catch (Exception e) {
            return manejarExcepcionGenerica(e, logger);
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
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "obtener lista " + id);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            PlaylistDto lista = listaService.findListaById(id, token);
            
            logger.info("Lista de gestor obtenida exitosamente: {} (ID: {})", lista.getNombre(), lista.getId());
            return crearRespuestaExitoLista("Lista obtenida exitosamente", lista);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener lista de gestor {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al obtener la lista");
        } catch (Exception e) {
            return manejarExcepcionGeneral(e, "obtener lista de gestor " + id);
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
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "obtener contenidos de lista " + id);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            List<iso25.g05.esi_media.dto.ContenidoResumenDTO> contenidos = listaService.findContenidosLista(id, token);
            
            Map<String, Object> response = new HashMap<>();
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
            return manejarExcepcionGeneral(e, "obtener contenidos de lista " + id);
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
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "editar lista de gestor " + id);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            
            // Usar el nuevo método que maneja DTOs completos incluyendo contenidosIds
            PlaylistDto listaEditada = listaService.updateListaDesdeDto(id, updatedListaDto, token);
            
            logger.info("Lista de gestor editada exitosamente: {} (ID: {})", listaEditada.getNombre(), listaEditada.getId());
            return crearRespuestaExitoLista("Lista actualizada exitosamente", listaEditada);
            
        } catch (RuntimeException e) {
            logger.error("Error al editar lista de gestor {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al editar la lista");
        } catch (Exception e) {
            return manejarExcepcionGeneral(e, "editar lista de gestor " + id);
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
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "eliminar lista de gestor " + id);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            listaService.deleteLista(id, token);
            
            logger.info("Lista de gestor eliminada exitosamente: ID {}", id);
            return crearRespuestaExitoSimple("Lista eliminada exitosamente");
            
        } catch (RuntimeException e) {
            logger.error("Error al eliminar lista de gestor {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al eliminar la lista");
        } catch (Exception e) {
            return manejarExcepcionGeneral(e, "eliminar lista de gestor " + id);
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
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "añadir contenido a lista de gestor " + id);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            PlaylistDto listaActualizada = listaService.addContenido(id, contenidoId, token);
            
            Map<String, Object> response = new HashMap<>();
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
            return manejarExcepcionGeneral(e, "añadir contenido a lista de gestor " + id);
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
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "eliminar contenido de lista de gestor " + id);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            PlaylistDto listaActualizada = listaService.removeContenido(id, contenidoId, token);
            
            Map<String, Object> response = new HashMap<>();
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
            return manejarExcepcionGeneral(e, "eliminar contenido de lista de gestor " + id);
        }
    }
    


}