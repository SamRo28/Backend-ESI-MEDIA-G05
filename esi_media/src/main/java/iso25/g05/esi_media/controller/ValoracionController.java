package iso25.g05.esi_media.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iso25.g05.esi_media.dto.CreateValoracionDTO;
import iso25.g05.esi_media.dto.ValorarDTO;
import iso25.g05.esi_media.model.Valoracion;
import iso25.g05.esi_media.dto.ShowRatingDTO;
import iso25.g05.esi_media.dto.AverageRatingDTO;
import iso25.g05.esi_media.repository.ValoracionRepository;
import iso25.g05.esi_media.service.TokenForValoracionService;
import iso25.g05.esi_media.service.ValoracionService;

@RestController
@RequestMapping("/api/valoraciones")
public class ValoracionController {

    private final ValoracionService valoracionService;
    private final ValoracionRepository valoracionRepository;
    private final TokenForValoracionService tokenForValoracionService;

    @Autowired
    public ValoracionController(ValoracionService valoracionService,
                                ValoracionRepository valoracionRepository,
                                TokenForValoracionService tokenForValoracionService) {
        this.valoracionService = valoracionService;
        this.valoracionRepository = valoracionRepository;
        this.tokenForValoracionService = tokenForValoracionService;
    }

    // Crea la instancia de clase asociación Valoracion con valor null o devuelve la existente.
    @PostMapping
    public ResponseEntity<Valoracion> createOrGet(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                   @RequestBody CreateValoracionDTO dto) {
        String usuarioId = tokenForValoracionService.resolveUsuarioIdFromAuth(authHeader);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Valoracion v = valoracionService.registerPlay(usuarioId, dto.getContenidoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(v);
    }

    // Obtiene la Valoracion por visualizadorId y contenidoId
    @GetMapping
    public ResponseEntity<Valoracion> getByPair(@RequestParam(name = "visualizadorId", required = false) String visualizadorId,
                                                @RequestParam(name = "contenidoId", required = false) String contenidoId) {
        if (visualizadorId != null && contenidoId != null) {
            Optional<Valoracion> v = valoracionRepository.findByVisualizadorIdAndContenidoId(visualizadorId, contenidoId);
            return v.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        }
        return ResponseEntity.badRequest().build();
    }

    // Muestra la Valoracion para un contenido y estado del usuario
    @GetMapping("/show")
    public ResponseEntity<ShowRatingDTO> showRating(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                     @RequestParam(name = "contenidoId") String contenidoId) {
        String usuarioId = tokenForValoracionService.resolveUsuarioIdFromAuth(authHeader);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ShowRatingDTO dto = valoracionService.showRating(usuarioId, contenidoId);
        // dto.getMyRating() == null puede significar dos cosas: no existe instancia o existe pero no valoró.
        // Para diferenciarlas, consultamos la instancia directamente.
        var opt = valoracionService.getMyValoracionInstance(usuarioId, contenidoId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (dto.getMyRating() == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }

    // Obtiene promedio de valoraciones para un contenido
    @GetMapping("/average")
    public ResponseEntity<AverageRatingDTO> average(@RequestParam(name = "contenidoId") String contenidoId) {
        AverageRatingDTO dto = valoracionService.getAverageRating(contenidoId);
        return ResponseEntity.ok(dto);
    }

    // Obtiene únicamente MI valoración para un contenido (si existe)
    @GetMapping("/my")
    public ResponseEntity<Double> myRating(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @RequestParam(name = "contenidoId") String contenidoId) {
        String usuarioId = tokenForValoracionService.resolveUsuarioIdFromAuth(authHeader);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var opt = valoracionService.getMyValoracionInstance(usuarioId, contenidoId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Valoracion v = opt.get();
        if (v.getValoracionFinal() == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(v.getValoracionFinal());
    }

    // Realiza la valoración (modifica el valor de una instancia existente de un Visualizador y un Contenido)
    @PostMapping("/{id}/valorar")
    public ResponseEntity<Void> valorarPorId(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @PathVariable("id") String id,
                                             @RequestBody ValorarDTO dto) {
        String usuarioId = tokenForValoracionService.resolveUsuarioIdFromAuth(authHeader);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Valoracion> vOpt = valoracionRepository.findById(id);
        if (vOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Valoracion v = vOpt.get();
        if (!usuarioId.equals(v.getVisualizadorId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        valoracionService.rateContent(usuarioId, v.getContenidoId(), dto.getValoracion());
        return ResponseEntity.noContent().build();
    }
}
