package iso25.g05.esi_media.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Controlador para la gestión de perfiles de usuario (vista admin).
 * Implementa la historia: "Acceder a cualquier perfil".
 */
@RestController
@RequestMapping("/perfiles")
public class PerfilAdminController {

    private static final Logger logger = LoggerFactory.getLogger(PerfilAdminController.class);

    private final UsuarioRepository usuarioRepository;
    private final LogService logService;

    public PerfilAdminController(UsuarioRepository usuarioRepository, LogService logService) {
        this.usuarioRepository = usuarioRepository;
        this.logService = logService;
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<Object> obtenerPerfil(
            @PathVariable String usuarioId,
            @RequestHeader(value = "Admin-ID", required = false) String adminId) {

        try {
            if (adminId == null || adminId.isEmpty()) {
                logger.warn("Intento de acceso sin Admin-ID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(crearRespuestaError("No autorizado. Se requiere identificación de administrador."));
            }

            Optional<Usuario> adminOpt = usuarioRepository.findById(adminId);
            if (adminOpt.isEmpty() || !(adminOpt.get() instanceof Administrador)) {
                logService.registrarAccesoNoAutorizado(adminId, "Perfil usuario: " + usuarioId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(crearRespuestaError("Acceso denegado. Solo administradores pueden consultar perfiles."));
            }

            Administrador admin = (Administrador) adminOpt.get();

            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                logService.registrarErrorConsulta(adminId, usuarioId, "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearRespuestaError("Usuario no encontrado"));
            }

            Usuario usuario = usuarioOpt.get();
            PerfilDTO perfil = crearPerfilDTO(usuario);

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

    private PerfilDTO crearPerfilDTO(Usuario usuario) {
        String id = usuario.getId();
        String nombre = usuario.getNombre();
        String apellidos = usuario.getApellidos();
        String email = usuario.getEmail();
        Object foto = usuario.getFoto();
        boolean bloqueado = usuario.isBloqueado();
        Date fechaRegistro = usuario.getFechaRegistro();

        if (usuario instanceof Administrador admin) {
            return new PerfilDTO.Builder()
                    .id(id)
                    .nombre(nombre)
                    .apellidos(apellidos)
                    .email(email)
                    .foto(foto)
                    .bloqueado(bloqueado)
                    .rol("Administrador")
                    .departamento(admin.getDepartamento())
                    .fechaRegistro(fechaRegistro)
                    .build();
        } else if (usuario instanceof GestordeContenido gestor) {
            return new PerfilDTO.Builder()
                    .id(id)
                    .nombre(nombre)
                    .apellidos(apellidos)
                    .email(email)
                    .foto(foto)
                    .bloqueado(bloqueado)
                    .rol("Gestor")
                    .alias(gestor.getalias())
                    .descripcion(gestor.getdescripcion())
                    .especialidad(gestor.getcampoespecializacion())
                    .tipoContenido(gestor.gettipocontenidovideooaudio())
                    .fechaRegistro(fechaRegistro)
                    .build();
        } else if (usuario instanceof Visualizador visualizador) {
            Integer edad = calcularEdad(visualizador.getFechaNac());
            return new PerfilDTO.Builder()
                    .id(id)
                    .nombre(nombre)
                    .apellidos(apellidos)
                    .email(email)
                    .foto(foto)
                    .bloqueado(bloqueado)
                    .rol("Visualizador")
                    .alias(visualizador.getAlias())
                    .fechaNacimiento(visualizador.getFechaNac())
                    .vip(visualizador.isVip())
                    .edad(edad)
                    .fechaRegistro(fechaRegistro)
                    .build();
        } else {
            return new PerfilDTO.Builder()
                    .id(id)
                    .nombre(nombre)
                    .apellidos(apellidos)
                    .email(email)
                    .foto(foto)
                    .bloqueado(bloqueado)
                    .rol("Usuario")
                    .fechaRegistro(fechaRegistro)
                    .build();
        }
    }

    private Integer calcularEdad(Date fechaNacimiento) {
        if (fechaNacimiento == null) return null;
        LocalDate fechaNac = fechaNacimiento.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate ahora = LocalDate.now();
        return Period.between(fechaNac, ahora).getYears();
    }

    private Map<String, String> crearRespuestaError(String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return error;
    }
}
