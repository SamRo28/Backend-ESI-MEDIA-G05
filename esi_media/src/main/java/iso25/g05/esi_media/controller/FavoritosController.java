package iso25.g05.esi_media.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.service.VisualizadorService;

/**
 * Controlador para gestionar la lista de favoritos de un visualizador.
 */
@RestController
@RequestMapping("/api/favoritos")
public class FavoritosController {

    @Autowired
    private VisualizadorService visualizadorService;

    @GetMapping
    public ResponseEntity<List<ContenidoResumenDTO>> listarFavoritos(@CookieValue(value = "SESSION_TOKEN", required = false) String token) {
        return ResponseEntity.ok(visualizadorService.obtenerFavoritos(token));
    }

    @PostMapping("/{contenidoId}")
    public ResponseEntity<Map<String, String>> agregarFavorito(@CookieValue(value = "SESSION_TOKEN", required = false) String token,
            @PathVariable String contenidoId) {
        visualizadorService.agregarFavorito(token, contenidoId);
        return ResponseEntity.ok(Map.of("mensaje", "Contenido añadido a favoritos"));
    }

    @DeleteMapping("/{contenidoId}")
    public ResponseEntity<Map<String, String>> eliminarFavorito(@CookieValue(value = "SESSION_TOKEN", required = false) String token,
            @PathVariable String contenidoId) {
        visualizadorService.eliminarFavorito(token, contenidoId);
        return ResponseEntity.ok(Map.of("mensaje", "Contenido eliminado de favoritos"));
    }
}
