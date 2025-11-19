package iso25.g05.esi_media.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iso25.g05.esi_media.dto.ContenidoDetalleDTO;
import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.dto.ContenidoUpdateDTO;
import iso25.g05.esi_media.service.GestorContenidoService;
import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de contenidos por parte de Gestores.
 *
 * Rutas:
 *  - GET    /gestor/contenidos               Listado paginado de contenidos
 *  - GET    /gestor/contenidos/{id}         Detalle de un contenido
 *  - PUT    /gestor/contenidos/{id}         Actualización de campos editables
 *  - DELETE /gestor/contenidos/{id}         Eliminación de contenido
 */
@RestController
@RequestMapping("/gestor/contenidos")
public class GestorContenidoController {

    @Autowired
    private GestorContenidoService gestorContenidoService;

    @GetMapping
    public Page<ContenidoResumenDTO> listar(
            Pageable pageable,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "query", required = false) String query) {

        return gestorContenidoService.listar(token, pageable, tipo, query);
    }

    @GetMapping("/{id}")
    public ContenidoDetalleDTO detalle(
            @PathVariable String id,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token) {

        return gestorContenidoService.detalle(id, token);
    }

    @PutMapping("/{id}")
    public ContenidoDetalleDTO actualizar(
            @PathVariable String id,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token,
            @Valid @RequestBody ContenidoUpdateDTO dto) {

        return gestorContenidoService.actualizar(id, dto, token);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String id,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token) {

        gestorContenidoService.eliminar(id, token);
        return ResponseEntity.noContent().build();
    }
}

