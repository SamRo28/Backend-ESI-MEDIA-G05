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
 * Controlador REST específico para usuarios (visualizadores) en gestión de listas
 * 
 * FUNCIONALIDAD:
 * - Los visualizadores pueden crear y gestionar sus propias listas PRIVADAS
 * - Pueden acceder en SOLO LECTURA a listas VISIBLES de gestores
 * - Se aplica filtrado automático por edad, VIP y disponibilidad temporal
 * 
 * RUTAS:
 * GESTIÓN DE LISTAS PROPIAS:
 * - POST /listas/usuario - Crear nueva lista privada
 * - GET /listas/usuario/mias - Obtener listas propias del usuario
 * - GET /listas/usuario/{id} - Obtener lista propia por ID
 * - GET /listas/usuario/{id}/contenidos - Obtener contenidos de lista propia
 * - PUT /listas/usuario/{id} - Editar lista propia
 * - DELETE /listas/usuario/{id} - Eliminar lista propia
 * - POST /listas/usuario/{id}/contenidos/{contenidoId} - Añadir contenido a lista propia
 * - DELETE /listas/usuario/{id}/contenidos/{contenidoId} - Eliminar contenido de lista propia
 * 
 * ACCESO A LISTAS PÚBLICAS DE GESTORES (SOLO LECTURA):
 * - GET /listas/usuario/publicas - Obtener listas visibles de gestores
 * - GET /listas/usuario/publica/{id} - Ver detalle de lista pública (sin modificar)
 * - GET /listas/usuario/publica/{id}/contenidos - Ver contenidos filtrados de lista pública
 */
@RestController
@RequestMapping("/listas/usuario")
@CrossOrigin(origins = "*")
public class ListaUsuarioController extends BaseListaController {
    
    private static final Logger logger = LoggerFactory.getLogger(ListaUsuarioController.class);
    
    @Override
    protected Logger getLogger() {
        return logger;
    }
    
    /**
     * Crea una nueva lista para el usuario autenticado
     * POST /listas/usuario
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearLista(
            @RequestBody PlaylistDto listaDto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Usar validación de token común
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "crear lista de usuario");
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            PlaylistDto listaCreada = listaService.crearListaDesdeDto(listaDto, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Lista creada correctamente y asociada al usuario.");
            response.put(LISTA, listaCreada);
            
            logger.info("Lista de usuario creada exitosamente: {} (ID: {})", listaCreada.getNombre(), listaCreada.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al crear lista de usuario: {}", e.getMessage());
            return manejarExcepcion(e, "Error al crear la lista");
        } catch (Exception e) {
            return manejarExcepcionGeneral(e, "crear lista de usuario");
        }
    }
    
    /**
     * Obtiene las listas propias del usuario autenticado
     * GET /listas/usuario/mias
     */
    @GetMapping("/mias")
    public ResponseEntity<Map<String, Object>> obtenerListasUsuario(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "obtener listas de usuario");
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            List<PlaylistDto> listas = listaService.findListasPropias(token);
            
            logger.info("Listas de usuario obtenidas exitosamente: {} lista(s)", listas.size());
            return crearRespuestaExitoListas("Listas obtenidas exitosamente", listas);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener listas de usuario: {}", e.getMessage());
            return manejarExcepcion(e, "Error al obtener las listas");
        } catch (Exception e) {
            return manejarExcepcionGeneral(e, "obtener listas de usuario");
        }
    }
    
    /**
     * Obtiene una lista específica por ID del usuario
     * GET /listas/usuario/{id}
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
            
            logger.info("Lista de usuario obtenida exitosamente: {} (ID: {})", lista.getNombre(), lista.getId());
            return crearRespuestaExitoLista("Lista obtenida exitosamente", lista);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener lista de usuario {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al obtener la lista");
        } catch (Exception e) {
            return manejarExcepcionGeneral(e, "obtener lista de usuario " + id);
        }
    }
    
    /**
     * Obtiene los contenidos de una lista específica del usuario
     * GET /listas/usuario/{id}/contenidos
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
     * Edita una lista existente del usuario
     * PUT /listas/usuario/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> editarLista(
            @PathVariable String id,
            @RequestBody PlaylistDto updatedListaDto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "editar lista de usuario " + id);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            
            // Usar el nuevo método que maneja DTOs completos incluyendo contenidosIds
            PlaylistDto listaEditada = listaService.updateListaDesdeDto(id, updatedListaDto, token);
            
            logger.info("Lista de usuario editada exitosamente: {} (ID: {})", listaEditada.getNombre(), listaEditada.getId());
            return crearRespuestaExitoLista("Lista actualizada exitosamente", listaEditada);
            
        } catch (RuntimeException e) {
            logger.error("Error al editar lista de usuario {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al editar la lista");
        } catch (Exception e) {
            return manejarExcepcionGeneral(e, "editar lista de usuario " + id);
        }
    }
    
    /**
     * Elimina una lista del usuario
     * DELETE /listas/usuario/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarLista(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "eliminar lista de usuario " + id);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            listaService.deleteLista(id, token);
            
            logger.info("Lista de usuario eliminada exitosamente: ID {}", id);
            return crearRespuestaExitoSimple("Lista eliminada exitosamente");
            
        } catch (RuntimeException e) {
            logger.error("Error al eliminar lista de usuario {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al eliminar la lista");
        } catch (Exception e) {
            return manejarExcepcionGeneral(e, "eliminar lista de usuario " + id);
        }
    }
    
    /**
     * Añade un contenido a una lista del usuario
     * POST /listas/usuario/{id}/contenidos/{contenidoId}
     */
    @PostMapping("/{id}/contenidos/{contenidoId}")
    public ResponseEntity<Map<String, Object>> agregarContenido(
            @PathVariable String id,
            @PathVariable String contenidoId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "añadir contenido a lista de usuario " + id);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            PlaylistDto listaActualizada = listaService.addContenido(id, contenidoId, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Contenido añadido a la lista exitosamente");
            response.put(LISTA, listaActualizada);
            response.put(TOTAL_CONTENIDOS, listaActualizada.getContenidosIds().size());
            
            logger.info("Contenido {} añadido a lista de usuario {} exitosamente", contenidoId, id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al añadir contenido a lista de usuario {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al añadir contenido a la lista");
        } catch (Exception e) {
            return manejarExcepcionGeneral(e, "añadir contenido a lista de usuario " + id);
        }
    }
    
    /**
     * Elimina un contenido de una lista del usuario
     * DELETE /listas/usuario/{id}/contenidos/{contenidoId}
     */
    @DeleteMapping("/{id}/contenidos/{contenidoId}")
    public ResponseEntity<Map<String, Object>> eliminarContenido(
            @PathVariable String id,
            @PathVariable String contenidoId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "eliminar contenido de lista de usuario " + id);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            PlaylistDto listaActualizada = listaService.removeContenido(id, contenidoId, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Contenido eliminado de la lista exitosamente");
            response.put(LISTA, listaActualizada);
            response.put(TOTAL_CONTENIDOS, listaActualizada.getContenidosIds().size());
            
            logger.info("Contenido {} eliminado de lista de usuario {} exitosamente", contenidoId, id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al eliminar contenido de lista de usuario {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al eliminar contenido de la lista");
        } catch (Exception e) {
            return manejarExcepcionGeneral(e, "eliminar contenido de lista de usuario " + id);
        }
    }
    
    /**
     * Obtiene todas las listas visibles de gestores (públicas)
     * Accesible para visualizadores - SOLO LECTURA
     * GET /listas/usuario/publicas
     */
    @GetMapping("/publicas")
    public ResponseEntity<Map<String, Object>> obtenerListasPublicas(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "acceder a listas públicas");
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            List<PlaylistDto> listas = listaService.findListasVisiblesGestores(token);
            
            logger.info("Listas públicas obtenidas exitosamente: {} lista(s)", listas.size());
            return crearRespuestaExitoListas("Listas públicas obtenidas exitosamente", listas);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener listas públicas: {}", e.getMessage());
            return manejarExcepcion(e, "Error al obtener las listas públicas");
        } catch (Exception e) {
            return manejarExcepcionGeneral(e, "obtener listas públicas");
        }
    }
    
    /**
     * Obtiene una lista pública de gestor con validación de permisos
     * Permite a visualizadores ver listas VISIBLES de gestores (solo lectura)
     * GET /listas/usuario/publica/{id}
     */
    @GetMapping("/publica/{id}")
    public ResponseEntity<Map<String, Object>> obtenerListaPublicaPorId(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "obtener lista pública " + id);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            PlaylistDto lista = listaService.findListaByIdConPermisos(id, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Lista pública obtenida exitosamente");
            response.put(LISTA, lista);
            
            // Indicar que los visualizadores NO pueden modificar listas de gestores
            response.put("puedeModificar", false);
            
            logger.info("Lista pública obtenida exitosamente: {} (ID: {})", lista.getNombre(), lista.getId());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener lista pública {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al obtener la lista pública");
        } catch (Exception e) {
            return manejarExcepcionGeneral(e, "obtener lista pública " + id);
        }
    }
    
    /**
     * Obtiene los contenidos de una lista pública con filtrado automático
     * Aplica restricciones de edad, VIP y disponibilidad temporal para visualizadores
     * GET /listas/usuario/publica/{id}/contenidos
     */
    @GetMapping("/publica/{id}/contenidos")
    public ResponseEntity<Map<String, Object>> obtenerContenidosListaPublica(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<Map<String, Object>> validacion = validarToken(authHeader, "obtener contenidos de lista pública " + id);
        if (validacion != null) return validacion;
        
        try {
            String token = extraerToken(authHeader);
            
            // Primero validar acceso a la lista
            PlaylistDto lista = listaService.findListaByIdConPermisos(id, token);
            
            // Obtener contenidos con filtrado automático según tipo de usuario
            List<iso25.g05.esi_media.dto.ContenidoResumenDTO> contenidos = listaService.findContenidosListaConFiltrado(id, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put(MENSAJE, "Contenidos filtrados obtenidos exitosamente");
            response.put(CONTENIDOS, contenidos);
            response.put(TOTAL, contenidos.size());
            response.put(LISTA, lista);
            
            logger.info("Contenidos filtrados de lista pública {} obtenidos exitosamente: {} contenido(s)", id, contenidos.size());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener contenidos de lista pública {}: {}", id, e.getMessage());
            return manejarExcepcion(e, "Error al obtener los contenidos de la lista");
        } catch (Exception e) {
            return manejarExcepcionGeneral(e, "obtener contenidos de lista pública " + id);
        }
    }

}