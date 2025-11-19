package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.dto.ContenidoDTO;
import iso25.g05.esi_media.dto.TagStatDTO;
import iso25.g05.esi_media.service.FiltradoContenidosAvanzadoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST para el filtrado avanzado de contenidos
 * Expone endpoints para obtener TOP contenidos y TOP tags
 */
@RestController
@RequestMapping("/api/filtradoContenidosAvanzado")
@CrossOrigin(origins = "*")
public class FiltradoContenidosAvanzadoController {
    
    private final FiltradoContenidosAvanzadoService filtradoService;
    private static final Logger logger = LoggerFactory.getLogger(FiltradoContenidosAvanzadoController.class);
    private static final String TYPE_VIDEO = "video";
    private static final String TYPE_AUDIO = "audio";
    private static final String TYPE_ALL = "all";
    
    public FiltradoContenidosAvanzadoController(FiltradoContenidosAvanzadoService filtradoService) {
        this.filtradoService = filtradoService;
    }
    
    /**
     * Endpoint para obtener los TOP N contenidos con más visualizaciones
     * 
     * GET /api/filtradoContenidosAvanzado/top-contents
     * 
     * Query Parameters:
     * - limit: número de contenidos a devolver (default: 5, max: 50)
     * - contentType: tipo de contenido - "video", "audio", "all" (default: "all")
     * 
     * Headers:
     * - Authorization: Bearer token (opcional, para usuarios anónimos)
     * 
     * @param limit Número máximo de contenidos a devolver
     * @param contentType Tipo de contenido a filtrar
     * @param authHeader Header de autorización (opcional)
     * @return Lista de ContenidoDTO con los contenidos más vistos
     */
    @GetMapping("/top-contents")
    public ResponseEntity<List<ContenidoDTO>> topContents(
            @RequestParam(defaultValue = "all") String contentType,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        final int FIXED_LIMIT = 5; // TOP 5 obligatorio

        try {
                // Validar contentType in situ (sin utilidades externas)
                if (contentType == null || !(contentType.equals(TYPE_VIDEO) || contentType.equals(TYPE_AUDIO) || contentType.equals(TYPE_ALL))) {
                    contentType = TYPE_ALL; // Valor por defecto si no es válido
                }

            // TODO: Todavía No extraemos ni decodificamos el userId aquí; debe hacerlo la capa de auth.
            // Pasamos `null` como userId para mantener comportamiento conservador (no mostrar +18 NUNCA).
            String userId = null;

            // Llamar al servicio con límite fijo
            List<ContenidoDTO> topContents = filtradoService.getTopContents(FIXED_LIMIT, contentType, userId);

            return ResponseEntity.ok(topContents);

        } catch (Exception e) {
            logger.error("Error en top-contents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Endpoint para obtener los TOP N tags con más visualizaciones
     * 
     * GET /api/filtradoContenidosAvanzado/top-tags
     * 
     * Query Parameters:
     * - limit: número de tags a devolver (default: 5)
     * - contentType: tipo de contenido - "video", "audio", "all" (default: "all")
     * 
     * Headers:
     * - Authorization: Bearer token (opcional, para usuarios anónimos)
     * 
     * @param limit Número máximo de tags a devolver
     * @param contentType Tipo de contenido a filtrar
     * @param authHeader Header de autorización (opcional)
     * @return Lista de TagStatDTO con los tags más vistos
     */
    @GetMapping("/top-tags")
    public ResponseEntity<List<TagStatDTO>> topTags(
            @RequestParam(defaultValue = "all") String contentType,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        final int FIXED_LIMIT = 5; // TOP 5 obligatorio

        try {
                // Validar contentType in situ (sin utilidades externas)

                if (contentType == null || !(contentType.equals(TYPE_VIDEO) || contentType.equals(TYPE_AUDIO) || contentType.equals(TYPE_ALL))) {
                    contentType = TYPE_ALL; // Valor por defecto si no es válido
                }

                // TODO: Todavía No extraemos ni decodificamos el userId aquí; debe hacerlo la capa de auth.
                // Pasamos `null` como userId para mantener comportamiento conservador (no mostrar +18 NUNCA).
                String userId = null;

            // Llamar al servicio con límite fijo
            List<TagStatDTO> topTags = filtradoService.getTopTags(FIXED_LIMIT, contentType, userId);

            return ResponseEntity.ok(topTags);

        } catch (Exception e) {
            logger.error("Error en top-tags: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint para obtener los TOP N contenidos mejor valorados (promedio)
     * Comportamiento y parámetros iguales a `/top-contents`.
     */
    @GetMapping("/top-rated-contents")
    public ResponseEntity<List<ContenidoDTO>> topRatedContents(
            @RequestParam(defaultValue = "all") String contentType,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        final int FIXED_LIMIT = 5; // TOP 5 obligatorio

        try {
            if (contentType == null || !(contentType.equals(TYPE_VIDEO) || contentType.equals(TYPE_AUDIO) || contentType.equals(TYPE_ALL))) {
                contentType = TYPE_ALL;
            }

            // TODO: Todavía No extraemos ni decodificamos el userId aquí; debe hacerlo la capa de auth.
            // Pasamos `null` como userId para mantener comportamiento conservador (no mostrar +18 NUNCA).
            String userId = null;

            List<ContenidoDTO> topContents = filtradoService.getTopRatedContents(FIXED_LIMIT, contentType, userId);

            return ResponseEntity.ok(topContents);

        } catch (Exception e) {
            logger.error("Error en top-rated-contents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Endpoint adicional para obtener estadísticas generales (opcional)
     * Puede ser útil para debugging y monitoreo
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("service", "FiltradoContenidosAvanzado");
            stats.put("status", "UP");
            stats.put("description", "Servicio para filtrado avanzado de contenidos");
            stats.put("endpoints", List.of("/top-contents", "/top-tags"));
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("service", "FiltradoContenidosAvanzado");
            errorStats.put("status", "ERROR");
            errorStats.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorStats);
        }
    }
}