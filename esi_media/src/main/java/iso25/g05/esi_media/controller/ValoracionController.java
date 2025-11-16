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

    // Crea la asociación play (valoracion con valor null) o devuelve la existente.
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

    // Obtener por visualizadorId y contenidoId
    @GetMapping
    public ResponseEntity<Valoracion> getByPair(@RequestParam(name = "visualizadorId", required = false) String visualizadorId,
                                                @RequestParam(name = "contenidoId", required = false) String contenidoId) {
        if (visualizadorId != null && contenidoId != null) {
            Optional<Valoracion> v = valoracionRepository.findByVisualizadorIdAndContenidoId(visualizadorId, contenidoId);
            return v.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        }
        return ResponseEntity.badRequest().build();
    }

    // Valorar por id de la Valoracion (requiere que el token corresponda al visualizador)
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
