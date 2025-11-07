package iso25.g05.esi_media.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iso25.g05.esi_media.dto.CrearAdministradorRequest;
import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.service.AdministradorService;

@RestController
@RequestMapping("/administradores")
@CrossOrigin(origins = "*")
public class AdministradorController {
    
    @Autowired
    private final AdministradorService administradorService;

    public AdministradorController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }
    
    
    /**
     * Endpoint simplificado para crear administradores (sin autenticación requerida)
     * @param request Datos del administrador a crear
     * @return Respuesta con el administrador creado
     */
    @PostMapping("/crear-simple")
    public ResponseEntity<Object> crearAdministradorSimple(@RequestBody CrearAdministradorRequest request) {
        
        try {
            Administrador nuevoAdmin = administradorService.crearAdministradorSimple(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAdmin);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e);
            
        }
    }
}