package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.dto.ContenidoDetalleDTO;
import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.service.MultimediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para reproducción/listado de contenido multimedia por visualizadores.
 *
 * Endpoints:
 *  - GET /multimedia                → lista paginada de ContenidoResumenDTO
 *  - GET /multimedia/{id}          → detalle (ContenidoDetalleDTO)
 *  - GET /multimedia/audio/{id}    → streaming de audio (.mp3) [Paso 6]
 */
@RestController
@RequestMapping("/multimedia")
@CrossOrigin(origins = "*")
public class MultimediaController {

    @Autowired
    private MultimediaService multimediaService;

    /**
     * GET /multimedia
     * Lista contenidos accesibles para el visualizador autenticado (paginado).
     * Requiere header Authorization con token ("Bearer x" o "x").
     */
    @GetMapping
    public ResponseEntity<Page<ContenidoResumenDTO>> listarContenidos(
            Pageable pageable,
            @RequestHeader("Authorization") String authHeader) {

        Page<ContenidoResumenDTO> pagina = multimediaService.listarContenidos(pageable, authHeader);
        return ResponseEntity.ok(pagina);
    }

    /**
     * GET /multimedia/{id}
     * Devuelve detalle del contenido validando edad/VIP/visibilidad.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContenidoDetalleDTO> obtenerDetalle(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {

        ContenidoDetalleDTO detalle = multimediaService.obtenerContenidoPorId(id, authHeader);
        return ResponseEntity.ok(detalle);
    }

    /**
     * GET /multimedia/audio/{id}
     * Endpoint para streaming de audio (.mp3). Se implementa en el Paso 6 con soporte Range.
     * De momento devuelve 501 (Not Implemented) para dejar el contrato creado.
     */
    @GetMapping("/audio/{id}")
    public ResponseEntity<?> streamAudioPendiente(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body("Streaming de audio con rango se implementará en el Paso 6");
    }
}
