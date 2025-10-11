package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.Gestor_de_Contenido;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.AdministradorRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private AdministradorRepository administradorRepository;

    /**
     * Obtener todos los usuarios - Endpoint compatible con frontend
     */
    @GetMapping("/listar")
    public ResponseEntity<?> listarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            
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
        Map<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("id", usuario.getId());
        usuarioMap.put("nombre", usuario.getNombre());
        usuarioMap.put("apellidos", usuario.getApellidos());
        usuarioMap.put("email", usuario.getEmail());
        usuarioMap.put("foto", null); // Por ahora null, acceder vía getter si existe
        usuarioMap.put("bloqueado", usuario.isBloqueado());
        
        // Determinar el rol según el tipo de usuario
        String rol = "Visualizador"; // Por defecto
        String departamento = "";
        
        if (usuario instanceof Administrador admin) {
            rol = "Administrador";
            departamento = admin.get_departamento();
        } else if (usuario instanceof Gestor_de_Contenido gestor) {
            rol = "Gestor";
            usuarioMap.put("apodo", gestor.get_alias());
        } else if (usuario instanceof Visualizador visualizador) {
            usuarioMap.put("apodo", visualizador.getAlias());
        }
        
        usuarioMap.put("rol", rol);
        usuarioMap.put("departamento", departamento);
        
        return usuarioMap;
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
            
            // Crear administrador vacío y configurarlo manualmente
            Administrador nuevoAdmin = new Administrador();
            
            // Configurar propiedades básicas
            nuevoAdmin.setNombre(nombre);
            nuevoAdmin.setApellidos(apellidos);
            nuevoAdmin.setEmail(email);
            nuevoAdmin.setBloqueado(false);
            nuevoAdmin.set_departamento(departamento != null ? departamento : "General");
            nuevoAdmin.setTipoAdministrador(Administrador.TipoAdministrador.ADMINISTRADOR);
            
            // Crear contraseña completamente limpia
            Contrasenia contraseniaObj = new Contrasenia();
            contraseniaObj.setContraseniaActual(contrasenia);
            // NO asignar usuario a la contraseña para evitar referencia circular
            
            // Asignar contraseña al administrador
            nuevoAdmin._contrasenia = contraseniaObj;
            
            // Inicializar listas vacías para evitar null pointer
            nuevoAdmin._codigos_recuperacion_ = new ArrayList<>();
            nuevoAdmin.sesions_token_ = new ArrayList<>();
            
            System.out.println("Guardando administrador limpio en base de datos...");
            Administrador adminGuardado = usuarioRepository.save(nuevoAdmin);
            System.out.println("Administrador guardado exitosamente con ID: " + adminGuardado.getId());
            
            // Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", adminGuardado.getId());
            response.put("nombre", adminGuardado.getNombre());
            response.put("apellidos", adminGuardado.getApellidos());
            response.put("email", adminGuardado.getEmail());
            response.put("rol", "Administrador");
            response.put("departamento", adminGuardado.get_departamento());
            response.put("bloqueado", adminGuardado.isBloqueado());
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