package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.dto.PerfilDTO;
import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.service.LogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para la gestión de perfiles de usuario
 * Implementa la historia de usuario: "Acceder a cualquier perfil"
 * 
 * Permite a los administradores consultar los perfiles de cualquier usuario
 * del sistema en modo solo lectura, con auditoría de las consultas.
 */
@RestController
@RequestMapping("/perfiles")
@CrossOrigin(origins = "*")
public class PerfilController {
    
    private final UsuarioRepository usuarioRepository;
    private final LogService logService;
    
    public PerfilController(UsuarioRepository usuarioRepository, LogService logService) {
        this.usuarioRepository = usuarioRepository;
        this.logService = logService;
    }
    
    /**
     * Obtiene el perfil de un usuario por su ID
     * Solo accesible por administradores
     * Registra la consulta para trazabilidad
     * 
     * @param usuarioId ID del usuario a consultar
     * @param adminId ID del administrador que realiza la consulta (header)
     * @return PerfilDTO con la información del usuario según su tipo
     */
    @GetMapping("/{usuarioId}")
    public ResponseEntity<Object> obtenerPerfil(
            @PathVariable String usuarioId,
            @RequestHeader(value = "Admin-ID", required = false) String adminId) {
        
        try {
            // Validar que se proporcione el ID del administrador
            if (adminId == null || adminId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(crearRespuestaError("No autorizado. Se requiere identificación de administrador."));
            }
            
            // Verificar que el administrador existe
            Optional<Usuario> adminOpt = usuarioRepository.findById(adminId);
            if (adminOpt.isEmpty() || !(adminOpt.get() instanceof Administrador)) {
                logService.registrarAccesoNoAutorizado(adminId, "Perfil usuario: " + usuarioId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(crearRespuestaError("Acceso denegado. Solo administradores pueden consultar perfiles."));
            }
            
            Administrador admin = (Administrador) adminOpt.get();
            
            // Buscar el usuario solicitado
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                logService.registrarErrorConsulta(adminId, usuarioId, "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError("Usuario no encontrado"));
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Crear el DTO según el tipo de usuario
            PerfilDTO perfil = crearPerfilDTO(usuario);
            
            // Registrar la consulta para auditoría
            logService.registrarConsultaPerfil(
                adminId, 
                admin.getEmail(), 
                usuarioId, 
                usuario.getEmail(),
                perfil.getRol()
            );
            
            return ResponseEntity.ok(perfil);
            
        } catch (Exception e) {
            logService.registrarErrorConsulta(adminId, usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(crearRespuestaError("Error al obtener el perfil: " + e.getMessage()));
        }
    }
    
    /**
     * Crea un PerfilDTO según el tipo de usuario
     * @param usuario Usuario del cual crear el perfil
     * @return PerfilDTO con la información correspondiente
     */
    private PerfilDTO crearPerfilDTO(Usuario usuario) {
        String id = usuario.getId();
        String nombre = usuario.getNombre();
        String apellidos = usuario.getApellidos();
        String email = usuario.getEmail();
        Object foto = usuario.getFoto();
        boolean bloqueado = usuario.isBloqueado();
        Date fechaRegistro = usuario.getFechaRegistro();
        
        if (usuario instanceof Administrador admin) {
            return new PerfilDTO(
                id, nombre, apellidos, email, foto, bloqueado,
                admin.getDepartamento(),
                fechaRegistro
            );
            
        } else if (usuario instanceof GestordeContenido gestor) {
            return new PerfilDTO(
                id, nombre, apellidos, email, foto, bloqueado,
                gestor.getalias(),
                gestor.getdescripcion(),
                gestor.getcampoespecializacion(),
                gestor.gettipocontenidovideooaudio(),
                fechaRegistro
            );
            
        } else if (usuario instanceof Visualizador visualizador) {
            Integer edad = calcularEdad(visualizador.getFechaNac());
            return new PerfilDTO(
                id, nombre, apellidos, email, foto, bloqueado,
                visualizador.getAlias(),
                visualizador.getFechaNac(),
                visualizador.isVip(),
                edad,
                fechaRegistro
            );
            
        } else {
            // Usuario genérico (por si acaso)
            PerfilDTO perfil = new PerfilDTO();
            perfil.setId(id);
            perfil.setNombre(nombre);
            perfil.setApellidos(apellidos);
            perfil.setEmail(email);
            perfil.setFoto(foto);
            perfil.setBloqueado(bloqueado);
            perfil.setRol("Usuario");
            perfil.setFechaRegistro(fechaRegistro);
            return perfil;
        }
    }
    
    /**
     * Calcula la edad a partir de una fecha de nacimiento
     * @param fechaNacimiento Fecha de nacimiento
     * @return Edad en años, o null si la fecha es null
     */
    private Integer calcularEdad(Date fechaNacimiento) {
        if (fechaNacimiento == null) {
            return null;
        }
        
        LocalDate fechaNac = fechaNacimiento.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
        LocalDate ahora = LocalDate.now();
        
        return Period.between(fechaNac, ahora).getYears();
    }
    
    /**
     * Crea un mapa con un mensaje de error
     * @param mensaje Mensaje de error
     * @return Map con el error
     */
    private Map<String, String> crearRespuestaError(String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return error;
    }
}
