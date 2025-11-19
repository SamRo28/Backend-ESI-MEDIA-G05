package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.model.*;
import iso25.g05.esi_media.repository.*;
import iso25.g05.esi_media.service.LogService;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CookieValue;

import java.util.*;

@RestController
@RequestMapping("/contenidos")
public class ContenidoAdminController {

    private final UsuarioRepository usuarioRepository;
    private final ContenidoRepository contenidoRepository; // Videos base (colecciÃƒÂ³n contenidos)
    private final VideoRepository videoRepository;         // Videos especÃƒÂ­ficos (colecciÃƒÂ³n videos)
    private final AudioRepository audioRepository;         // Audios (colecciÃƒÂ³n audios)
    private final GestorDeContenidoRepository gestorRepository;
    private final LogService logService;

    private String ERROR = "error";
    private String TITULO = "titulo";
    private String VIDEO = "Video";
    private String GN = "gestorNombre";
    private String AUDIO = "Audio";
    private String DESC = "descripcion";
    private String DUR = "duracion";


    public ContenidoAdminController(UsuarioRepository usuarioRepository,
                                    ContenidoRepository contenidoRepository,
                                    VideoRepository videoRepository,
                                    AudioRepository audioRepository,
                                    GestorDeContenidoRepository gestorRepository,
                                    LogService logService) {
        this.usuarioRepository = usuarioRepository;
        this.contenidoRepository = contenidoRepository;
        this.videoRepository = videoRepository;
        this.audioRepository = audioRepository;
        this.gestorRepository = gestorRepository;
        this.logService = logService;
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listar(@RequestHeader(value = "Admin-ID", required = false) String adminId) {
        if (adminId == null || adminId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(ERROR, "No autorizado. Se requiere Admin-ID"));
        }
        // Validar formato de ID para evitar 500 si no es ObjectId
        if (!ObjectId.isValid(adminId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR, "Admin-ID invÃƒÂ¡lido"));
        }

        Optional<Usuario> adminOpt = usuarioRepository.findById(adminId);
        if (adminOpt.isEmpty() || !(adminOpt.get() instanceof Administrador)) {
            logService.registrarAccesoNoAutorizado(adminId, "Listado contenidos");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(ERROR, "Acceso denegado. Solo administradores"));
        }

        List<Map<String, Object>> resultado = new ArrayList<>();

        // Videos (colecciÃƒÂ³n 'contenidos')
        try {
            for (Contenido c : contenidoRepository.findAll()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", c.getId());
                item.put(TITULO, c.gettitulo());
                item.put("tipo", VIDEO);
                item.put(GN, resolverGestorNombre(c.getgestorId()));
                resultado.add(item);
            }
        } catch (Exception e) {
            System.err.println("[ContenidoAdminController] Error listando videos: " + e.getMessage());
        }

        // Videos (colecciÃƒÂ³n 'videos')
        try {
            for (Video v : videoRepository.findAll()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", v.getId());
                item.put(TITULO, v.gettitulo());
                item.put("tipo", VIDEO);
                item.put(GN, resolverGestorNombre(v.getgestorId()));
                resultado.add(item);
            }
        } catch (Exception e) {
            System.err.println("[ContenidoAdminController] Error listando videos (colecciÃƒÂ³n videos): " + e.getMessage());
        }

        // Audios (colecciÃƒÂ³n 'audios')
        try {
            for (Audio a : audioRepository.findAll()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", a.getId());
                item.put(TITULO, a.gettitulo());
                item.put("tipo", AUDIO);
                item.put(GN, resolverGestorNombre(a.getgestorId()));
                resultado.add(item);
            }
        } catch (Exception e) {
            System.err.println("[ContenidoAdminController] Error listando audios: " + e.getMessage());
        }

                System.out.println("[ContenidoAdminController] Total contenidos a devolver: " + resultado.size());
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable String id,
                                     @RequestHeader(value = "Admin-ID", required = false) String adminId) {
        if (adminId == null || adminId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(ERROR, "No autorizado. Se requiere Admin-ID"));
        }
        Optional<Usuario> adminOpt = usuarioRepository.findById(adminId);
        if (adminOpt.isEmpty() || !(adminOpt.get() instanceof Administrador)) {
            logService.registrarAccesoNoAutorizado(adminId, "Detalle contenido: " + id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(ERROR, "Acceso denegado. Solo administradores"));
        }

        // Buscar primero en videos (colecciÃƒÂ³n contenidos)
        Optional<Contenido> cOpt = contenidoRepository.findById(id);
        if (cOpt.isPresent()) {
            Contenido c = cOpt.get();
            return ResponseEntity.ok(mapearDetalle(c, VIDEO));
        }

        // Luego en audios
        Optional<Audio> aOpt = audioRepository.findById(id);
        if (aOpt.isPresent()) {
            Audio a = aOpt.get();
            return ResponseEntity.ok(mapearDetalle(a, AUDIO));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(ERROR, "Contenido no encontrado"));
    }

    private Map<String, Object> mapearDetalle(Contenido c, String tipo) {
        Map<String, Object> d = new HashMap<>();
        d.put("id", c.getId());
        d.put(TITULO, c.gettitulo());
        d.put("tipo", tipo);
        d.put(DESC, c.getdescripcion());
        d.put(DUR, c.getduracion());
        if (c instanceof Video v) {
            d.put("resolucion", v.getresolucion());
            d.put("url", v.geturl());
        }
        d.put("vip", c.isvip());
        d.put("edadMinima", c.getedadvisualizacion());
        d.put("gestorId", c.getgestorId());
        d.put(GN, resolverGestorNombre(c.getgestorId()));
        return d;
    }

    /**
     * Busca contenidos por titulo para usuarios normales (para crear listas)
     * No requiere Admin-ID, solo token de usuario válido
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarContenidos(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token) {
        
        // Validar que el token esté presente (básico)
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(ERROR, "Token de autorización requerido"));
        }
        
        // Validar query
        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR, "El query debe tener al menos 2 caracteres"));
        }
        
        // Limitar resultados
        if (limit <= 0 || limit > 50) limit = 10;
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        String queryLower = query.toLowerCase();
        
        try {
            // Buscar en contenidos (videos base) usando query optimizada
            List<Contenido> contenidos = contenidoRepository.findByTituloContainingIgnoreCase(query);
            contenidos.stream()
                .limit(limit)
                .forEach(c -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", c.getId());
                    item.put(TITULO, c.gettitulo());
                    item.put("tipo", VIDEO);
                    item.put(DESC, c.getdescripcion());
                    item.put(DUR, c.getduracion());
                    resultado.add(item);
                });
                
            // Si necesitamos más resultados, buscar en videos específicos
            if (resultado.size() < limit) {
                int remaining = limit - resultado.size();
                // Nota: VideoRepository y AudioRepository necesitarían métodos similares
                // Por simplicidad, usamos findAll con filtro por ahora
                videoRepository.findAll().stream()
                    .filter(v -> v.gettitulo() != null && 
                               v.gettitulo().toLowerCase().contains(queryLower))
                    .limit(remaining)
                    .forEach(v -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", v.getId());
                        item.put(TITULO, v.gettitulo());
                        item.put("tipo", VIDEO);
                        item.put(DESC, v.getdescripcion());
                        item.put(DUR, v.getduracion());
                        item.put("resolucion", v.getresolucion());
                        resultado.add(item);
                    });
            }
            
            // Si necesitamos más resultados, buscar en audios
            if (resultado.size() < limit) {
                int remaining = limit - resultado.size();
                audioRepository.findAll().stream()
                    .filter(a -> a.gettitulo() != null && 
                               a.gettitulo().toLowerCase().contains(queryLower))
                    .limit(remaining)
                    .forEach(a -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", a.getId());
                        item.put(TITULO, a.gettitulo());
                        item.put("tipo", AUDIO);
                        item.put(DESC, a.getdescripcion());
                        item.put(DUR, a.getduracion());
                        resultado.add(item);
                    });
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Búsqueda completada exitosamente");
            response.put("contenidos", resultado);
            response.put("total", resultado.size());
            response.put("query", query);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR, "Error interno del servidor"));
        }
    }

    private String resolverGestorNombre(String gestorId) {
        if (gestorId == null) return null;
        return gestorRepository.findById(gestorId)
                .map(g -> g.getNombre() + (g.getApellidos() != null ? (" " + g.getApellidos()) : ""))
                .orElse(null);
    }
}
