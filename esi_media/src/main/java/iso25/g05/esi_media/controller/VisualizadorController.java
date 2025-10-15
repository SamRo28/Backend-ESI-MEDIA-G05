package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.model.VisualizadorRegistroDTO;
import iso25.g05.esi_media.service.RegistroResultado;
import iso25.g05.esi_media.service.VisualizadorService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar el registro de visualizadores.
 * 
 * PROPÓSITO: Recibir peticiones HTTP del frontend, validar datos, 
 * llamar al servicio de negocio y devolver respuestas HTTP apropiadas.
 * 
 * ENDPOINTS:
 * POST /api/visualizador/registro - Registrar nuevo visualizador
 */
@RestController
@RequestMapping("/api/visualizador")
// Configuración CORS segura para desarrollo local - CAMBIAR EN PRODUCCIÓN
@CrossOrigin(
    origins = {"http://localhost:4200", "http://localhost:3000"},
    allowedHeaders = {"Content-Type", "Authorization"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
)
public class VisualizadorController {
    
    /**
     * Servicio que contiene la lógica de negocio del registro
     */
    private final VisualizadorService visualizadorService;
    
    /**
     * Constructor que inyecta el servicio
     */
    public VisualizadorController(VisualizadorService visualizadorService) {
        this.visualizadorService = visualizadorService;
    }
    
    /**
     * Endpoint principal para registrar un nuevo visualizador
     * 
     * FLUJO COMPLETO:
     * 1. Recibe JSON del frontend y lo convierte a VisualizadorRegistroDTO
     * 2. @Valid activa las validaciones de anotaciones Jakarta automáticamente
     * 3. Si hay errores de validación, los capturamos con BindingResult
     * 4. Si no hay errores básicos, llamamos al servicio para validaciones de negocio
     * 5. Devolvemos respuesta HTTP apropiada según el resultado
     * 
     * EJEMPLOS DE USO:
     * 
     * CASO EXITOSO:
     * POST /api/visualizador/registro
     * Body: {"nombre": "Juan", "apellidos": "Pérez", "email": "juan@email.com", ...}
     * → Response 201: {"mensaje": "Usuario registrado", "exitoso": true, "visualizador": {...}}
     * 
     * CASO CON ERRORES:
     * POST /api/visualizador/registro  
     * Body: {"nombre": "", "email": "email-inválido", ...}
     * → Response 400: {"mensaje": "Errores de validación", "errores": ["El nombre es obligatorio", "Email inválido"]}
     */
    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registrarVisualizador(
            @Valid @RequestBody VisualizadorRegistroDTO visualizadorDTO,
            BindingResult bindingResult) {
        
        // PASO 1: Verificar si hubo errores de validación automática (@NotBlank, @Email, etc.)
        if (bindingResult.hasErrors()) {
            // Extraer todos los mensajes de error de las anotaciones Jakarta
            List<String> erroresValidacion = new ArrayList<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                erroresValidacion.add(error.getDefaultMessage());
            }
            
            // Devolver respuesta HTTP 400 (Bad Request) con los errores
            return crearRespuestaError(HttpStatus.BAD_REQUEST, 
                                     "Errores de validación en los datos enviados", 
                                     erroresValidacion);
        }
        
        // PASO 2: Si las validaciones básicas pasaron, llamar al servicio de negocio
        RegistroResultado resultado = visualizadorService.registrarVisualizador(visualizadorDTO);
        
        // PASO 3: Analizar el resultado y devolver respuesta HTTP apropiada
        if (resultado.isExitoso()) {
            // ÉXITO: Devolver HTTP 201 (Created) con los datos del usuario creado
            return crearRespuestaExito(HttpStatus.CREATED, resultado);
        } else {
            // ERROR: Devolver HTTP 400 (Bad Request) con los errores específicos
            return crearRespuestaError(HttpStatus.BAD_REQUEST, 
                                     resultado.getMensaje(), 
                                     resultado.getErrores());
        }
    }
    
    /**
     * Método helper para crear respuestas de éxito estandarizadas
     * 
     * ESTRUCTURA DE RESPUESTA EXITOSA:
     * {
     *   "exitoso": true,
     *   "mensaje": "Visualizador registrado exitosamente",
     *   "visualizador": {
     *     "nombre": "Juan",
     *     "email": "juan@email.com",
     *     ... (sin contraseña por seguridad)
     *   }
     * }
     */
    private ResponseEntity<Map<String, Object>> crearRespuestaExito(HttpStatus status, RegistroResultado resultado) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("exitoso", true);
        respuesta.put("mensaje", resultado.getMensaje());
        
        // Incluir datos del visualizador (sin información sensible)
        if (resultado.getVisualizador() != null) {
            Map<String, Object> datosVisualizador = new HashMap<>();
            datosVisualizador.put("nombre", resultado.getVisualizador().getNombre());
            datosVisualizador.put("apellidos", resultado.getVisualizador().getApellidos());
            datosVisualizador.put("email", resultado.getVisualizador().getEmail());
            datosVisualizador.put("alias", resultado.getVisualizador().getAlias());
            datosVisualizador.put("vip", resultado.getVisualizador().isVip());
            // NOTA: NO incluimos la contraseña por seguridad
            
            respuesta.put("visualizador", datosVisualizador);
        }
        
        return new ResponseEntity<>(respuesta, status);
    }
    
    /**
     * Método helper para crear respuestas de error estandarizadas
     * 
     * ESTRUCTURA DE RESPUESTA DE ERROR:
     * {
     *   "exitoso": false,
     *   "mensaje": "Registro fallido: corrija los errores indicados",
     *   "errores": [
     *     "El nombre es obligatorio",
     *     "El email ya está registrado",
     *     "La contraseña debe tener al menos 8 caracteres"
     *   ]
     * }
     */
    private ResponseEntity<Map<String, Object>> crearRespuestaError(HttpStatus status, String mensaje, List<String> errores) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("exitoso", false);
        respuesta.put("mensaje", mensaje);
        respuesta.put("errores", errores != null ? errores : new ArrayList<>());
        
        return new ResponseEntity<>(respuesta, status);
    }
    
    /**
     * Endpoint adicional para obtener todos los visualizadores registrados
     * (Útil para debugging y testing - en producción probablemente se removería)
     * 
     * GET /api/visualizador/todos
     */
    @GetMapping("/todos")
    public ResponseEntity<Map<String, Object>> obtenerTodosLosVisualizadores() {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("visualizadores", visualizadorService.obtenerTodosLosVisualizadores());
        respuesta.put("total", visualizadorService.obtenerTodosLosVisualizadores().size());
        
        return ResponseEntity.ok(respuesta);
    }
    
    /**
     * Endpoint adicional para limpiar todos los registros
     * (Solo para testing - en producción se removería)
     * 
     * DELETE /api/visualizador/limpiar
     */
    @DeleteMapping("/limpiar")
    public ResponseEntity<Map<String, Object>> limpiarRegistros() {
        visualizadorService.limpiarRegistros();
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Todos los registros han sido eliminados");
        respuesta.put("exitoso", true);
        
        return ResponseEntity.ok(respuesta);
    }
}