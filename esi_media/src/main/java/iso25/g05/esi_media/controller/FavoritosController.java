package iso25.g05.esi_media.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.service.VisualizadorService;

/**
 * Controlador para gestionar la lista de favoritos de un visualizador.
 */
@RestController
@RequestMapping("/api/favoritos")
@CrossOrigin(origins = "*")
public class FavoritosController {

    @Autowired
    private VisualizadorService visualizadorService;

    @GetMapping
    public ResponseEntity<List<ContenidoResumenDTO>> listarFavoritos(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(visualizadorService.obtenerFavoritos(authHeader));
    }

    @PostMapping("/{contenidoId}")
    public ResponseEntity<Map<String, String>> agregarFavorito(@RequestHeader("Authorization") String authHeader,
            @PathVariable String contenidoId) {
        visualizadorService.agregarFavorito(authHeader, contenidoId);
        return ResponseEntity.ok(Map.of("mensaje", "Contenido añadido a favoritos"));
    }

    @DeleteMapping("/{contenidoId}")
    public ResponseEntity<Map<String, String>> eliminarFavorito(@RequestHeader("Authorization") String authHeader,
            @PathVariable String contenidoId) {
        visualizadorService.eliminarFavorito(authHeader, contenidoId);
        return ResponseEntity.ok(Map.of("mensaje", "Contenido eliminado de favoritos"));
    }
}
