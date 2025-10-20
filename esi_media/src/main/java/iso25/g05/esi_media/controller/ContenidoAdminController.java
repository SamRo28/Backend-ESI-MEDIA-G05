package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.model.*;
import iso25.g05.esi_media.repository.*;
import iso25.g05.esi_media.service.LogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/contenidos")
@CrossOrigin(origins = "*")
public class ContenidoAdminController {

    private final UsuarioRepository usuarioRepository;
    private final ContenidoRepository contenidoRepository; // Videos (colección contenidos)
    private final AudioRepository audioRepository;         // Audios (colección audios)
    private final GestorDeContenidoRepository gestorRepository;
    private final LogService logService;

    public ContenidoAdminController(UsuarioRepository usuarioRepository,
                                    ContenidoRepository contenidoRepository,
                                    AudioRepository audioRepository,
                                    GestorDeContenidoRepository gestorRepository,
                                    LogService logService) {
        this.usuarioRepository = usuarioRepository;
        this.contenidoRepository = contenidoRepository;
        this.audioRepository = audioRepository;
        this.gestorRepository = gestorRepository;
        this.logService = logService;
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listar(@RequestHeader(value = "Admin-ID", required = false) String adminId) {
        if (adminId == null || adminId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autorizado. Se requiere Admin-ID"));
        }

        Optional<Usuario> adminOpt = usuarioRepository.findById(adminId);
        if (adminOpt.isEmpty() || !(adminOpt.get() instanceof Administrador)) {
            logService.registrarAccesoNoAutorizado(adminId, "Listado contenidos");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado. Solo administradores"));
        }

        List<Map<String, Object>> resultado = new ArrayList<>();

        // Videos (almacenados en colección 'contenidos')
        for (Contenido c : contenidoRepository.findAll()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("titulo", c.gettitulo());
            item.put("tipo", "Video");
            item.put("gestorNombre", resolverGestorNombre(c.getgestorId()));
            resultado.add(item);
        }

        // Audios (colección 'audios')
        for (Audio a : audioRepository.findAll()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", a.getId());
            item.put("titulo", a.gettitulo());
            item.put("tipo", "Audio");
            item.put("gestorNombre", resolverGestorNombre(a.getgestorId()));
            resultado.add(item);
        }

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable String id,
                                     @RequestHeader(value = "Admin-ID", required = false) String adminId) {
        if (adminId == null || adminId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autorizado. Se requiere Admin-ID"));
        }
        Optional<Usuario> adminOpt = usuarioRepository.findById(adminId);
        if (adminOpt.isEmpty() || !(adminOpt.get() instanceof Administrador)) {
            logService.registrarAccesoNoAutorizado(adminId, "Detalle contenido: " + id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado. Solo administradores"));
        }

        // Buscar primero en videos (colección contenidos)
        Optional<Contenido> cOpt = contenidoRepository.findById(id);
        if (cOpt.isPresent()) {
            Contenido c = cOpt.get();
            return ResponseEntity.ok(mapearDetalle(c, "Video"));
        }

        // Luego en audios
        Optional<Audio> aOpt = audioRepository.findById(id);
        if (aOpt.isPresent()) {
            Audio a = aOpt.get();
            return ResponseEntity.ok(mapearDetalle(a, "Audio"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Contenido no encontrado"));
    }

    private Map<String, Object> mapearDetalle(Contenido c, String tipo) {
        Map<String, Object> d = new HashMap<>();
        d.put("id", c.getId());
        d.put("titulo", c.gettitulo());
        d.put("tipo", tipo);
        d.put("descripcion", c.getdescripcion());
        d.put("duracion", c.getduracion());
        if (c instanceof Video v) {
            d.put("resolucion", v.getresolucion());
        }
        d.put("estado", c.isestado());
        d.put("fechaEstado", c.getfechaestadoautomatico());
        d.put("fechaDisponibleHasta", c.getfechadisponiblehasta());
        d.put("vip", c.isvip());
        d.put("edadMinima", c.getedadvisualizacion());
        d.put("gestorId", c.getgestorId());
        d.put("gestorNombre", resolverGestorNombre(c.getgestorId()));
        return d;
    }

    private String resolverGestorNombre(String gestorId) {
        if (gestorId == null) return null;
        return gestorRepository.findById(gestorId)
                .map(g -> g.getNombre() + (g.getApellidos() != null ? (" " + g.getApellidos()) : ""))
                .orElse(null);
    }
}

