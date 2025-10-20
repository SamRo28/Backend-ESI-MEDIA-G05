package iso25.g05.esi_media.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.repository.ContraseniaRepository;

import java.util.Optional;

/**
 * Controlador para operaciones con contraseñas
 */
@RestController
@RequestMapping("/contrasenias")
@CrossOrigin(origins = "*")
public class ContraseniaController {

    @Autowired
    private ContraseniaRepository contraseniaRepository;

    /**
     * Obtener una contraseña por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Contrasenia> getContraseniaPorId(@PathVariable String id) {
        try {
            Optional<Contrasenia> contrasenia = contraseniaRepository.findById(id);
            if (contrasenia.isPresent()) {
                return ResponseEntity.ok(contrasenia.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar una contraseña por su ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarContrasenia(@PathVariable String id) {
        try {
            if (contraseniaRepository.existsById(id)) {
                contraseniaRepository.deleteById(id);
                return ResponseEntity.ok("Contraseña eliminada correctamente");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar la contraseña: " + e.getMessage());
        }
    }
}