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

import iso25.g05.esi_media.EsiMediaApplication;
import iso25.g05.esi_media.config.MongoTestConfig;
import iso25.g05.esi_media.dto.CrearGestorRequest;

/**
 * Pruebas TDD para la creación de Gestores de Contenido a través del formulario
 * Enfocadas en la Historia de Usuario: "Crear Gestores de Contenido"
 * 
 * Criterios de Aceptación:
 * 1. El correo electrónico debe ser único y validado
 * 2. Se asigna automáticamente el rol "GestordeContenido"  
 * 3. Validación correcta de todos los campos del formulario
 * 4. Campos específicos: alias, especialidad, tipoContenido, descripción
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {EsiMediaApplication.class, MongoTestConfig.class})
@TestPropertySource(properties = {
    "spring.data.mongodb.database=esi_media_test"
})
@ActiveProfiles("test")
@DisplayName("Tests para Creación de Gestores de Contenido - Formulario de Registro")
public class GestorControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/gestores";
        
        // Limpiar colecciones antes de cada test
        mongoTemplate.getCollection("users").deleteMany(new Document());
        mongoTemplate.getCollection("contrasenias").deleteMany(new Document());
    }

    // ==================== PRUEBAS DE CREACIÓN EXITOSA ====================

    @Test
    @DisplayName("Test 1: Crear gestor de contenido con todos los campos válidos")
    void testCrearGestorExitoso() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("María");
        request.setApellidos("González López");
        request.setEmail("maria.gonzalez@esi.uclm.es");
        request.setContrasenia("GestorPass123!");
        request.setAlias("MariaG");
        request.setDescripcion("Especialista en contenido audiovisual");
        request.setEspecialidad("Producción de Video");
        request.setTipoContenido("Video");
        request.setFoto("perfil1.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        // El controlador devuelve el objeto GestordeContenido directamente, no un Map con mensaje
        assertEquals("maria.gonzalez@esi.uclm.es", response.getBody().get("email"));
        assertEquals("María", response.getBody().get("nombre"));
        assertEquals("MariaG", response.getBody().get("alias"));
        // El campo en el modelo es "campoespecializacion", no "especialidad"
        assertEquals("Producción de Video", response.getBody().get("campoespecializacion"));
        
        // Verificar que se guardó en la base de datos
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "maria.gonzalez@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD, "El gestor debe estar guardado en la BD");
        assertEquals("María", gestorEnBD.getString("nombre"));
        assertEquals("González López", gestorEnBD.getString("apellidos"));
        assertEquals("maria.gonzalez@esi.uclm.es", gestorEnBD.getString("email"));
        assertEquals("MariaG", gestorEnBD.getString("alias"));
        assertEquals("Producción de Video", gestorEnBD.getString("campoespecializacion"));
        assertEquals("Video", gestorEnBD.getString("tipocontenidovideooaudio"));
    }

    @Test
    @DisplayName("Test 2: Verificar que se asigna automáticamente el rol 'GestordeContenido'")
    void testAsignacionAutomaticaRolGestor() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Carlos");
        request.setApellidos("Martín");
        request.setEmail("carlos.martin@esi.uclm.es");
        request.setContrasenia("CarlosPass456!");
        request.setAlias("CarlosM");
        request.setDescripcion("Experto en audio digital");
        request.setEspecialidad("Producción de Audio");
        request.setTipoContenido("Audio");
        request.setFoto("perfil2.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        // Verificar que el tipo/clase en BD sea GestordeContenido
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "carlos.martin@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD);
        assertEquals("iso25.g05.esi_media.model.GestordeContenido", gestorEnBD.getString("_class"),
            "El campo _class debe indicar que es un GestordeContenido");
    }

    @Test
    @DisplayName("Test 3: Crear gestor con descripción opcional vacía")
    void testCrearGestorSinDescripcion() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Ana");
        request.setApellidos("Rodríguez");
        request.setEmail("ana.rodriguez@esi.uclm.es");
        request.setContrasenia("AnaPass789!");
        request.setAlias("AnaR");
        request.setDescripcion(null); // Sin descripción
        request.setEspecialidad("Edición de Video");
        request.setTipoContenido("Video");
        request.setFoto("perfil3.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "ana.rodriguez@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD);
        assertNull(gestorEnBD.get("descripcion"), "La descripción debe ser null si no se proporcionó");
    }

    @Test
    @DisplayName("Test 4: Crear gestor con todos los campos obligatorios específicos")
    void testCamposObligatoriosGestor() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Luis");
        request.setApellidos("Fernández");
        request.setEmail("luis.fernandez@esi.uclm.es");
        request.setContrasenia("LuisPass123!");
        request.setAlias("LuisF");
        request.setDescripcion("Creador de contenido multimedia");
        request.setEspecialidad("Animación 3D");
        request.setTipoContenido("Video");
        request.setFoto("perfil4.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "luis.fernandez@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD);
        assertEquals("LuisF", gestorEnBD.getString("alias"));
        assertEquals("Animación 3D", gestorEnBD.getString("campoespecializacion"));
        assertEquals("Video", gestorEnBD.getString("tipocontenidovideooaudio"));
        assertEquals("Creador de contenido multimedia", gestorEnBD.getString("descripcion"));
    }

    // ==================== PRUEBAS DE TIPOS DE CONTENIDO ====================

    @Test
    @DisplayName("Test 5: Crear gestor especializado en Audio")
    void testGestorEspecializadoAudio() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Elena");
        request.setApellidos("Torres");
        request.setEmail("elena.torres@esi.uclm.es");
        request.setContrasenia("ElenaPass456!");
        request.setAlias("ElenaT");
        request.setDescripcion("Productora de podcasts y contenido de audio");
        request.setEspecialidad("Producción de Podcasts");
        request.setTipoContenido("Audio");
        request.setFoto("perfil1.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        // El campo 'tipo' no existe en el response, es un GestordeContenido directamente
        assertNotNull(response.getBody().get("email"), "Debe tener email");
        
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "elena.torres@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD);
        assertEquals("Audio", gestorEnBD.getString("tipocontenidovideooaudio"));
        assertEquals("Producción de Podcasts", gestorEnBD.getString("campoespecializacion"));
    }

    @Test
    @DisplayName("Test 6: Crear gestor especializado en Video")
    void testGestorEspecializadoVideo() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("David");
        request.setApellidos("Morales");
        request.setEmail("david.morales@esi.uclm.es");
        request.setContrasenia("DavidPass789!");
        request.setAlias("DavidM");
        request.setDescripcion("Director y editor de contenido audiovisual");
        request.setEspecialidad("Dirección Audiovisual");
        request.setTipoContenido("Video");
        request.setFoto("perfil2.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "david.morales@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD);
        assertEquals("Video", gestorEnBD.getString("tipocontenidovideooaudio"));
        assertEquals("Dirección Audiovisual", gestorEnBD.getString("campoespecializacion"));
    }

    // ==================== PRUEBAS DE VALIDACIÓN DE ALIAS ====================

    @Test
    @DisplayName("Test 7: Permitir alias duplicados (validación pendiente)")
    void testAliasDuplicadoPermitidoActualmente() {
        // Arrange - Crear primer gestor
        CrearGestorRequest primerGestor = new CrearGestorRequest();
        primerGestor.setNombre("Pedro");
        primerGestor.setApellidos("García");
        primerGestor.setEmail("pedro.garcia@esi.uclm.es");
        primerGestor.setContrasenia("PedroPass123!");
        primerGestor.setAlias("PedroG"); // Mismo alias
        primerGestor.setEspecialidad("Edición");
        primerGestor.setTipoContenido("Video");
        
        ResponseEntity<Map> response1 = restTemplate.postForEntity(baseUrl + "/crear", primerGestor, Map.class);

        // Intentar crear segundo gestor con mismo alias
        CrearGestorRequest segundoGestor = new CrearGestorRequest();
        segundoGestor.setNombre("Pablo");
        segundoGestor.setApellidos("García");
        segundoGestor.setEmail("pablo.garcia@esi.uclm.es");
        segundoGestor.setContrasenia("PabloPass456!");
        segundoGestor.setAlias("PedroG"); // Alias duplicado
        segundoGestor.setEspecialidad("Producción");
        segundoGestor.setTipoContenido("Audio");

        // Act
        ResponseEntity<Map> response2 = restTemplate.postForEntity(
            baseUrl + "/crear",
            segundoGestor,
            Map.class
        );

        // Assert - Actualmente el sistema PERMITE alias duplicados
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        assertEquals(HttpStatus.CREATED, response2.getStatusCode(),
            "Actualmente permite alias duplicados - validación pendiente de implementar");
        
        // Verificar que existen dos gestores con ese alias
        long count = mongoTemplate.getCollection("users")
            .countDocuments(new Document("alias", "PedroG"));
        
        assertEquals(2, count, "Actualmente permite múltiples gestores con el mismo alias");
    }

    // ==================== PRUEBAS DE VALIDACIÓN DE CAMPOS OBLIGATORIOS ====================

    @Test
    @DisplayName("Test 8: Permitir creación sin alias (validación pendiente)")
    void testCreacionSinAlias() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Gestor");
        request.setApellidos("SinAlias");
        request.setEmail("gestor.sin.alias@esi.uclm.es");
        request.setContrasenia("Pass123!");
        request.setAlias(null); // Sin alias
        request.setEspecialidad("Testing");
        request.setTipoContenido("Video");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear",
            request,
            Map.class
        );

        // Assert - Actualmente permite valores nulos
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
            "Actualmente permite creación sin alias - validación pendiente");
        
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "gestor.sin.alias@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD);
        assertNull(gestorEnBD.getString("alias"), "El alias se guarda como null");
    }

    @Test
    @DisplayName("Test 9: Permitir creación sin especialidad (validación pendiente)")
    void testCreacionSinEspecialidad() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Gestor");
        request.setApellidos("SinEspecialidad");
        request.setEmail("gestor.sin.especialidad@esi.uclm.es");
        request.setContrasenia("Pass123!");
        request.setAlias("GestorSE");
        request.setEspecialidad(null); // Sin especialidad
        request.setTipoContenido("Audio");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear",
            request,
            Map.class
        );

        // Assert - Actualmente permite valores nulos
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
            "Actualmente permite creación sin especialidad - validación pendiente");
            
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "gestor.sin.especialidad@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD);
        assertNull(gestorEnBD.getString("campoespecializacion"), "La especialidad se guarda como null");
    }

    @Test
    @DisplayName("Test 10: Permitir creación sin tipo de contenido (validación pendiente)")
    void testCreacionSinTipoContenido() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Gestor");
        request.setApellidos("SinTipo");
        request.setEmail("gestor.sin.tipo@esi.uclm.es");
        request.setContrasenia("Pass123!");
        request.setAlias("GestorST");
        request.setEspecialidad("General");
        request.setTipoContenido(null); // Sin tipo de contenido

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear",
            request,
            Map.class
        );

        // Assert - Actualmente permite valores nulos
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
            "Actualmente permite creación sin tipo de contenido - validación pendiente");
            
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "gestor.sin.tipo@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD);
        assertNull(gestorEnBD.getString("tipocontenidovideooaudio"), "El tipo de contenido se guarda como null");
    }

    // ==================== PRUEBAS DE CREACIÓN DE CONTRASEÑA ====================

    @Test
    @DisplayName("Test 11: Verificar que se crea el documento de contraseña en la colección 'contrasenias'")
    void testCreacionDocumentoContrasenia() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Roberto");
        request.setApellidos("Sánchez");
        request.setEmail("roberto.sanchez@esi.uclm.es");
        request.setContrasenia("RobertoPass123!");
        request.setAlias("RobertoS");
        request.setDescripcion("Especialista en contenido digital");
        request.setEspecialidad("Marketing Digital");
        request.setTipoContenido("Video");
        request.setFoto("perfil3.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        // Verificar desde la BD que el gestor tiene contraseña
        Document gestorBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "roberto.sanchez@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorBD, "Gestor debe existir en BD");
        Object contraseniaObj = gestorBD.get("contrasenia");
        assertNotNull(contraseniaObj, "Debe devolver el objeto de contraseña");
        
        // Verificar que al menos existe alguna contraseña en la colección contrasenias
        long countContrasenias = mongoTemplate.getCollection("contrasenias").countDocuments();
        assertTrue(countContrasenias > 0, "Debe existir al menos una contraseña en la colección contrasenias");
    }

    @Test
    @DisplayName("Test 12: Verificar que el gestor tiene referencia DBRef a su contraseña")
    void testReferenciaDBRefContrasenia() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Laura");
        request.setApellidos("Jiménez");
        request.setEmail("laura.jimenez@esi.uclm.es");
        request.setContrasenia("LauraPass456!");
        request.setAlias("LauraJ");
        request.setDescripcion("Creadora de contenido educativo");
        request.setEspecialidad("Educación Digital");
        request.setTipoContenido("Video");
        request.setFoto("perfil4.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "laura.jimenez@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD);
        assertNotNull(gestorEnBD.get("contrasenia"),
            "El gestor debe tener el campo contrasenia");
        
        // Verificar que es un DBRef
        Object contraseniaField = gestorEnBD.get("contrasenia");
        assertTrue(contraseniaField instanceof com.mongodb.DBRef,
            "El campo contrasenia debe ser un DBRef");
        
        com.mongodb.DBRef dbRef = (com.mongodb.DBRef) contraseniaField;
        assertEquals("contrasenias", dbRef.getCollectionName(),
            "El DBRef debe apuntar a la colección 'contrasenias'");
    }

    // ==================== PRUEBAS DE VALORES POR DEFECTO ====================

    @Test
    @DisplayName("Test 13: Verificar que el gestor NO está bloqueado por defecto")
    void testGestorNoBloqueadoPorDefecto() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Diego");
        request.setApellidos("Vargas");
        request.setEmail("diego.vargas@esi.uclm.es");
        request.setContrasenia("DiegoPass789!");
        request.setAlias("DiegoV");
        request.setEspecialidad("Testing de Contenido");
        request.setTipoContenido("Audio");

        // Act
        restTemplate.postForEntity(baseUrl + "/crear", request, Map.class);

        // Assert
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "diego.vargas@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD);
        assertFalse(gestorEnBD.getBoolean("bloqueado"),
            "El gestor NO debe estar bloqueado por defecto");
    }

    @Test
    @DisplayName("Test 14: Verificar que se establece la fecha de registro")
    void testFechaRegistroEstablecida() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Sofia");
        request.setApellidos("Herrera");
        request.setEmail("sofia.herrera@esi.uclm.es");
        request.setContrasenia("SofiaPass123!");
        request.setAlias("SofiaH");
        request.setEspecialidad("QA de Contenido");
        request.setTipoContenido("Video");

        // Act
        restTemplate.postForEntity(baseUrl + "/crear", request, Map.class);

        // Assert
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "sofia.herrera@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD);
        assertNotNull(gestorEnBD.getDate("fecharegistro"),
            "Debe establecerse automáticamente la fecha de registro");
    }

    @Test
    @DisplayName("Test 15: Verificar que se inicializan las listas generadas vacías")
    void testListasGeneradasVacias() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Miguel");
        request.setApellidos("Castro");
        request.setEmail("miguel.castro@esi.uclm.es");
        request.setContrasenia("MiguelPass456!");
        request.setAlias("MiguelC");
        request.setEspecialidad("Gestión de Listas");
        request.setTipoContenido("Video");

        // Act
        restTemplate.postForEntity(baseUrl + "/crear", request, Map.class);

        // Assert
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "miguel.castro@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD);
        assertNotNull(gestorEnBD.get("listasgeneradas"),
            "Debe tener el campo listasgeneradas");
        assertTrue(gestorEnBD.get("listasgeneradas") instanceof java.util.List,
            "listasgeneradas debe ser una lista");
        assertEquals(0, ((java.util.List<?>) gestorEnBD.get("listasgeneradas")).size(),
            "La lista de listas generadas debe estar vacía inicialmente");
    }

    // ==================== PRUEBAS DE INTEGRIDAD DE DATOS ====================

    @Test
    @DisplayName("Test 16: Verificar que todos los campos específicos del gestor se guardan correctamente")
    void testTodosCamposEspecificosGuardados() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Andrea");
        request.setApellidos("Méndez Ruiz");
        request.setEmail("andrea.mendez@esi.uclm.es");
        request.setContrasenia("AndreaPass789!");
        request.setAlias("AndreaM");
        request.setDescripcion("Especialista en contenido multimedia interactivo");
        request.setEspecialidad("Realidad Virtual");
        request.setTipoContenido("Video");
        request.setFoto("perfil1.png");

        // Act
        restTemplate.postForEntity(baseUrl + "/crear", request, Map.class);

        // Assert
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "andrea.mendez@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorEnBD);
        assertEquals("Andrea", gestorEnBD.getString("nombre"));
        assertEquals("Méndez Ruiz", gestorEnBD.getString("apellidos"));
        assertEquals("andrea.mendez@esi.uclm.es", gestorEnBD.getString("email"));
        assertEquals("perfil1.png", gestorEnBD.getString("foto"));
        assertEquals("AndreaM", gestorEnBD.getString("alias"));
        assertEquals("Especialista en contenido multimedia interactivo", gestorEnBD.getString("descripcion"));
        assertEquals("Realidad Virtual", gestorEnBD.getString("campoespecializacion"));
        assertEquals("Video", gestorEnBD.getString("tipocontenidovideooaudio"));
        assertFalse(gestorEnBD.getBoolean("bloqueado"));
        assertNotNull(gestorEnBD.getDate("fecharegistro"));
        assertEquals("iso25.g05.esi_media.model.GestordeContenido", gestorEnBD.getString("_class"));
    }

    @Test
    @DisplayName("Test 17: Crear múltiples gestores secuencialmente")
    void testCrearMultiplesGestores() {
        // Arrange & Act
        for (int i = 1; i <= 5; i++) {
            CrearGestorRequest request = new CrearGestorRequest();
            request.setNombre("Gestor" + i);
            request.setApellidos("Apellido" + i);
            request.setEmail("gestor" + i + "@esi.uclm.es");
            request.setContrasenia("Pass" + i + "123!");
            request.setAlias("Gestor" + i);
            request.setDescripcion("Descripción del gestor " + i);
            request.setEspecialidad("Especialidad" + i);
            request.setTipoContenido(i % 2 == 0 ? "Video" : "Audio");
            request.setFoto("perfil" + (i % 4 + 1) + ".png");

            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/crear",
                request,
                Map.class
            );

            assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Debe crear el gestor " + i + " exitosamente");
        }

        // Assert
        long count = mongoTemplate.getCollection("users").countDocuments();
        assertEquals(5, count, "Deben existir 5 gestores en la BD");
    }

    @Test
    @DisplayName("Test 18: Verificar respuesta contiene todos los datos esperados del gestor")
    void testRespuestaContieneTodasPropiedadesGestor() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Elena");
        request.setApellidos("Vega");
        request.setEmail("elena.vega@esi.uclm.es");
        request.setContrasenia("ElenaPass123!");
        request.setAlias("ElenaV");
        request.setDescripcion("Gestora de contenido digital");
        request.setEspecialidad("Marketing de Contenidos");
        request.setTipoContenido("Video");
        request.setFoto("perfil2.png");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        
        assertNotNull(body);
        // No hay campo 'mensaje', es un GestordeContenido directamente
        assertTrue(body.containsKey("email"), "Debe contener el campo 'email'");
        assertTrue(body.containsKey("nombre"), "Debe contener el campo 'nombre'");
        assertTrue(body.containsKey("alias"), "Debe contener el campo 'alias'");
        assertTrue(body.containsKey("campoespecializacion"), "Debe contener el campo 'campoespecializacion'");
        // assertTrue(body.containsKey("contrasenia"), "Debe contener el campo 'contrasenia'");
        
        assertEquals("Elena", body.get("nombre"));
        assertEquals("elena.vega@esi.uclm.es", body.get("email"));
        assertEquals("ElenaV", body.get("alias"));
        assertEquals("Marketing de Contenidos", body.get("campoespecializacion"));
        // No hay campo 'tipo' en el modelo GestordeContenido
        
        // Verificar en BD que los demás campos se guardaron correctamente
        Document gestorEnBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "elena.vega@esi.uclm.es"))
            .first();
        assertEquals("Vega", gestorEnBD.getString("apellidos"));
        assertEquals("Gestora de contenido digital", gestorEnBD.getString("descripcion"));
    }

    @Test
    @DisplayName("Test 19: Verificar que las contraseñas de gestores se almacenan correctamente")
    void testAlmacenamientoContraseniaGestor() {
        // Arrange
        CrearGestorRequest request = new CrearGestorRequest();
        request.setNombre("Javier");
        request.setApellidos("Romero");
        request.setEmail("javier.romero@esi.uclm.es");
        request.setContrasenia("JavierSecure123!");
        request.setAlias("JavierR");
        request.setEspecialidad("Seguridad de Contenido");
        request.setTipoContenido("Video");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/crear",
            request,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        // Verificar desde la BD que el gestor tiene contraseña
        Document gestorBD = mongoTemplate.getCollection("users")
            .find(new Document("email", "javier.romero@esi.uclm.es"))
            .first();
        
        assertNotNull(gestorBD, "Gestor debe existir en BD");
        Object contraseniaObj = gestorBD.get("contrasenia");
        assertNotNull(contraseniaObj, "Debe tener una contraseña asignada");
        
        // Verificar que al menos existe alguna contraseña en la colección contrasenias
        long countContrasenias = mongoTemplate.getCollection("contrasenias").countDocuments();
        assertTrue(countContrasenias > 0, "Debe existir al menos una contraseña en la colección contrasenias");
    }

    @Test
    @DisplayName("Test 20: Verificar que cada gestor tiene una contraseña única e independiente")
    void testContrasenasIndependientesGestores() {
        // Arrange
        CrearGestorRequest gestor1 = new CrearGestorRequest();
        gestor1.setNombre("Gestor1");
        gestor1.setApellidos("Test1");
        gestor1.setEmail("gestor1@test.com");
        gestor1.setContrasenia("Password1!");
        gestor1.setAlias("G1");
        gestor1.setEspecialidad("Test1");
        gestor1.setTipoContenido("Audio");
        
        CrearGestorRequest gestor2 = new CrearGestorRequest();
        gestor2.setNombre("Gestor2");
        gestor2.setApellidos("Test2");
        gestor2.setEmail("gestor2@test.com");
        gestor2.setContrasenia("Password2!");
        gestor2.setAlias("G2");
        gestor2.setEspecialidad("Test2");
        gestor2.setTipoContenido("Video");

        // Act
        ResponseEntity<Map> response1 = restTemplate.postForEntity(
            baseUrl + "/crear",
            gestor1,
            Map.class
        );
        
        ResponseEntity<Map> response2 = restTemplate.postForEntity(
            baseUrl + "/crear",
            gestor2,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        
        // Verificar desde la BD que cada gestor tiene su propia contraseña
        Document gestor1BD = mongoTemplate.getCollection("users")
            .find(new Document("email", "gestor1@test.com"))
            .first();
        Document gestor2BD = mongoTemplate.getCollection("users")
            .find(new Document("email", "gestor2@test.com"))
            .first();
        
        assertNotNull(gestor1BD, "Gestor1 debe existir en BD");
        assertNotNull(gestor2BD, "Gestor2 debe existir en BD");
        
        Object contrasenia1Obj = gestor1BD.get("contrasenia");
        Object contrasenia2Obj = gestor2BD.get("contrasenia");
        
        assertNotNull(contrasenia1Obj, "Gestor1 debe tener contraseña");
        assertNotNull(contrasenia2Obj, "Gestor2 debe tener contraseña");
        
        // Extraer IDs de contraseña
        String contraseniaId1;
        String contraseniaId2;
        
        if (contrasenia1Obj instanceof ObjectId) {
            contraseniaId1 = ((ObjectId) contrasenia1Obj).toHexString();
        } else if (contrasenia1Obj instanceof Document) {
            Object idObj = ((Document) contrasenia1Obj).get("id");
            contraseniaId1 = idObj instanceof ObjectId ? ((ObjectId) idObj).toHexString() : (String) idObj;
        } else {
            contraseniaId1 = contrasenia1Obj.toString();
        }
        
        if (contrasenia2Obj instanceof ObjectId) {
            contraseniaId2 = ((ObjectId) contrasenia2Obj).toHexString();
        } else if (contrasenia2Obj instanceof Document) {
            Object idObj = ((Document) contrasenia2Obj).get("id");
            contraseniaId2 = idObj instanceof ObjectId ? ((ObjectId) idObj).toHexString() : (String) idObj;
        } else {
            contraseniaId2 = contrasenia2Obj.toString();
        }
        
        assertNotEquals(contraseniaId1, contraseniaId2,
            "Cada gestor debe tener su propia contraseña con ID único");
        
        // Verificar que ambas contraseñas existen en la BD
        long count = mongoTemplate.getCollection("contrasenias").countDocuments();
        assertEquals(2, count, "Deben existir 2 documentos de contraseña independientes");
    }
}
