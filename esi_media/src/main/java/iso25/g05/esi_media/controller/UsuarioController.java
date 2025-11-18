package iso25.g05.esi_media.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.service.LogService;
import iso25.g05.esi_media.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador unificado para gestión de usuarios
 * Incluye endpoints para listar, registrar, login y gestión general
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UsuarioController {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
    private static final String MSG = "mensaje";
    private static final String NOMBRE = "nombre";
    private static final String APELLIDOS = "apellidos";
    private static final String EMAIL = "email";
    private static final String BLOQUEADO = "bloqueado";

    

    private final UsuarioRepository usuarioRepository;
    private final UserService userService;
    private final LogService logService;

    public UsuarioController(UsuarioRepository usuarioRepository, UserService userService, LogService logService) {
        this.usuarioRepository = usuarioRepository;
        this.userService = userService;
        this.logService = logService;
    }
    
    // ==================== ENDPOINTS DE LOGIN (/users) ====================
    
    /**
     * Login de usuario con email y contraseña
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData, 
                                    HttpServletRequest request) { // <-- AÑADIR HttpServletRequest
        
        String ipAddress = getClientIp(request);
        try {
            Usuario loggedInUser = userService.login(loginData, ipAddress);
            
            if (loggedInUser == null){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Credenciales inválidas");
            }
            else if(loggedInUser.isBloqueado()){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario bloqueado, hable con su administrador");
            }
        
            Map<String, Object> res =  Map.of(
                "tipo", loggedInUser.getClass().getSimpleName(),
                "usuario", loggedInUser,
                "token",  loggedInUser.getSesionstoken().getToken()
                );
            return ResponseEntity.status(HttpStatus.OK).body(res);
        } catch (ResponseStatusException e) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
         
        
        
    }

    @PostMapping("/logout")
     public ResponseEntity<?> login(@RequestBody Map<String, String> loginData){
        String token = loginData.get("token");

        if (userService.logout(token)){
            
                return ResponseEntity.status(HttpStatus.OK).body("Se ha cerrado sesión correctamente");
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Credenciales inválidas");

     }

    // ==================== SUSCRIPCION (VIP/ESTANDAR) ====================

    @GetMapping("/{id}/subscription")
    public ResponseEntity<?> getSubscription(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "auth", required = false) String authQueryParam) {
        try {
            Usuario authUser = validarTokenYObtenerUsuario(authHeader, authQueryParam);
            // Solo comprobamos que haya usuario autenticado
            if (authUser == null || authUser.getId() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(MSG, "No autorizado"));
            }
            if (!(authUser instanceof Visualizador visualizador)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MSG, "Solo visualizadores"));
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("vip", visualizador.isVip());
            resp.put("fechaCambio", visualizador.getFechacambiosuscripcion());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(MSG, "No se pudo obtener suscripción"));
        }
    }

    @PutMapping("/{id}/subscription")
    public ResponseEntity<?> updateSubscription(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "auth", required = false) String authQueryParam,
            @RequestBody Map<String, Object> body) {
        try {
            Usuario authUser = validarTokenYObtenerUsuario(authHeader, authQueryParam);
            // Solo comprobamos que haya usuario autenticado
            if (authUser == null || authUser.getId() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(MSG, "No autorizado"));
            }
            if (!(authUser instanceof Visualizador visualizador)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MSG, "Solo visualizadores"));
            }
            Object v = body != null ? body.get("vip") : null;
            if (!(v instanceof Boolean)) {
                return ResponseEntity.badRequest().body(Map.of(MSG, "Solicitud invalida"));
            }
            boolean nuevoVip = (Boolean) v;
            boolean anteriorVip = visualizador.isVip();
            if (nuevoVip != anteriorVip) {
                visualizador.setVip(nuevoVip);
                visualizador.setFechacambiosuscripcion(new java.util.Date());
                usuarioRepository.save(visualizador);
                // Log de auditoría
                try { logService.registrarAccion("Cambio de suscripcion a " + (nuevoVip ? "VIP" : "Estandar"),
                        authUser.getEmail()); } catch (Exception ignore) {}
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("vip", visualizador.isVip());
            resp.put("fechaCambio", visualizador.getFechacambiosuscripcion());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(MSG, "No se pudo actualizar la suscripción"));
        }
    }

    // ==================== Helpers ====================
    private Usuario validarTokenYObtenerUsuario(String authHeader, String authQueryParam) {
        String tokenValue = extraerToken((authHeader != null && !authHeader.isBlank()) ? authHeader : authQueryParam);
        if (tokenValue == null || tokenValue.isBlank()) return null;
        return usuarioRepository.findBySesionToken(tokenValue).orElse(null);
    }

    private String extraerToken(String headerOrToken) {
        if (headerOrToken == null) return null;
        String v = headerOrToken.trim();
        if (v.toLowerCase().startsWith("bearer ")) {
            return v.substring(7).trim();
        }
        return v;
    }


    /**
     * Login con autenticación de 3 factores
     */
    @PostMapping("/login3Auth")
    public Map<String, Object> login3Auth(@RequestBody Map<String, String> loginData) {
        String codigoRecuperacionId = userService.login3Auth(loginData);
        Map<String, Object> response = new HashMap<>();
        response.put("codigoRecuperacionId", codigoRecuperacionId);
        return response;
    }


    /**
     * Login con autenticación de 3 factores
     */
    @PostMapping("/verify3AuthCode")
    public Token confirm3Auth(@RequestBody Map<String, String> loginData) {
        return userService.confirmLogin3Auth(loginData);
        
    }
    
    // ==================== ENDPOINTS DE USUARIOS (/api/usuarios) ====================

    /**
     * Obtener todos los usuarios - Endpoint compatible con frontend
     */
    @PostMapping("/listar")
    public ResponseEntity<?> listarUsuarios(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // Verificar que el token esté presente
            if (token == null || token.trim().isEmpty()) {
                logger.warn("Intento de acceso sin token al listar usuarios");
                Map<String, String> error = new HashMap<>();
                error.put(MSG, "Token de autorización requerido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
             
            
            logger.info("Acceso autorizado con token: {}", token.substring(0, Math.min(20, token.length())) + "...");
            
            List<Usuario> usuarios = usuarioRepository.findAll();
            logger.info("=== DEBUG LISTAR USUARIOS ===");
            for (Usuario u : usuarios) {
                logger.info("Usuario: {} - Clase: {}", u.getNombre(), u.getClass().getName());
            }
            List<Map<String, Object>> usuariosFormateados = usuarios.stream()
                .map(this::formatearUsuario)
                .toList();
                
            return ResponseEntity.ok(usuariosFormateados);
        } catch (Exception e) {
            logger.error("{}: {}", MSG, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put(MSG, "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/verify2FACode")
    public ResponseEntity<?> confirm2faCode(@RequestBody Map<String, String> data) {

        String token = userService.confirm2faCode(data);
        if (token == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(token);
    }
    
    /**
     * Formatear usuario al formato esperado por el frontend
     */
    private Map<String, Object> formatearUsuario(Usuario usuario) {
        Map<String, Object> usuarioFormateado = new HashMap<>();
        usuarioFormateado.put("id", usuario.getId());
        String rol = "Visualizador";
        if (usuario instanceof Administrador) {
            rol = "Administrador";
            logger.info("✅ {} es Administrador", usuario.getNombre());

        } else if (usuario instanceof GestordeContenido) {
            rol = "Gestor";
            logger.info("✅ {} es Gestor", usuario.getNombre());

        } else if (usuario instanceof Visualizador) {
            rol = "Visualizador";
            logger.info("✅ {} es Visualizador", usuario.getNombre());

        } else {
            logger.warn("⚠️ {} - Clase no reconocida: {}", usuario.getNombre(), usuario.getClass().getName());

        }
        usuarioFormateado.put("rol", rol);
        usuarioFormateado.put(NOMBRE, usuario.getNombre());
        usuarioFormateado.put(APELLIDOS, usuario.getApellidos());
        usuarioFormateado.put(EMAIL, usuario.getEmail());
       
        usuarioFormateado.put("foto", usuario.getFoto() != null ? usuario.getFoto() : "perfil1.png");
        usuarioFormateado.put(BLOQUEADO, usuario.isBloqueado());

        return usuarioFormateado;
    }
    
    
    /**
     * Obtener todos los usuarios - Endpoint original
     */
    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodosLosUsuarios() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    
    /**
     * Obtener un usuario por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable String id) {
        try {
            Optional<Usuario> usuario = usuarioRepository.findById(id);
            if (usuario.isPresent()) {
                return ResponseEntity.ok(usuario.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    
    /**
     * Buscar usuario por email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> obtenerUsuarioPorEmail(@PathVariable String email) {
        try {
            Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
            if (usuario.isPresent()) {
                return ResponseEntity.ok(usuario.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{id}/profile")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> updateProfile(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        String tipo = (String) updates.get("tipo");
        Map<String, Object> userUpdates = (Map<String, Object>) updates.get("userData");
        Usuario updatedUser;
        try {
            updatedUser = userService.updateUser(id, tipo, userUpdates);
            return updatedUser != null
            ? ResponseEntity.ok(updatedUser)
            : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar perfil");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar perfil: " + e.getMessage());
        }
        
    }
    
    
    /**
     * Eliminar un usuario y su contraseña asociada de manera optimizada
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarUsuario(@PathVariable String id) {
        long startTime = System.currentTimeMillis();
        Map<String, String> response = new HashMap<>();
        
        try {
            // Obtener información del usuario por ID (solo una vez)
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                String contraseniaId = null;
                
                // Capturar ID de contraseña si existe
                if (usuario.getContrasenia() != null && usuario.getContrasenia().getId() != null) {
                    contraseniaId = usuario.getContrasenia().getId();
                }
                
                
                // Eliminar usuario inmediatamente para liberar recursos
                usuarioRepository.deleteById(id);
                logger.info("Usuario eliminado: {}", id);
                
                // Eliminar contraseña después (si existe)
                if (contraseniaId != null) {
                    userService.deletePassword(contraseniaId);
                }
                
                // Eliminar contraseña después (si existe)
                if (contraseniaId != null) {
                    userService.deletePassword(contraseniaId);
                }
                
                long duration = System.currentTimeMillis() - startTime;
                response.put(MSG, "Usuario eliminado correctamente");
                response.put("tiempoEjecucion", duration + "ms");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error al eliminar usuario: {}", e.getMessage(), e);
            logger.error("Error al eliminar usuario: {}", e.getMessage(), e);
            response.put("error", "Error al eliminar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    /**
     * Método de ayuda para obtener la del cliente
     */
    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }
}
    

    
