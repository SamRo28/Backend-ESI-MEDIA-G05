package iso25.g05.esi_media.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iso25.g05.esi_media.service.VisualizadorService;

/**
 * Controlador REST para la gestión del perfil del usuario.
 */
@RestController
@RequestMapping("/perfiles")
@RequestMapping("/api/perfil")
@CrossOrigin(origins = "*")
public class PerfilController {

    @Autowired
    private VisualizadorService visualizadorService;

    /**
     * DELETE /api/perfil/me
     * Permite a un usuario autenticado eliminar su propia cuenta.
     *
     * @param authHeader cabecera Authorization con el token de sesión.
     * @return Respuesta de éxito o error.
     */
    @DeleteMapping("/me")
    public ResponseEntity<?> eliminarMiCuenta(@RequestHeader("Authorization") String authHeader) {
        visualizadorService.eliminarMiCuenta(authHeader);
        return ResponseEntity.ok(Map.of("mensaje", "Cuenta eliminada correctamente"));
    }

}