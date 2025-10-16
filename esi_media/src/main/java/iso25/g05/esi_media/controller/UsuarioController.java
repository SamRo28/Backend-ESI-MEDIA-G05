package iso25.g05.esi_media.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.AdministradorRepository;
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

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private AdministradorRepository administradorRepository;
    
    @Autowired
    private UserService userService;
    
    // ==================== ENDPOINTS DE LOGIN (/users) ====================
    
    /**
     * Login de usuario con email y contraseña
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        Usuario loggedInUser = userService.login(loginData);
        if (loggedInUser == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials");
        
        // Crear un mapa con los datos del usuario y el campo _class
        Map<String, Object> response = new HashMap<>();
        response.put("_id", loggedInUser.getId());
        response.put("nombre", loggedInUser.getNombre());
        response.put("apellidos", loggedInUser.getApellidos());
        response.put("email", loggedInUser.getEmail());
        response.put("foto", loggedInUser.getFoto());
        response.put("bloqueado", loggedInUser.isBloqueado());
        response.put("fecharegistro", loggedInUser.getFechaRegistro());
        response.put("sesionstoken", loggedInUser.sesionstoken);
        response.put("twoFactorAutenticationEnabled", loggedInUser.isTwoFactorAutenticationEnabled());
        response.put("threeFactorAutenticationEnabled", loggedInUser.isThreeFactorAutenticationEnabled());
        
        // Agregar el campo _class con el nombre completo de la clase
        response.put("_class", loggedInUser.getClass().getName());
        
        // Si es administrador, agregar el departamento
        if (loggedInUser instanceof Administrador) {
            response.put("departamento", ((Administrador) loggedInUser).getDepartamento());
        }
        
        System.out.println("📤 Enviando respuesta de login con _class: " + loggedInUser.getClass().getName());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Login con autenticación de 3 factores
     */
    @PostMapping("/login3Auth")
    public void login3Auth(@RequestBody Map<String, String> loginData) {
        userService.login3Auth(loginData);
    }
    
    // ==================== ENDPOINTS DE USUARIOS (/api/usuarios) ====================

    /**
     * Obtener todos los usuarios - Endpoint compatible con frontend
     */
    @GetMapping("/listar")
    public ResponseEntity<?> listarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            
            // DEBUG: Ver qué tipos de objetos estamos recibiendo
            System.out.println("=== DEBUG LISTAR USUARIOS ===");
            for (Usuario u : usuarios) {
                System.out.println("Usuario: " + u.getNombre() + " - Clase: " + u.getClass().getName());
            }
            
            // Convertir usuarios a formato esperado por el frontend
            List<Map<String, Object>> usuariosFormateados = usuarios.stream()
                .map(this::formatearUsuario)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(usuariosFormateados);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al obtener usuarios: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Formatear usuario al formato esperado por el frontend
     */
    private Map<String, Object> formatearUsuario(Usuario usuario) {
        Map<String, Object> usuarioFormateado = new HashMap<>();
        usuarioFormateado.put("id", usuario.getId());
        
        String rol = "Visualizador"; // Por defecto
        if (usuario instanceof Administrador) {
            rol = "Administrador";
            System.out.println("✅ " + usuario.getNombre() + " es Administrador");
        } else if (usuario instanceof GestordeContenido) {
            rol = "Gestor";
            System.out.println("✅ " + usuario.getNombre() + " es Gestor");
        } else if (usuario instanceof Visualizador) {
            rol = "Visualizador";
            System.out.println("✅ " + usuario.getNombre() + " es Visualizador");
        } else {
            System.out.println("⚠️ " + usuario.getNombre() + " - Clase no reconocida: " + usuario.getClass().getName());
        }
        
        usuarioFormateado.put("rol", rol); // Cambiado de "tipo" a "rol"
        usuarioFormateado.put("nombre", usuario.getNombre());
        usuarioFormateado.put("apellidos", usuario.getApellidos());
        usuarioFormateado.put("email", usuario.getEmail());
        usuarioFormateado.put("foto", usuario.getFoto() != null ? usuario.getFoto() : "perfil1.png");
        usuarioFormateado.put("bloqueado", usuario.isBloqueado());
        
        return usuarioFormateado;
    }
    
    /**
     * Registrar un nuevo usuario - Endpoint compatible con frontend
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, Object>> registrarUsuario(@RequestBody Map<String, Object> userData) {
        System.out.println("=== REGISTRO DE USUARIO ===");
        System.out.println("Datos recibidos: " + userData);
        
        try {
            String nombre = (String) userData.get("nombre");
            String apellidos = (String) userData.get("apellidos");
            String email = (String) userData.get("email");
            String contrasenia = (String) userData.get("contrasenia");
            String departamento = (String) userData.get("departamento");
            String rol = (String) userData.get("rol");
            Object foto = userData.get("foto");
            
            System.out.println("Datos extraídos - Nombre: " + nombre + ", Email: " + email + ", Rol: " + rol);
            
            // Verificaciones básicas
            if (nombre == null || apellidos == null || email == null || contrasenia == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("mensaje", "Faltan campos obligatorios");
                System.out.println("ERROR: Faltan campos obligatorios");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Verificar si el email ya existe
            System.out.println("Verificando si el email existe: " + email);
            if (usuarioRepository.existsByEmail(email)) {
                Map<String, Object> error = new HashMap<>();
                error.put("mensaje", "El email ya está registrado");
                System.out.println("ERROR: Email ya existe");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
            
            // SOLUCIÓN DEFINITIVA: Crear administrador completamente limpio
            System.out.println("Creando Administrador limpio sin referencias problemáticas...");
            
            // Crear contraseña completamente limpia
            Contrasenia contraseniaObj = new Contrasenia(
                null, // id
                null, // fecha_expiracion
                contrasenia, // contrasenia_actual
                new ArrayList<>() // contrasenias_usadas vacio
            );
            
            // Crear administrador usando el constructor correcto
            Administrador nuevoAdmin = new Administrador(
                apellidos,
                false, // no bloqueado
                contraseniaObj,
                email,
                null, // foto
                nombre,
                departamento != null ? departamento : "General"
            );
            
            System.out.println("Guardando administrador limpio en base de datos...");
            Administrador adminGuardado = usuarioRepository.save(nuevoAdmin);
            System.out.println("Administrador guardado exitosamente con ID: temp-id"); // + adminGuardado.getId());
            
            // Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", "temp-id"); // adminGuardado.getId());
            response.put("nombre", "temp-nombre"); // adminGuardado.getNombre());
            response.put("apellidos", "temp-apellidos"); // adminGuardado.getApellidos());
            response.put("email", "temp@email.com"); // adminGuardado.getEmail());
            response.put("rol", "Administrador");
            response.put("departamento", "temp-dept"); // adminGuardado.getdepartamento());
            response.put("bloqueado", false); // adminGuardado.isBloqueado());
            response.put("mensaje", "Administrador creado exitosamente");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "Error al crear usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
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
            
            System.out.println("🔍 Usuario antes de actualizar perfil - sesionstoken count: " + 
                (usuario.sesionstoken != null ? usuario.sesionstoken.size() : "null"));
            
            // Actualizar campos si están presentes
            if (updates.containsKey("nombre")) {
                usuario.setNombre(updates.get("nombre"));
            }
            if (updates.containsKey("apellidos")) {
                usuario.setApellidos(updates.get("apellidos"));
            }
            if (updates.containsKey("foto")) {
                usuario.setFoto(updates.get("foto"));
            }
            
            System.out.println("💾 Guardando usuario - sesionstoken count: " + 
                (usuario.sesionstoken != null ? usuario.sesionstoken.size() : "null"));
            
            // Guardar en MongoDB
            Usuario updatedUsuario = usuarioRepository.save(usuario);
            
            System.out.println("✅ Usuario guardado - sesionstoken count: " + 
                (updatedUsuario.sesionstoken != null ? updatedUsuario.sesionstoken.size() : "null"));
            
            // Construir respuesta con _class incluido (igual que en login)
            Map<String, Object> response = new HashMap<>();
            response.put("_id", updatedUsuario.getId());
            response.put("nombre", updatedUsuario.getNombre());
            response.put("apellidos", updatedUsuario.getApellidos());
            response.put("email", updatedUsuario.getEmail());
            response.put("foto", updatedUsuario.getFoto());
            response.put("bloqueado", updatedUsuario.isBloqueado());
            response.put("_class", updatedUsuario.getClass().getName());
            
            // Si es Administrador, incluir departamento
            if (updatedUsuario instanceof Administrador) {
                response.put("departamento", ((Administrador) updatedUsuario).getDepartamento());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar perfil: " + e.getMessage());
        }
    }

    /**
     * Eliminar un usuario
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable String id) {
        try {
            if (usuarioRepository.existsById(id)) {
                usuarioRepository.deleteById(id);
                return ResponseEntity.ok("Usuario eliminado correctamente");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}