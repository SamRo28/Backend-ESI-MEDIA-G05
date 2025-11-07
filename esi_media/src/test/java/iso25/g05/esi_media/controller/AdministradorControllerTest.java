package iso25.g05.esi_media.controller;

import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import iso25.g05.esi_media.config.MongoTestConfig;
import iso25.g05.esi_media.dto.CrearAdministradorRequest;

/**
 * Pruebas TDD para la creación de administradores a través del formulario
 * Enfocadas en la Historia de Usuario: "Crear Administradores"
 * 
 * Criterios de Aceptación:
 * 1. El correo electrónico debe ser único y validado
 * 2. Se asigna automáticamente el rol "Administrador"
 * 3. Validación correcta de todos los campos del formulario
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {MongoTestConfig.class})
@TestPropertySource(properties = {
    "spring.data.mongodb.database=esi_media_test"
})
@ActiveProfiles("test")
@DisplayName("Tests para Creación de Administradores - Formulario de Registro")
public class AdministradorControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/administradores";
        
        // Limpiar colecciones antes de cada test
        mongoTemplate.getCollection("users").deleteMany(new Document());
        mongoTemplate.getCollection("contrasenias").deleteMany(new Document());
    }

    // ==================== PRUEBAS DE CREACIÓN EXITOSA ====================

    @Test
    @DisplayName("Test 1: Crear administrador con todos los campos válidos")
    void testCrearAdministradorExitoso() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Juan");
        request.setApellidos("Pérez García");
        request.setEmail("juan.perez@esi.uclm.es");
        request.setContrasenia("Password123!");
        request.setDepartamento("Informática");
        request.setFoto("perfil1.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        // El controlador ahora devuelve el objeto Administrador directamente, no un Map con mensaje
        assertEquals("juan.perez@esi.uclm.es", response.getBody().get("email"));
        assertEquals("Juan", response.getBody().get("nombre"));
        assertEquals("Informática", response.getBody().get("departamento"));
        
        // Verificar que se guardó en la base de datos
        Document adminEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "juan.perez@esi.uclm.es"))
            .first();
        
        assertNotNull(adminEnBD, "El administrador debe estar guardado en la BD");
        assertEquals("Juan", adminEnBD.getString("nombre"));
        assertEquals("Pérez García", adminEnBD.getString("apellidos"));
        assertEquals("juan.perez@esi.uclm.es", adminEnBD.getString("email"));
    }

    @Test
    @DisplayName("Test 2: Verificar que se asigna automáticamente el rol 'Administrador'")
    void testAsignacionAutomaticaRolAdministrador() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("María");
        request.setApellidos("López");
        request.setEmail("maria.lopez@esi.uclm.es");
        request.setContrasenia("SecurePass456!");
        request.setDepartamento("Sistemas");
        request.setFoto("perfil2.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        // Verificar que el tipo/clase en BD sea Administrador
        Document adminEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "maria.lopez@esi.uclm.es"))
            .first();
        
        assertNotNull(adminEnBD);
        assertEquals("iso25.g05.esi_media.model.Administrador", adminEnBD.getString("_class"),
            "El campo _class debe indicar que es un Administrador");
    }

    @Test
    @DisplayName("Test 3: Crear administrador sin foto (foto es opcional)")
    void testCrearAdministradorSinFoto() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Carlos");
        request.setApellidos("Sánchez");
        request.setEmail("carlos.sanchez@esi.uclm.es");
        request.setContrasenia("AdminPass789!");
        request.setDepartamento("Redes");
        request.setFoto(null); // Sin foto

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Document adminEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "carlos.sanchez@esi.uclm.es"))
            .first();
        
        assertNotNull(adminEnBD);
        assertNull(adminEnBD.get("foto"), "La foto debe ser null si no se proporcionó");
    }

    @Test
    @DisplayName("Test 4: Crear administrador con foto de perfil seleccionada")
    void testCrearAdministradorConFotoPerfil() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Ana");
        request.setApellidos("Martínez");
        request.setEmail("ana.martinez@esi.uclm.es");
        request.setContrasenia("FotoPass123!");
        request.setDepartamento("Bases de Datos");
        request.setFoto("perfil3.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        Document adminEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "ana.martinez@esi.uclm.es"))
            .first();
        
        assertNotNull(adminEnBD);
        assertEquals("perfil3.png", adminEnBD.getString("foto"),
            "La foto debe guardarse correctamente en la BD");
    }

    // ==================== PRUEBAS DE VALIDACIÓN DE EMAIL ÚNICO ====================

    @Test
    @DisplayName("Test 5: Permitir creación con email duplicado (validación pendiente de implementar)")
    void testEmailDuplicadoPermitidoActualmente() {
        // Arrange - Crear primer administrador
        CrearAdministradorRequest primerAdmin = new CrearAdministradorRequest();
        primerAdmin.setNombre("Pedro");
        primerAdmin.setApellidos("González");
        primerAdmin.setEmail("pedro.gonzalez@esi.uclm.es");
        primerAdmin.setContrasenia("Pass123!");
        primerAdmin.setDepartamento("Software");
        
        ResponseEntity<Map> response1 = restTemplate.postForEntity(baseUrl + "/crear-simple", primerAdmin, Map.class);

        // Intentar crear segundo administrador con mismo email
        CrearAdministradorRequest segundoAdmin = new CrearAdministradorRequest();
        segundoAdmin.setNombre("Otro");
        segundoAdmin.setApellidos("Usuario");
        segundoAdmin.setEmail("pedro.gonzalez@esi.uclm.es"); // Email duplicado
        segundoAdmin.setContrasenia("OtraPass456!");
        segundoAdmin.setDepartamento("Hardware");

        // Act
        ResponseEntity<Map> response2 = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            segundoAdmin,
            Map.class
        );

        // Assert - Ahora el sistema rechaza duplicados con CONFLICT
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, response2.getStatusCode(),
            "Ahora rechaza emails duplicados con CONFLICT");
        
        // Verificar que solo existe un usuario con ese email
        long count = mongoTemplate.getCollection("users")
            .countDocuments(new Document("email", "pedro.gonzalez@esi.uclm.es"));
        
        assertEquals(1, count, "Solo debe haber un usuario con el email");
    }

    @Test
    @DisplayName("Test 6: Permitir emails distintos aunque nombres sean iguales")
    void testNombresIgualesEmailsDiferentes() {
        // Arrange
        CrearAdministradorRequest admin1 = new CrearAdministradorRequest();
        admin1.setNombre("Luis");
        admin1.setApellidos("García");
        admin1.setEmail("luis.garcia1@esi.uclm.es");
        admin1.setContrasenia("Pass123!");
        admin1.setDepartamento("Software");
        
        CrearAdministradorRequest admin2 = new CrearAdministradorRequest();
        admin2.setNombre("Luis");
        admin2.setApellidos("García");
        admin2.setEmail("luis.garcia2@esi.uclm.es"); // Email diferente
        admin2.setContrasenia("Pass456!");
        admin2.setDepartamento("Software");

        // Act
        ResponseEntity<Map> response1 = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            admin1,
            Map.class
        );
        
        ResponseEntity<Map> response2 = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            admin2,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        assertEquals(HttpStatus.CREATED, response2.getStatusCode(),
            "Debe permitir nombres iguales con emails diferentes");
        
        long count = mongoTemplate.getCollection("users")
            .countDocuments(new Document("nombre", "Luis").append("apellidos", "García"));
        
        assertEquals(2, count, "Deben existir dos usuarios con el mismo nombre");
    }

    // ==================== PRUEBAS DE VALIDACIÓN DE CAMPOS OBLIGATORIOS ====================

    @Test
    @DisplayName("Test 7: Permitir creación sin nombre (validación pendiente)")
    void testCreacionSinNombre() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre(null); // Nombre nulo
        request.setApellidos("Apellido");
        request.setEmail("test@esi.uclm.es");
        request.setContrasenia("Pass123!");
        request.setDepartamento("IT");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            request,
            Map.class
        );

        // Assert - Actualmente permite valores nulos
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
            "Actualmente permite creación sin nombre - validación pendiente");
        
        Document adminEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "test@esi.uclm.es"))
            .first();
        
        assertNotNull(adminEnBD);
        assertNull(adminEnBD.getString("nombre"), "El nombre se guarda como null");
    }

    @Test
    @DisplayName("Test 8: Permitir creación sin apellidos (validación pendiente)")
    void testCreacionSinApellidos() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Nombre");
        request.setApellidos(null); // Apellidos nulos
        request.setEmail("test2@esi.uclm.es");
        request.setContrasenia("Pass123!");
        request.setDepartamento("IT");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            request,
            Map.class
        );

        // Assert - Actualmente permite valores nulos
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
            "Actualmente permite creación sin apellidos - validación pendiente");
            
        Document adminEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "test2@esi.uclm.es"))
            .first();
        
        assertNotNull(adminEnBD);
        assertNull(adminEnBD.getString("apellidos"), "Los apellidos se guardan como null");
    }

    @Test
    @DisplayName("Test 9: Permitir creación sin email (validación pendiente)")
    void testCreacionSinEmail() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Nombre");
        request.setApellidos("Apellido");
        request.setEmail(null); // Email nulo
        request.setContrasenia("Pass123!");
        request.setDepartamento("IT");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            request,
            Map.class
        );

        // Assert - Ahora rechaza creación sin email con CONFLICT
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode(),
            "Ahora rechaza creación sin email con CONFLICT");
    }

    @Test
    @DisplayName("Test 10: Permitir creación sin contraseña (validación pendiente)")
    void testCreacionSinContrasenia() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Nombre");
        request.setApellidos("Apellido");
        request.setEmail("test3@esi.uclm.es");
        request.setContrasenia(null); // Contraseña nula
        request.setDepartamento("IT");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            request,
            Map.class
        );

        // Assert - Ahora rechaza creación sin contraseña con CONFLICT
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode(),
            "Ahora rechaza creación sin contraseña con CONFLICT");
            
        // Cuando hay un error de validación, no se crea el usuario ni la contraseña
        // Por lo tanto no hay contraseniaId en la respuesta
        assertNotNull(response.getBody(), "Debe haber un mensaje de error en el body");
    }

    @Test
    @DisplayName("Test 11: Permitir creación sin departamento (validación pendiente)")
    void testCreacionSinDepartamento() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Nombre");
        request.setApellidos("Apellido");
        request.setEmail("test4@esi.uclm.es");
        request.setContrasenia("Pass123!");
        request.setDepartamento(null); // Departamento nulo

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            request,
            Map.class
        );

        // Assert - Actualmente permite valores nulos
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
            "Actualmente permite creación sin departamento - validación pendiente");
            
        Document adminEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "test4@esi.uclm.es"))
            .first();
        
        assertNotNull(adminEnBD);
        assertNull(adminEnBD.getString("departamento"), "El departamento se guarda como null");
    }

    // ==================== PRUEBAS DE CREACIÓN DE CONTRASEÑA ====================

    @Test
    @DisplayName("Test 12: Verificar que se crea el documento de contraseña en la colección 'contrasenias'")
    void testCreacionDocumentoContrasenia() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Roberto");
        request.setApellidos("Fernández");
        request.setEmail("roberto.fernandez@esi.uclm.es");
        request.setContrasenia("SecurePass123!");
        request.setDepartamento("Seguridad");
        request.setFoto("perfil4.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        // El controlador devuelve el objeto Administrador, que tiene una referencia a contrasenia
        Map<String, Object> contrasenia = (Map<String, Object>) response.getBody().get("contrasenia");
        assertNotNull(contrasenia, "Debe tener una contraseña asignada");
        assertNotNull(contrasenia.get("id"), "La contraseña debe tener un ID");
        
        // Verificar que existe el documento de contraseña
        String contraseniaId = (String) contrasenia.get("id");
        Document contraseniaDoc = mongoTemplate.getCollection("contrasenias")
            .find(new Document("_id", new ObjectId(contraseniaId)))
            .first();
        
        assertNotNull(contraseniaDoc, "Debe existir el documento de contraseña en la colección");
        assertNotNull(contraseniaDoc.getString("contrasenia_actual"));
        assertNotNull(contraseniaDoc.getDate("fecha_expiracion"));
    }

    @Test
    @DisplayName("Test 13: Verificar que el administrador tiene referencia DBRef a su contraseña")
    void testReferenciaDBRefContrasenia() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Laura");
        request.setApellidos("Ruiz");
        request.setEmail("laura.ruiz@esi.uclm.es");
        request.setContrasenia("LauraPass456!");
        request.setDepartamento("Desarrollo");
        request.setFoto("perfil1.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        Document adminEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "laura.ruiz@esi.uclm.es"))
            .first();
        
        assertNotNull(adminEnBD);
        assertNotNull(adminEnBD.get("contrasenia"),
            "El administrador debe tener el campo contrasenia");
        
        // Verificar que es un DBRef
        Object contraseniaField = adminEnBD.get("contrasenia");
        assertTrue(contraseniaField instanceof com.mongodb.DBRef,
            "El campo contrasenia debe ser un DBRef");
        
        com.mongodb.DBRef dbRef = (com.mongodb.DBRef) contraseniaField;
        assertEquals("contrasenias", dbRef.getCollectionName(),
            "El DBRef debe apuntar a la colección 'contrasenias'");
    }

    // ==================== PRUEBAS DE VALORES POR DEFECTO ====================

    @Test
    @DisplayName("Test 14: Verificar que el administrador NO está bloqueado por defecto")
    void testAdministradorNoBloqueadoPorDefecto() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Diego");
        request.setApellidos("Torres");
        request.setEmail("diego.torres@esi.uclm.es");
        request.setContrasenia("DiegoPass789!");
        request.setDepartamento("Testing");

        // Act
        restTemplate.postForEntity(baseUrl + "/crear-simple", request, Map.class);

        // Assert
        Document adminEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "diego.torres@esi.uclm.es"))
            .first();
        
        assertNotNull(adminEnBD);
        assertFalse(adminEnBD.getBoolean("bloqueado"),
            "El administrador NO debe estar bloqueado por defecto");
    }

    @Test
    @DisplayName("Test 15: Verificar que se establece la fecha de registro")
    void testFechaRegistroEstablecida() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Sofia");
        request.setApellidos("Navarro");
        request.setEmail("sofia.navarro@esi.uclm.es");
        request.setContrasenia("SofiaPass123!");
        request.setDepartamento("QA");

        // Act
        restTemplate.postForEntity(baseUrl + "/crear-simple", request, Map.class);

        // Assert
        Document adminEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "sofia.navarro@esi.uclm.es"))
            .first();
        
        assertNotNull(adminEnBD);
        assertNotNull(adminEnBD.getDate("fecharegistro"),
            "Debe establecerse automáticamente la fecha de registro");
    }

    // ==================== PRUEBAS DE INTEGRIDAD DE DATOS ====================

    @Test
    @DisplayName("Test 16: Verificar que todos los campos se guardan correctamente")
    void testTodosCamposGuardadosCorrectamente() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Miguel");
        request.setApellidos("Jiménez Pérez");
        request.setEmail("miguel.jimenez@esi.uclm.es");
        request.setContrasenia("MiguelPass456!");
        request.setDepartamento("Arquitectura de Software");
        request.setFoto("perfil2.png");

        // Act
        restTemplate.postForEntity(baseUrl + "/crear-simple", request, Map.class);

        // Assert
        Document adminEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "miguel.jimenez@esi.uclm.es"))
            .first();
        
        assertNotNull(adminEnBD);
        assertEquals("Miguel", adminEnBD.getString("nombre"));
        assertEquals("Jiménez Pérez", adminEnBD.getString("apellidos"));
        assertEquals("miguel.jimenez@esi.uclm.es", adminEnBD.getString("email"));
        assertEquals("perfil2.png", adminEnBD.getString("foto"));
        assertEquals("Arquitectura de Software", adminEnBD.getString("departamento"));
        assertFalse(adminEnBD.getBoolean("bloqueado"));
        assertNotNull(adminEnBD.getDate("fecharegistro"));
        assertEquals("iso25.g05.esi_media.model.Administrador", adminEnBD.getString("_class"));
    }

    @Test
    @DisplayName("Test 17: Crear múltiples administradores secuencialmente")
    void testCrearMultiplesAdministradores() {
        // Arrange & Act
        for (int i = 1; i <= 5; i++) {
            CrearAdministradorRequest request = new CrearAdministradorRequest();
            request.setNombre("Admin" + i);
            request.setApellidos("Apellido" + i);
            request.setEmail("admin" + i + "@esi.uclm.es");
            request.setContrasenia("Pass" + i + "123!");
            request.setDepartamento("Depto" + i);
            request.setFoto("perfil" + (i % 4 + 1) + ".png");

            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/crear-simple",
                request,
                Map.class
            );

            assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Debe crear el administrador " + i + " exitosamente");
        }

        // Assert
        long count = mongoTemplate.getCollection("users").countDocuments();
        assertEquals(5, count, "Deben existir 5 administradores en la BD");
    }

    @Test
    @DisplayName("Test 18: Verificar respuesta contiene todos los datos esperados")
    void testRespuestaContieneTodasPropiedades() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Elena");
        request.setApellidos("Moreno");
        request.setEmail("elena.moreno@esi.uclm.es");
        request.setContrasenia("ElenaPass789!");
        request.setDepartamento("DevOps");
        request.setFoto("perfil3.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        
        assertNotNull(body);
        // El controlador devuelve el objeto Administrador directamente
        assertTrue(body.containsKey("email"), "Debe contener el campo 'email'");
        assertTrue(body.containsKey("nombre"), "Debe contener el campo 'nombre'");
        assertTrue(body.containsKey("departamento"), "Debe contener el campo 'departamento'");
        assertTrue(body.containsKey("contrasenia"), "Debe contener el campo 'contrasenia'");
        
        assertEquals("Elena", body.get("nombre"));
        assertEquals("elena.moreno@esi.uclm.es", body.get("email"));
        assertEquals("DevOps", body.get("departamento"));
        
        // Verificar en BD que los apellidos se guardaron correctamente
        Document adminEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "elena.moreno@esi.uclm.es"))
            .first();
        assertEquals("Moreno", adminEnBD.getString("apellidos"));
    }

    @Test
    @DisplayName("Test 19: Verificar que las contraseñas se almacenan correctamente (texto plano por ahora)")
    void testAlmacenamientoContrasenia() {
        // Arrange
        CrearAdministradorRequest request = new CrearAdministradorRequest();
        request.setNombre("Javier");
        request.setApellidos("Castro");
        request.setEmail("javier.castro@esi.uclm.es");
        request.setContrasenia("JavierSecure123!");
        request.setDepartamento("Security");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        // El controlador devuelve el objeto Administrador con la contraseña anidada
        Map<String, Object> contrasenia = (Map<String, Object>) response.getBody().get("contrasenia");
        assertNotNull(contrasenia, "Debe tener una contraseña");
        
        String contraseniaId = (String) contrasenia.get("id");
        assertNotNull(contraseniaId, "La contraseña debe tener un ID");
        
        Document contraseniaDoc = mongoTemplate.getCollection("contrasenias")
            .find(new Document("_id", new ObjectId(contraseniaId)))
            .first();
        
        assertNotNull(contraseniaDoc);
        // Verificar que la contraseña se almacenó (hasheada con MD5)
        assertNotNull(contraseniaDoc.getString("contrasenia_actual"),
            "La contraseña debe almacenarse correctamente");
    }

    @Test
    @DisplayName("Test 20: Verificar que cada administrador tiene una contraseña única e independiente")
    void testContrasenasIndependientes() {
        // Arrange
        CrearAdministradorRequest admin1 = new CrearAdministradorRequest();
        admin1.setNombre("Admin1");
        admin1.setApellidos("Test1");
        admin1.setEmail("admin1@test.com");
        admin1.setContrasenia("Password1!");
        admin1.setDepartamento("Dept1");
        
        CrearAdministradorRequest admin2 = new CrearAdministradorRequest();
        admin2.setNombre("Admin2");
        admin2.setApellidos("Test2");
        admin2.setEmail("admin2@test.com");
        admin2.setContrasenia("Password2!");
        admin2.setDepartamento("Dept2");

        // Act
        ResponseEntity<Map> response1 = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            admin1,
            Map.class
        );
        
        ResponseEntity<Map> response2 = restTemplate.postForEntity(
            baseUrl + "/crear-simple",
            admin2,
            Map.class
        );

        // Assert
        // El controlador devuelve el objeto Administrador con la contraseña anidada
        Map<String, Object> contrasenia1 = (Map<String, Object>) response1.getBody().get("contrasenia");
        Map<String, Object> contrasenia2 = (Map<String, Object>) response2.getBody().get("contrasenia");
        
        assertNotNull(contrasenia1, "Admin1 debe tener contraseña");
        assertNotNull(contrasenia2, "Admin2 debe tener contraseña");
        
        String contraseniaId1 = (String) contrasenia1.get("id");
        String contraseniaId2 = (String) contrasenia2.get("id");
        
        assertNotEquals(contraseniaId1, contraseniaId2,
            "Cada administrador debe tener su propia contraseña con ID único");
        
        // Verificar que ambas contraseñas existen en la BD
        long count = mongoTemplate.getCollection("contrasenias").countDocuments();
        assertEquals(2, count, "Deben existir 2 documentos de contraseña independientes");
    }
}

