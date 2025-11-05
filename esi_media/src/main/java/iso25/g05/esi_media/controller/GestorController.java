package iso25.g05.esi_media.controller;

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

import com.mongodb.client.MongoCollection;

import iso25.g05.esi_media.dto.CrearGestorRequest;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.repository.ContraseniaComunRepository;
import iso25.g05.esi_media.service.UserService;

@RestController
@RequestMapping("/gestores")
@CrossOrigin(origins = "*")
public class GestorController {
    
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private ContraseniaComunRepository contraseniaComunRepository;
    
    /**
     * Endpoint para crear Gestores de Contenido
     * Utiliza inserción directa a MongoDB en la colección 'users'
     */
    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearGestor(@RequestBody CrearGestorRequest request) {
        System.out.println("=== CREACIÓN GESTOR DE CONTENIDO ===");
        System.out.println("Datos recibidos: " + request);
        
        try {
            System.out.println("Procesando Gestor: " + request.getNombre() + " " + request.getApellidos() + " - " + request.getEmail());
            System.out.println("Alias: " + request.getAlias() + " | Especialidad: " + request.getEspecialidad());
            
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

            if(contraseniaComunRepository.existsById(c.getContraseniaActual())){
                Map<String, Object> error = new HashMap<>();
                error.put("mensaje", "La contraseña está en la lista de contraseñas comunes");
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
            }
            
            System.out.println("📝 Base de datos actual: " + mongoTemplate.getDb().getName());
            System.out.println("📝 Colección: " + contraseniasCollection.getNamespace());
            System.out.println("📝 Insertando contraseña: " + contraseniaDoc.toJson());
            contraseniasCollection.insertOne(contraseniaDoc);
            ObjectId contraseniaObjectId = contraseniaDoc.getObjectId("_id");
            String contraseniaId = contraseniaObjectId.toString();
            System.out.println("✅ Contraseña insertada en BD con _id: " + contraseniaObjectId);
            System.out.println("✅ String ID para DBRef: " + contraseniaId);
            
            // Paso 2: Crear DBRef para la contraseña
            com.mongodb.DBRef contraseniaRef = new com.mongodb.DBRef("contrasenias", new org.bson.types.ObjectId(contraseniaId));
            
            // Paso 3: Crear documento de Gestor con referencia a contraseña
            MongoCollection<Document> usersCollection = mongoTemplate.getCollection("users");
            
            Document gestorDoc = new Document()
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
                .append("_class", "iso25.g05.esi_media.model.GestordeContenido")
                // Campos específicos del Gestor
                .append("alias", request.getAlias())
                .append("descripcion", request.getDescripcion())
                .append("campoespecializacion", request.getEspecialidad())
                .append("tipocontenidovideooaudio", request.getTipoContenido())
                .append("listasgeneradas", new ArrayList<>());
            
            System.out.println("Insertando Gestor con contraseña vinculada...");
            usersCollection.insertOne(gestorDoc);
            System.out.println("✅ Gestor insertado exitosamente en colección USERS");
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Gestor de Contenido creado exitosamente con contraseña");
            response.put("email", request.getEmail());
            response.put("nombre", request.getNombre());
            response.put("alias", request.getAlias());
            response.put("especialidad", request.getEspecialidad());
            response.put("contraseniaId", contraseniaId);
            response.put("coleccion", "users");
            response.put("tipo", "GestordeContenido");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR en creación de Gestor: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", "Error al crear Gestor: " + e.getMessage());
            errorResponse.put("tipo", "GestordeContenido");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
