package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.service.VisualizadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de prueba para verificar la persistencia MongoDB.
 * 
 * PROPÓSITO:
 * - Probar que la conexión a MongoDB funcione
 * - Verificar operaciones CRUD básicas
 * - Testing rápido sin necesidad de Postman o frontend
 * 
 * ENDPOINTS DE PRUEBA:
 * GET /api/test/visualizadores - Listar todos los visualizadores
 * GET /api/test/visualizadores/{email} - Buscar por email
 * DELETE /api/test/visualizadores - Limpiar base de datos (solo desarrollo)
 * GET /api/test/stats - Estadísticas básicas
 * 
 * NOTA: Este controlador es solo para desarrollo y testing
 * En producción se puede eliminar o proteger con autenticación
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(
    origins = {"http://localhost:4200", "http://localhost:3000"},
    allowedHeaders = {"Content-Type", "Authorization"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
)
public class TestMongoController {
    
    private final VisualizadorService visualizadorService;
    
    public TestMongoController(VisualizadorService visualizadorService) {
        this.visualizadorService = visualizadorService;
    }
    
    /**
     * Obtener todos los visualizadores registrados
     * 
     * PRUEBA: Verificar que MongoDB devuelve datos correctamente
     * URL: GET http://localhost:8080/api/test/visualizadores
     */
    @GetMapping("/visualizadores")
    public ResponseEntity<List<Visualizador>> obtenerTodosLosVisualizadores() {
        try {
            List<Visualizador> visualizadores = visualizadorService.obtenerTodosLosVisualizadores();
            return ResponseEntity.ok(visualizadores);
        } catch (Exception e) {
            // Error de conexión o configuración
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Buscar visualizador por email
     * 
     * PRUEBA: Verificar búsquedas específicas funcionan
     * URL: GET http://localhost:8080/api/test/visualizadores/juan@email.com
     */
    @GetMapping("/visualizadores/{email}")
    public ResponseEntity<Visualizador> buscarPorEmail(@PathVariable String email) {
        try {
            Optional<Visualizador> visualizador = visualizadorService.buscarPorEmail(email);
            if (visualizador.isPresent()) {
                return ResponseEntity.ok(visualizador.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Limpiar base de datos (SOLO PARA DESARROLLO)
     * 
     * PRUEBA: Verificar que las operaciones de eliminación funcionan
     * URL: DELETE http://localhost:8080/api/test/visualizadores
     * 
     * ADVERTENCIA: Esto elimina TODOS los visualizadores
     */
    @DeleteMapping("/visualizadores")
    public ResponseEntity<String> limpiarBaseDatos() {
        try {
            long countAntes = visualizadorService.obtenerTodosLosVisualizadores().size();
            visualizadorService.limpiarRegistros();
            return ResponseEntity.ok("Base de datos limpiada. Eliminados: " + countAntes + " visualizadores");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body("Error al limpiar base de datos: " + e.getMessage());
        }
    }
    
    /**
     * Obtener estadísticas básicas
     * 
     * PRUEBA: Verificar que las consultas de agregación funcionan
     * URL: GET http://localhost:8080/api/test/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<String> obtenerEstadisticas() {
        try {
            long totalVisualizadores = visualizadorService.obtenerTodosLosVisualizadores().size();
            long totalVips = visualizadorService.contarVisualizadoresVip();
            
            String stats = String.format(
                "ESTADISTICAS MONGODB:\n" +
                "- Total visualizadores: %d\n" +
                "- Visualizadores VIP: %d\n" +
                "- Visualizadores regulares: %d\n" +
                "- Conexion MongoDB: FUNCIONANDO",
                totalVisualizadores, totalVips, (totalVisualizadores - totalVips)
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body("ERROR MongoDB: " + e.getMessage());
        }
    }
    
    /**
     * Verificar conectividad básica
     * 
     * PRUEBA: Endpoint simple para verificar que la aplicación responde
     * URL: GET http://localhost:8080/api/test/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG - Servidor funcionando correctamente");
    }
}