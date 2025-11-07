package iso25.g05.esi_media.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iso25.g05.esi_media.dto.CrearGestorRequest;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.service.GestorService;

import com.mongodb.client.MongoCollection;

import iso25.g05.esi_media.dto.CrearGestorRequest;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.service.UserService;

@RestController
@RequestMapping("/gestores")
@CrossOrigin(origins = "*")
public class GestorController {
    
    private static final Logger logger = LoggerFactory.getLogger(GestorController.class);
    private final GestorService gestorService;

    public GestorController(GestorService gestorService) {
        this.gestorService = gestorService;
    }
    
    /**
     * Endpoint para crear Gestores de Contenido
     * @param request Datos del gestor a crear
     * @return Respuesta con el gestor creado
     */
    @PostMapping("/crear")
    public ResponseEntity<GestordeContenido> crearGestor(@RequestBody CrearGestorRequest request) {
        logger.info("=== CREACIÓN GESTOR DE CONTENIDO ===");
        logger.info("Datos recibidos: {} {} - Alias: {}", 
                   request.getNombre(), request.getApellidos(), request.getAlias());
        
        try {
            GestordeContenido nuevoGestor = gestorService.crearGestor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoGestor);
        } catch (RuntimeException e) {
            logger.error("Error al crear gestor de contenido: {}", e.getMessage());
            throw e;
        }
    }
}
