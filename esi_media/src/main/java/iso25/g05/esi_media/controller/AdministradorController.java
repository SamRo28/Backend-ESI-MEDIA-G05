package iso25.g05.esi_media.controller;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;

import iso25.g05.esi_media.dto.CrearAdministradorRequest;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.service.UserService;

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
            logger.info("Procesando: {} {} - {}", request.getNombre(), request.getApellidos(), request.getEmail());
            
            // Paso 1: Crear documento de contraseña en la colección 'contrasenias'
            MongoCollection<Document> contraseniasCollection = mongoTemplate.getCollection("contrasenias");
            
            // Calcular fecha de expiración (1 año desde ahora)
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 1);
            Date fechaExpiracion = cal.getTime();
            
            Contrasenia c = new Contrasenia(
            null, 
            fechaExpiracion,
            request.getContrasenia(),
            new ArrayList<>()
            );

            Contrasenia contrasenia = userService.hashearContrasenia(c);

            Document contraseniaDoc = new Document()
                .append("fecha_expiracion", fechaExpiracion)
                .append("contrasenia_actual", contrasenia.getContraseniaActual())
                .append("contrasenia_usadas", new ArrayList<>())
                .append("_class", "iso25.g05.esi_media.model.Contrasenia");
            
            logger.info("📝 Base de datos actual: {}", mongoTemplate.getDb().getName());
            logger.info("📝 Colección: {}", contraseniasCollection.getNamespace());
            if (logger.isInfoEnabled()) {
                logger.info("📝 Insertando contraseña: {}", contraseniaDoc.toJson());
            }
            contraseniasCollection.insertOne(contraseniaDoc);
            ObjectId contraseniaObjectId = contraseniaDoc.getObjectId("_id");
            String contraseniaId = contraseniaObjectId.toString();
            logger.info("✅ Contraseña insertada en BD con _id: {}", contraseniaObjectId);
            logger.info("✅ String ID para DBRef: {}", contraseniaId);
            
            // Paso 2: Crear DBRef para la contraseña
            com.mongodb.DBRef contraseniaRef = new com.mongodb.DBRef("contrasenias", new org.bson.types.ObjectId(contraseniaId));
            
            // Paso 3: Crear documento de usuario con referencia a contraseña
            MongoCollection<Document> usersCollection = mongoTemplate.getCollection("users");
            
            Document adminDoc = new Document()
                .append("departamento", request.getDepartamento())
                .append("nombre", request.getNombre())
                .append("apellidos", request.getApellidos())
                .append("email", request.getEmail())
                .append("foto", request.getFoto())
                .append("bloqueado", false)
                .append("contrasenia", contraseniaRef) // DBRef a la contraseña
                .append("sesionstoken", new ArrayList<>())
                .append("fecharegistro", new Date())
                .append("twoFactorAutenticationEnabled", false)
                .append("threeFactorAutenticationEnabled", false)
                .append("_class", "iso25.g05.esi_media.model.Administrador");
            
            logger.info("Insertando usuario con contraseña vinculada...");
            usersCollection.insertOne(adminDoc);
            logger.info("✅ Usuario insertado exitosamente en colección USERS");
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Administrador creado exitosamente con contraseña");
            response.put("email", request.getEmail());
            response.put("nombre", request.getNombre());
            response.put("departamento", request.getDepartamento());
            response.put("contraseniaId", contraseniaId);
            response.put("coleccion", "users");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("ERROR en creación: {}", e.getMessage(), e);
            
        }
    }
}