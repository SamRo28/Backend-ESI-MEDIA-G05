package iso25.g05.esi_media.controller;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.service.UserService;

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
    private static final String DEPARTAMENTO = "departamento";
    private final UsuarioRepository usuarioRepository;
    private final UserService userService;

    public UsuarioController(UsuarioRepository usuarioRepository, UserService userService) {
        this.usuarioRepository = usuarioRepository;
        this.userService = userService;
    }
    
    // ==================== ENDPOINTS DE LOGIN (/users) ====================
    
    /**
     * Login de usuario con email y contraseña
     */
    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody Map<String, String> loginData) {
        Usuario loggedInUser = userService.login(loginData);
        if (loggedInUser == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials");
        return Map.of(
            "tipo", loggedInUser.getClass().getSimpleName(),
            "usuario", loggedInUser
        );
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
    @PostMapping("/confirm3Auth")
    public Map<String, Object> confirm3Auth(@RequestBody Map<String, String> loginData) {
        throw new UnsupportedOperationException("confirm3Auth endpoint is not implemented. Use login3Auth instead.");
    }
    
    // ==================== ENDPOINTS DE USUARIOS (/api/usuarios) ====================

    /**
     * Obtener todos los usuarios - Endpoint compatible con frontend
     */
    @GetMapping("/listar")
    public ResponseEntity<List<Map<String, Object>>> listarUsuarios() {
        try {
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @PostMapping("/confirm2faCode")
    public boolean confirm2faCode(@RequestBody Map<String, String> data) {
        return userService.confirm2faCode(data);
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
    
    /**
     * Actualizar perfil del usuario (nombre, apellidos, foto)
     */
    @PutMapping("/{id}/profile")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> updateProfile(@PathVariable String id, @RequestBody Map<String, String> updates) {
        try {
            Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);
            if (!optionalUsuario.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            Usuario usuario = optionalUsuario.get();
            logger.info("🔍 Usuario antes de actualizar perfil - sesionstoken count: {}", (usuario.sesionstoken != null ? usuario.sesionstoken.size() : "null"));
            // Actualizar campos si están presentes
            if (updates.containsKey(NOMBRE)) {
                usuario.setNombre(updates.get(NOMBRE));
            }
            if (updates.containsKey(APELLIDOS)) {
                usuario.setApellidos(updates.get(APELLIDOS));
            }
            if (updates.containsKey("foto")) {
                usuario.setFoto(updates.get("foto"));
            }
            logger.info("💾 Guardando usuario - sesionstoken count: {}", (usuario.sesionstoken != null ? usuario.sesionstoken.size() : "null"));
            // Guardar en MongoDB
            Usuario updatedUsuario = usuarioRepository.save(usuario);
            logger.info("✅ Usuario guardado - sesionstoken count: {}", (updatedUsuario.sesionstoken != null ? updatedUsuario.sesionstoken.size() : "null"));
            // Construir respuesta con _class incluido (igual que en login)
            Map<String, Object> response = new HashMap<>();
            response.put("_id", updatedUsuario.getId());
            response.put(NOMBRE, updatedUsuario.getNombre());
            response.put(APELLIDOS, updatedUsuario.getApellidos());
            response.put(EMAIL, updatedUsuario.getEmail());
            response.put("foto", updatedUsuario.getFoto());
            response.put(BLOQUEADO, updatedUsuario.isBloqueado());
            response.put("_class", updatedUsuario.getClass().getName());
            if (updatedUsuario instanceof Administrador administrador) {
                response.put(DEPARTAMENTO, administrador.getDepartamento());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
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
                
                long duration = System.currentTimeMillis() - startTime;
                response.put("mensaje", "Usuario eliminado correctamente");
                response.put("tiempoEjecucion", duration + "ms");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error al eliminar usuario: {}", e.getMessage(), e);
            response.put("error", "Error al eliminar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
    
