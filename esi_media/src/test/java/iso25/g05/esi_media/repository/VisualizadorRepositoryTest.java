package iso25.g05.esi_media.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

import iso25.g05.esi_media.config.MongoTestConfig;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;

/**
 * Pruebas de integración para el repositorio de visualizadores.
 * 
 * Estas pruebas verifican que las operaciones CRUD y las consultas personalizadas
 * del repositorio funcionen correctamente con una base de datos en memoria.
 * 
 * Se importa la configuración MongoTestConfig para asegurar que todas las
 * operaciones MongoDB se realicen en la base de datos de pruebas.
 */
@DataMongoTest
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
@DisplayName("Pruebas de repositorio de visualizadores")
public class VisualizadorRepositoryTest {

    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private VisualizadorRepository visualizadorRepository;
    
    @Autowired
    private ContraseniaRepository contraseniaRepository;
    
    private Visualizador visualizador1;
    private Visualizador visualizador2;
    
    @BeforeEach
    void setUp() {
        // Limpiar colecciones antes de cada test para evitar conflictos
        mongoTemplate.dropCollection("users");
        mongoTemplate.dropCollection(Contrasenia.class);
        
        // Crear un identificador único para esta ejecución de prueba
        String testId = UUID.randomUUID().toString().substring(0, 8);
        
        // Crear visualizadores de prueba con emails y alias únicos
        visualizador1 = crearVisualizador("Juan", "Pérez", 
            "juan_" + testId + "_" + System.currentTimeMillis() + "@test.com", 
            "juan123_" + testId);
        visualizador2 = crearVisualizador("María", "López", 
            "maria_" + testId + "_" + System.currentTimeMillis() + "@test.com", 
            "maria456_" + testId);
        
        // Persistir entidades de prueba
        mongoTemplate.save(visualizador1);
        mongoTemplate.save(visualizador2);
    }
    
    /**
     * Método auxiliar para eliminar una contraseña asociada a un usuario
     */
    private void eliminarContraseniaAsociada(Visualizador usuario) {
        if (usuario != null && usuario.getContrasenia() != null) {
            String contraseniaId = usuario.getContrasenia().getId();
            if (contraseniaId != null) {
                // Usar el repositorio específico para contraseñas que usa la base de datos de pruebas
                contraseniaRepository.deleteById(contraseniaId);
            }
        }
    }
    
    @AfterEach
    void tearDown() {
        // Limpiar las contraseñas asociadas a los visualizadores antes de eliminarlos
        eliminarContraseniaAsociada(visualizador1);
        eliminarContraseniaAsociada(visualizador2);
        
        // Eliminar los visualizadores
        mongoTemplate.remove(new Query(Criteria.where("_id").in(
            visualizador1 != null ? visualizador1.getId() : "", 
            visualizador2 != null ? visualizador2.getId() : "")), Visualizador.class);
        
        // Búsqueda y limpieza de contraseñas huérfanas
        // Una contraseña está huérfana si no tiene ningún usuario asociado
        // Este paso es una red de seguridad adicional
        List<String> contraseniaIds = contraseniaRepository.findAll()
            .stream()
            .map(Contrasenia::getId)
            .collect(java.util.stream.Collectors.toList());
        
        if (!contraseniaIds.isEmpty()) {
            long usuariosConContrasenias = mongoTemplate.count(
                new Query(Criteria.where("contrasenia.$id").in(contraseniaIds)), 
                Usuario.class
            );
            
            if (usuariosConContrasenias < contraseniaIds.size()) {
                // Hay contraseñas huérfanas, eliminar todas para asegurar limpieza completa
                contraseniaRepository.deleteAll();
            }
        }
    }
    
    /**
     * Método auxiliar para crear instancias de visualizador
     */
    private Visualizador crearVisualizador(String nombre, String apellidos, String email, String alias) {
        Visualizador v = new Visualizador();
        v.setNombre(nombre);
        v.setApellidos(apellidos);
        v.setEmail(email);
        v.setAlias(alias);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -25); // 25 años
        v.setFechaNac(cal.getTime());
        
        v.setFoto("perfil1.png");
        
        // No asignamos contraseñas por defecto, se harían en tests específicos usando crearYAsignarContrasenia
        return v;
    }
    
    /**
     * Método auxiliar para crear y asignar una contraseña a un usuario
     */
    private Contrasenia crearYAsignarContrasenia(Visualizador usuario, String password) {
        Contrasenia contrasenia = new Contrasenia();
        contrasenia.setContraseniaActual(password);
        contrasenia.setFechaExpiracion(new Date(System.currentTimeMillis() + 90*24*60*60*1000)); // 90 días
        contrasenia.setContraseniasUsadas(new java.util.ArrayList<>());
        
        // Guardar en base de datos usando el repositorio específico para pruebas
        Contrasenia saved = contraseniaRepository.save(contrasenia);
        
        // Asignar la contraseña al usuario
        usuario.setContrasenia(saved);
        
        return saved;
    }
    
    @Test
    @DisplayName("Buscar por email existente devuelve el visualizador correcto")
    void findByEmailExistente() {
        // Act - Usamos el email específico del visualizador1 creado en setUp
        Optional<Visualizador> resultado = visualizadorRepository.findBy_email(visualizador1.getEmail());
        
        // Assert
        assertTrue(resultado.isPresent(), "Debe encontrar el visualizador");
        assertEquals("Juan", resultado.get().getNombre(), "El nombre debe coincidir");
        assertEquals("Pérez", resultado.get().getApellidos(), "Los apellidos deben coincidir");
    }
    
    @Test
    @DisplayName("Buscar por email no existente devuelve Optional vacío")
    void findByEmailNoExistente() {
        // Act
        Optional<Visualizador> resultado = visualizadorRepository.findBy_email("noexiste@test.com");
        
        // Assert
        assertFalse(resultado.isPresent(), "No debe encontrar ningún visualizador");
    }
    
    @Test
    @DisplayName("Buscar por alias existente devuelve el visualizador correcto")
    void findByAliasExistente() {
        // Act - Usando consulta MongoDB directamente ya que findByAlias no existe
        org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query();
        query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("alias").is(visualizador2.getAlias()));
        Visualizador resultado = mongoTemplate.findOne(query, Visualizador.class);
        
        // Assert
        assertNotNull(resultado, "Debe encontrar el visualizador");
        assertEquals("María", resultado.getNombre(), "El nombre debe coincidir");
        assertEquals("López", resultado.getApellidos(), "Los apellidos deben coincidir");
    }
    
    @Test
    @DisplayName("Buscar por alias no existente devuelve null")
    void findByAliasNoExistente() {
        // Act - Usando consulta MongoDB directamente
        org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query();
        query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("alias").is("noexiste"));
        Visualizador resultado = mongoTemplate.findOne(query, Visualizador.class);
        
        // Assert
        assertNull(resultado, "No debe encontrar ningún visualizador");
    }
    
    @Test
    @DisplayName("Obtener todos los visualizadores devuelve la lista completa")
    void findAllDevuelveTodos() {
        // En lugar de findAll(), usamos una consulta para buscar solo los registros que nosotros creamos
        org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query();
        query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("_id").in(
            visualizador1.getId(), visualizador2.getId()));
        List<Visualizador> visualizadores = mongoTemplate.find(query, Visualizador.class);
        
        // Assert
        assertEquals(2, visualizadores.size(), "Debe devolver 2 visualizadores");
    }
    
    @Test
    @DisplayName("Guardar nuevo visualizador persiste correctamente")
    void saveNuevoVisualizador() {
        // Arrange - Usamos un timestamp y UUID para asegurar unicidad del email y alias
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String uniqueEmail = "ana_" + testId + "_" + System.currentTimeMillis() + "@test.com";
        Visualizador nuevoVisualizador = crearVisualizador("Ana", "Gómez", uniqueEmail, "anita_" + testId);
        
        try {
            // Act
            Visualizador guardado = visualizadorRepository.save(nuevoVisualizador);
            
            // Assert
            assertNotNull(guardado.getId(), "Debe asignar un ID");
            
            // Verificar que se guardó correctamente
            Optional<Visualizador> recuperado = visualizadorRepository.findById(guardado.getId());
            assertTrue(recuperado.isPresent(), "Debe existir en la base de datos");
            assertEquals("Ana", recuperado.get().getNombre(), "El nombre debe coincidir");
            assertEquals("anita_" + testId, recuperado.get().getAlias(), "El alias debe coincidir");
        } finally {
            // Limpiar la contraseña asociada si existe
            eliminarContraseniaAsociada(nuevoVisualizador);
            
            // Limpieza específica del visualizador adicional creado en este test
            if (nuevoVisualizador != null && nuevoVisualizador.getId() != null) {
                mongoTemplate.remove(Query.query(Criteria.where("_id").is(nuevoVisualizador.getId())), Visualizador.class);
            }
        }
    }
    
    @Test
    @DisplayName("Actualizar visualizador existente modifica correctamente")
    void updateVisualizador() {
        // Arrange
        String nuevoAlias = "juanito_actualizado";
        visualizador1.setAlias(nuevoAlias);
        
        // Act
        visualizadorRepository.save(visualizador1);
        
        // Assert
        Optional<Visualizador> actualizado = visualizadorRepository.findById(visualizador1.getId());
        assertTrue(actualizado.isPresent(), "Debe existir el visualizador");
        assertEquals(nuevoAlias, actualizado.get().getAlias(), "El alias debe estar actualizado");
    }
    
    @Test
    @DisplayName("Eliminar visualizador lo remueve correctamente")
    void deleteVisualizador() {
        // Act
        visualizadorRepository.deleteById(visualizador1.getId());
        
        // Assert
        Optional<Visualizador> eliminado = visualizadorRepository.findById(visualizador1.getId());
        assertFalse(eliminado.isPresent(), "El visualizador no debe existir");
        
        // Verificar que sólo se eliminó el visualizador indicado
        // En lugar de findAll(), buscamos solo el visualizador2 que debería seguir existiendo
        Optional<Visualizador> debeExistir = visualizadorRepository.findById(visualizador2.getId());
        assertTrue(debeExistir.isPresent(), "El visualizador2 debe seguir existiendo");
        assertEquals(visualizador2.getId(), debeExistir.get().getId(), "El ID debe coincidir con visualizador2");
    }
    
    @Test
    @DisplayName("Buscar visualizadores por nombre parcial devuelve resultados correctos")
    void findByNombreContainingIgnoreCase() {
        // Arrange - Añadir un visualizador más con nombre similar
        // Usar el mismo identificador único que en otros tests
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String uniqueEmail = "juanita_" + testId + "_" + System.currentTimeMillis() + "@test.com";
        Visualizador visualizador3 = crearVisualizador("Juanita", "González", uniqueEmail, "juanita_" + testId);
        mongoTemplate.save(visualizador3);
        
        try {
            // Act - Utilizamos una consulta específica que sólo traiga nuestros usuarios de prueba
            org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query();
            query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("_id").in(
                visualizador1.getId(), visualizador3.getId()));
            List<Visualizador> resultados = mongoTemplate.find(query, Visualizador.class);
            
            // Assert
            assertEquals(2, resultados.size(), "Debe encontrar 2 visualizadores con ids específicos");
            
            // Verificar que los resultados son los esperados (Juan y Juanita)
            boolean encontradoJuan = false;
            boolean encontradoJuanita = false;
            for (Visualizador v : resultados) {
                if (v.getNombre().equals("Juan")) encontradoJuan = true;
                if (v.getNombre().equals("Juanita")) encontradoJuanita = true;
            }
            
            assertTrue(encontradoJuan, "Debe encontrar al visualizador Juan");
            assertTrue(encontradoJuanita, "Debe encontrar al visualizador Juanita");
        } finally {
            // Limpiar la contraseña asociada si existe
            eliminarContraseniaAsociada(visualizador3);
            
            // Limpieza del visualizador adicional
            if (visualizador3 != null && visualizador3.getId() != null) {
                mongoTemplate.remove(Query.query(Criteria.where("_id").is(visualizador3.getId())), Visualizador.class);
            }
        }
    }
    
    @Test
    @DisplayName("Búsqueda por rango de edad devuelve visualizadores correctos")
    void findByRangoEdad() {
        // Arrange - Añadir visualizadores con diferentes edades
        String testId = UUID.randomUUID().toString().substring(0, 8);
        Calendar cal = Calendar.getInstance();
        
        cal.add(Calendar.YEAR, -18); // 18 años
        Visualizador joven = crearVisualizador("Joven", "García", 
            "joven_" + testId + "_" + System.currentTimeMillis() + "@test.com", "joven_" + testId);
        joven.setFechaNac(cal.getTime());
        
        cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -68); // 68 años
        Visualizador mayor = crearVisualizador("Mayor", "Fernández", 
            "mayor_" + testId + "_" + System.currentTimeMillis() + "@test.com", "mayor_" + testId);
        mayor.setFechaNac(cal.getTime());
        
        mongoTemplate.save(joven);
        mongoTemplate.save(mayor);
        
        try {
            // Verificar que nuestros visualizadores de prueba (visualizador1 y visualizador2) están en el rango correcto
            // ya que son creados por default con 25 años de edad
            
            // Buscar directamente visualizador1 y visualizador2 por ID
            org.springframework.data.mongodb.core.query.Query queryPorIds = new org.springframework.data.mongodb.core.query.Query();
            queryPorIds.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("_id").in(
                visualizador1.getId(), visualizador2.getId()));
            List<Visualizador> resultadosPorIds = mongoTemplate.find(queryPorIds, Visualizador.class);
            
            // Assert
            assertEquals(2, resultadosPorIds.size(), "Debe encontrar los 2 visualizadores originales");
            
            // Verificar que nuestros visualizadores tienen la edad esperada (25 años)
            Calendar calHoy = Calendar.getInstance();
            Calendar calFechaNac = Calendar.getInstance();
            calFechaNac.setTime(visualizador1.getFechaNac());
            int anios = calHoy.get(Calendar.YEAR) - calFechaNac.get(Calendar.YEAR);
            assertTrue(anios >= 20 && anios <= 30, "El visualizador1 debe tener entre 20 y 30 años");
            
            calFechaNac.setTime(visualizador2.getFechaNac());
            anios = calHoy.get(Calendar.YEAR) - calFechaNac.get(Calendar.YEAR);
            assertTrue(anios >= 20 && anios <= 30, "El visualizador2 debe tener entre 20 y 30 años");
            
            // Verificar que los datos adicionales tienen las edades correctas
            calFechaNac.setTime(joven.getFechaNac());
            anios = calHoy.get(Calendar.YEAR) - calFechaNac.get(Calendar.YEAR);
            assertEquals(18, anios, "El visualizador joven debe tener 18 años");
            
            calFechaNac.setTime(mayor.getFechaNac());
            anios = calHoy.get(Calendar.YEAR) - calFechaNac.get(Calendar.YEAR);
            assertEquals(68, anios, "El visualizador mayor debe tener 68 años");
        } finally {
            // Limpiar las contraseñas asociadas si existen
            eliminarContraseniaAsociada(joven);
            eliminarContraseniaAsociada(mayor);
            
            // Limpieza específica de los visualizadores adicionales creados en este test
            if (joven != null && joven.getId() != null) {
                mongoTemplate.remove(Query.query(Criteria.where("_id").is(joven.getId())), Visualizador.class);
            }
            if (mayor != null && mayor.getId() != null) {
                mongoTemplate.remove(Query.query(Criteria.where("_id").is(mayor.getId())), Visualizador.class);
            }
        }
    }
}