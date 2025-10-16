package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.dto.CrearAdministradorRequest;
import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoCollection;
import com.mongodb.MongoWriteException;
import com.mongodb.DBRef;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Calendar;
import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/administradores")
public class AdministradorController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * Endpoint para crear un nuevo administrador
     * @param request Datos del administrador a crear
     * @param adminActualId ID del administrador que realiza la petición (cabecera)
     * @return Respuesta con el administrador creado
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearAdministrador(
            @RequestBody CrearAdministradorRequest request,
            @RequestHeader("Admin-ID") String adminActualId) {
        
        try {
            Administrador nuevoAdmin = userService.crearAdministrador(request, adminActualId);
            
            // No devolver información sensible como contraseñas
            AdminResponse response = new AdminResponse(
                "temp-id", // nuevoAdmin.getId(),
                "temp-nombre", // nuevoAdmin.getNombre(),
                "temp-apellidos", // nuevoAdmin.getApellidos(),
                "temp@email.com", // nuevoAdmin.getEmail(),
                "temp-dept", // nuevoAdmin.getdepartamento(),
                "ADMINISTRADOR", // Valor fijo ya que es administrador
                new java.util.Date() // Fecha actual como placeholder
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Endpoint simplificado para crear administradores (sin autenticación requerida)
     * Utiliza inserción directa a MongoDB en la colección 'users'
     */
    @PostMapping("/crear-simple")
    public ResponseEntity<Map<String, Object>> crearAdministradorSimple(@RequestBody CrearAdministradorRequest request) {
        System.out.println("=== CREACIÓN BYPASS ÍNDICE CORRUPTO ===");
        System.out.println("Datos recibidos: " + request);
        
        try {
            System.out.println("Procesando: " + request.getNombre() + " " + request.getApellidos() + " - " + request.getEmail());
            
            // Paso 1: Crear documento de contraseña en la colección 'contrasenias'
            MongoCollection<Document> contraseniasCollection = mongoTemplate.getCollection("contrasenias");
            
            // Calcular fecha de expiración (1 año desde ahora)
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 1);
            Date fechaExpiracion = cal.getTime();
            
            Document contraseniaDoc = new Document()
                .append("fecha_expiracion", fechaExpiracion)
                .append("contrasenia_actual", request.getContrasenia())
                .append("contrasenia_usadas", new ArrayList<>())
                .append("_class", "iso25.g05.esi_media.model.Contrasenia");
            
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
            
            System.out.println("Insertando usuario con contraseña vinculada...");
            usersCollection.insertOne(adminDoc);
            System.out.println("✅ Usuario insertado exitosamente en colección USERS");
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Administrador creado exitosamente con contraseña");
            response.put("email", request.getEmail());
            response.put("nombre", request.getNombre());
            response.put("departamento", request.getDepartamento());
            response.put("contraseniaId", contraseniaId);
            response.put("coleccion", "users");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR en creación: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", "Error al crear usuario: " + e.getMessage());
            errorResponse.put("solucion", "Contacte al administrador para resolver problemas de índice en MongoDB");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Clase interna para la respuesta
    public static class AdminResponse {
        private String id;
        private String nombre;
        private String apellidos;
        private String email;
        private String departamento;
        private String tipoAdministrador;
        private java.util.Date fechaRegistro;
        
        public AdminResponse(String id, String nombre, String apellidos, String email, 
                           String departamento, String tipoAdministrador, java.util.Date fechaRegistro) {
            this.id = id;
            this.nombre = nombre;
            this.apellidos = apellidos;
            this.email = email;
            this.departamento = departamento;
            this.tipoAdministrador = tipoAdministrador;
            this.fechaRegistro = fechaRegistro;
        }
        
        // Getters
        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getApellidos() { return apellidos; }
        public String getEmail() { return email; }
        public String getDepartamento() { return departamento; }
        public String getTipoAdministrador() { return tipoAdministrador; }
        public java.util.Date getFechaRegistro() { return fechaRegistro; }
    }
    
    // Clase interna para errores
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() { return error; }
    }
}