package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.dto.CrearAdministradorRequest;
import iso25.g05.esi_media.dto.CrearGestorRequest;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.service.usersservice;
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
    private usersservice userService;
    
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
            
            // ESTRATEGIA 1: Usar insertOne directamente con la colección para evitar mapeo de objetos
            MongoCollection<Document> usersCollection = mongoTemplate.getCollection("users");
            
            // Crear documento con nombres de campos sin guiones bajos
            Document adminDoc = new Document()
                .append("departamento", request.getDepartamento())
                .append("nombre", request.getNombre())
                .append("apellidos", request.getApellidos())
                .append("email", request.getEmail())
                .append("foto", request.getFoto()) // Campo foto añadido
                .append("bloqueado", false)
                .append("sesionstoken", new ArrayList<>()) // Array vacío (sin guiones bajos)
                .append("fecharegistro", new Date())
                .append("twoFactorAutenticationEnabled", false)
                .append("threeFactorAutenticationEnabled", false)
                .append("_class", "iso25.g05.esi_media.model.Administrador");
            
            System.out.println("Insertando directamente en colección...");
            
            // Inserción SOLO en colección users - sin alternativas
            usersCollection.insertOne(adminDoc);
            System.out.println("✅ Usuario insertado exitosamente en colección USERS");
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Administrador creado exitosamente en colección users");
            response.put("email", request.getEmail());
            response.put("nombre", request.getNombre());
            response.put("coleccion", "users");
            response.put("estructura", "compatible con documentos existentes");
            
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
    
    /**
     * Endpoint simplificado para crear Gestores de Contenido
     * Utiliza inserción directa a MongoDB en la colección 'users'
     */
    @PostMapping("/crear-gestor")
    public ResponseEntity<Map<String, Object>> crearGestorSimple(@RequestBody CrearGestorRequest request) {
        System.out.println("=== CREACIÓN GESTOR DE CONTENIDO ===");
        System.out.println("Datos recibidos: " + request);
        
        try {
            System.out.println("Procesando Gestor: " + request.getNombre() + " " + request.getApellidos() + " - " + request.getEmail());
            System.out.println("Alias: " + request.getAlias() + " | Especialidad: " + request.getEspecialidad());
            
            MongoCollection<Document> usersCollection = mongoTemplate.getCollection("users");
            
            // Crear documento para Gestor de Contenido
            Document gestorDoc = new Document()
                .append("nombre", request.getNombre())
                .append("apellidos", request.getApellidos())
                .append("email", request.getEmail())
                .append("foto", request.getFoto()) // Campo foto añadido
                .append("bloqueado", false)
                .append("sesionstoken", new ArrayList<>()) // Array vacío (sin guiones bajos)
                .append("fecharegistro", new Date())
                .append("twoFactorAutenticationEnabled", false)
                .append("threeFactorAutenticationEnabled", false)
                .append("_class", "iso25.g05.esi_media.model.GestordeContenido")
                // Campos específicos del Gestor (nuevos nombres sin guiones bajos)
                .append("alias", request.getAlias())
                .append("descripcion", request.getDescripcion())
                .append("campoespecializacion", request.getEspecialidad())
                .append("tipocontenidovideooaudio", request.getTipoContenido())
                .append("listasgeneradas", new ArrayList<>()); // Array vacío de listas
            
            System.out.println("Insertando Gestor directamente en colección users...");
            
            // Inserción en colección users
            usersCollection.insertOne(gestorDoc);
            System.out.println("✅ Gestor insertado exitosamente en colección USERS");
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Gestor de Contenido creado exitosamente");
            response.put("email", request.getEmail());
            response.put("nombre", request.getNombre());
            response.put("alias", request.getAlias());
            response.put("especialidad", request.getEspecialidad());
            response.put("tipoContenido", request.getTipoContenido());
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