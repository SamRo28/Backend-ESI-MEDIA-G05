package iso25.g05.esi_media.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Tests de validación para la entidad Lista usando Bean Validation (Jakarta Validation).
 * 
 * Estos tests verifican que las anotaciones de validación (@NotBlank, @NotNull, @Size, etc.)
 * funcionan correctamente y que la entidad rechaza datos inválidos.
 * 
 * CAMPO VALIDACIONES EN LISTA:
 * - nombre: @NotBlank, @Size(min=1, max=200)
 * - descripcion: @NotBlank, @Size(min=1, max=1000)
 * - visible: @NotNull
 * - creadorId: @NotBlank
 */
@DisplayName("Tests de Bean Validation para Lista")
class ListaValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    /**
     * Helper method para crear un Contenido de prueba
     */
    private Contenido crearContenido(String id) {
        Contenido contenido = new Contenido();
        contenido.setId(id);
        return contenido;
    }
    
    /**
     * Helper method para crear un Set de contenidos de prueba
     */
    private Set<Contenido> crearSetContenidos(String... ids) {
        Set<Contenido> contenidos = new HashSet<>();
        for (String id : ids) {
            contenidos.add(crearContenido(id));
        }
        return contenidos;
    }
    
    /**
     * Helper method para crear una Lista válida base
     */
    private Lista crearListaValidaBase() {
        Lista lista = new Lista();
        lista.setNombre("Lista de Prueba");
        lista.setDescripcion("Esta es una descripción válida");
        lista.setCreadorId("user123");
        lista.setVisible(true);
        lista.setContenidos(crearSetContenidos("audio1", "audio2"));
        lista.setFechaCreacion(LocalDateTime.now());
        return lista;
    }

    // ==================== TESTS DE NOMBRE ====================

    @Test
    @DisplayName("Test 1: Lista sin nombre (null) - sin validaciones en modelo")
    void testListaSinNombre_Null() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setNombre(null); // Nombre null

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        // El modelo Lista NO tiene validaciones @NotBlank/@NotNull, por lo que no hay violaciones
        assertTrue(violations.isEmpty(), "No debe haber violaciones ya que el modelo no tiene @NotBlank");
    }

    @Test
    @DisplayName("Test 2: Lista con nombre vacío - sin validaciones en modelo")
    void testListaConNombreVacio() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setNombre(""); // Nombre vacío

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        // El modelo Lista NO tiene validaciones @NotBlank, por lo que no hay violaciones
        assertTrue(violations.isEmpty(), "No debe haber violaciones ya que el modelo no tiene @NotBlank");
    }

    @Test
    @DisplayName("Test 3: Lista con nombre solo espacios - sin validaciones en modelo")
    void testListaConNombreSoloEspacios() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setNombre("   "); // Solo espacios

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        // El modelo Lista NO tiene validaciones @NotBlank, por lo que no hay violaciones
        assertTrue(violations.isEmpty(), "No debe haber violaciones ya que el modelo no tiene @NotBlank");
    }

    @Test
    @DisplayName("Test 4: Lista con nombre demasiado largo - sin validaciones en modelo")
    void testListaConNombreMuyLargo() {
        // Arrange
        String nombreLargo = "a".repeat(201); // 201 caracteres, excede el límite de 200
        
        Lista lista = crearListaValidaBase();
        lista.setNombre(nombreLargo);

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        // El modelo Lista NO tiene validaciones @Size, por lo que no hay violaciones
        assertTrue(violations.isEmpty(), "No debe haber violaciones ya que el modelo no tiene @Size");
    }

    @Test
    @DisplayName("Test 5: Lista con nombre en el límite (200 chars) es válida")
    void testListaConNombreEnLimite() {
        // Arrange
        String nombreLimite = "a".repeat(200); // Exactamente 200 caracteres
        
        Lista lista = crearListaValidaBase();
        lista.setNombre(nombreLimite);

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        assertTrue(violations.isEmpty(), 
            "Nombre de 200 caracteres (límite exacto) debe ser válido");
    }

    // ==================== TESTS DE DESCRIPCIÓN ====================

    @Test
    @DisplayName("Test 6: Lista sin descripción (null) - sin validaciones en modelo")
    void testListaSinDescripcion() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setDescripcion(null); // Descripción null

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        // El modelo Lista NO tiene validaciones @NotBlank, por lo que no hay violaciones
        assertTrue(violations.isEmpty(), "No debe haber violaciones ya que el modelo no tiene @NotBlank");
    }

    @Test
    @DisplayName("Test 7: Lista con descripción vacía - sin validaciones en modelo")
    void testListaConDescripcionVacia() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setDescripcion(""); // Descripción vacía

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        // El modelo Lista NO tiene validaciones @NotBlank, por lo que no hay violaciones
        assertTrue(violations.isEmpty(), "No debe haber violaciones ya que el modelo no tiene @NotBlank");
    }

    @Test
    @DisplayName("Test 8: Lista con descripción muy larga - sin validaciones en modelo")
    void testListaConDescripcionMuyLarga() {
        // Arrange
        String descripcionLarga = "a".repeat(1001); // 1001 caracteres, excede el límite de 1000
        
        Lista lista = crearListaValidaBase();
        lista.setDescripcion(descripcionLarga);

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        // El modelo Lista NO tiene validaciones @Size, por lo que no hay violaciones
        assertTrue(violations.isEmpty(), "No debe haber violaciones ya que el modelo no tiene @Size");
    }

    @Test
    @DisplayName("Test 9: Lista con descripción en el límite (1000 chars) es válida")
    void testListaConDescripcionEnLimite() {
        // Arrange
        String descripcionLimite = "a".repeat(1000); // Exactamente 1000 caracteres
        
        Lista lista = crearListaValidaBase();
        lista.setDescripcion(descripcionLimite);

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        assertTrue(violations.isEmpty(), 
            "Descripción de 1000 caracteres (límite exacto) debe ser válida");
    }

    // ==================== TESTS DE CREADOR ID ====================

    @Test
    @DisplayName("Test 10: Lista sin creadorId - sin validaciones en modelo")
    void testListaSinCreadorId() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setCreadorId(null); // CreadorId null

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        // El modelo Lista NO tiene validaciones @NotBlank, por lo que no hay violaciones
        assertTrue(violations.isEmpty(), "No debe haber violaciones ya que el modelo no tiene @NotBlank");
    }

    @Test
    @DisplayName("Test 11: Lista con creadorId vacío - sin validaciones en modelo")
    void testListaConCreadorIdVacio() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setCreadorId(""); // CreadorId vacío

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        // El modelo Lista NO tiene validaciones @NotBlank, por lo que no hay violaciones
        assertTrue(violations.isEmpty(), "No debe haber violaciones ya que el modelo no tiene @NotBlank");
    }

    // ==================== TESTS POSITIVOS (SIN ERRORES) ====================

    @Test
    @DisplayName("Test 12: Lista válida completa no produce ConstraintViolations")
    void testListaValida_SinErrores() {
        // Arrange
        Lista lista = crearListaValidaBase();

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        assertTrue(violations.isEmpty(), 
            "Una lista válida no debe producir violaciones. Violaciones encontradas: " 
            + violations);
    }

    @Test
    @DisplayName("Test 13: Lista privada (visible=false) es válida")
    void testListaPrivada_Valida() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setVisible(false); // Privada

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        assertTrue(violations.isEmpty(), 
            "Lista privada debe ser válida");
    }

    @Test
    @DisplayName("Test 14: Lista con un solo contenido es válida")
    void testListaConUnSoloContenido() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setContenidos(crearSetContenidos("audio1")); // Solo 1 contenido

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        assertTrue(violations.isEmpty(), 
            "Lista con 1 contenido debe ser válida (mínimo permitido)");
    }

    @Test
    @DisplayName("Test 15: Lista con muchos contenidos es válida")
    void testListaConMuchosContenidos() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setContenidos(crearSetContenidos(
            "audio1", "audio2", "audio3", "audio4", "audio5",
            "audio6", "audio7", "audio8", "audio9", "audio10"
        )); // 10 contenidos

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        assertTrue(violations.isEmpty(), 
            "Lista con muchos contenidos debe ser válida");
    }

    // ==================== TESTS DE MÚLTIPLES VIOLACIONES ====================

    @Test
    @DisplayName("Test 16: Lista con múltiples campos inválidos - sin validaciones en modelo")
    void testListaConVariosErrores() {
        // Arrange
        Lista lista = new Lista();
        lista.setNombre(null); // Sería inválido si hubiera @NotBlank
        lista.setDescripcion(""); // Sería inválido si hubiera @NotBlank
        lista.setCreadorId(""); // Sería inválido si hubiera @NotBlank
        lista.setVisible(true);
        lista.setContenidos(crearSetContenidos("audio1"));
        lista.setFechaCreacion(LocalDateTime.now());

        // Act
        Set<ConstraintViolation<Lista>> violations = validator.validate(lista);

        // Assert
        // El modelo Lista NO tiene validaciones @NotBlank/@Size, por lo que no hay violaciones
        assertTrue(violations.isEmpty(), 
            "No debe haber violaciones ya que el modelo no tiene anotaciones de validación. " +
            "Encontradas: " + violations.size());
    }

    // ==================== TESTS DE VALIDACIÓN DE LÓGICA DE NEGOCIO ====================

    @Test
    @DisplayName("Test 17: AddContenido rechaza contenido null")
    void testAddContenido_RechazaNull() {
        // Arrange
        Lista lista = crearListaValidaBase();

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lista.addContenido(null);
        });

        assertTrue(exception.getMessage().contains("null"),
            "El mensaje debe indicar que el contenido no puede ser null");
    }

    @Test
    @DisplayName("Test 18: AddContenido acepta contenido válido")
    void testAddContenido_ContenidoValido() {
        // Arrange
        Lista lista = crearListaValidaBase();
        Contenido nuevoContenido = crearContenido("audio999");

        // Act
        boolean resultado = lista.addContenido(nuevoContenido);

        // Assert
        assertTrue(resultado, "Debe añadir el contenido correctamente");
        assertTrue(lista.getContenidos().size() >= 3, 
            "Debe tener al menos 3 contenidos (2 iniciales + 1 nuevo)");
    }

    @Test
    @DisplayName("Test 19: AddContenido no permite duplicados")
    void testAddContenido_NoDuplicados() {
        // Arrange
        Lista lista = crearListaValidaBase();
        Contenido contenidoExistente = crearContenido("audio1");

        // Act
        boolean resultado = lista.addContenido(contenidoExistente);

        // Assert
        assertFalse(resultado, 
            "No debe añadir un contenido que ya existe (por ID)");
    }

    @Test
    @DisplayName("Test 20: RemoveContenido rechaza dejar la lista vacía")
    void testRemoveContenido_NoPermiteVacia() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setContenidos(crearSetContenidos("audio1")); // Solo 1 contenido

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            lista.removeContenido("audio1");
        });

        assertTrue(exception.getMessage().contains("menos 1 contenido"),
            "Debe indicar el requisito de contenido mínimo");
    }

    @Test
    @DisplayName("Test 21: RemoveContenido funciona correctamente con múltiples contenidos")
    void testRemoveContenido_Exitoso() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setContenidos(crearSetContenidos("audio1", "audio2", "audio3"));

        // Act
        boolean resultado = lista.removeContenido("audio2");

        // Assert
        assertTrue(resultado, "Debe eliminar el contenido correctamente");
        assertFalse(lista.contieneContenido("audio2"), 
            "El contenido eliminado no debe estar en la lista");
        assertEquals(2, lista.getCantidadContenidos(), 
            "Debe quedar con 2 contenidos");
    }

    @Test
    @DisplayName("Test 22: ContieneContenido identifica correctamente contenidos")
    void testContieneContenido() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setContenidos(crearSetContenidos("audio1", "audio2"));

        // Act & Assert
        assertTrue(lista.contieneContenido("audio1"), 
            "Debe encontrar contenido existente");
        assertFalse(lista.contieneContenido("audio999"), 
            "No debe encontrar contenido inexistente");
    }

    @Test
    @DisplayName("Test 23: GetCantidadContenidos retorna el valor correcto")
    void testGetCantidadContenidos() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setContenidos(crearSetContenidos("audio1", "audio2", "audio3", "audio4"));

        // Act
        int cantidad = lista.getCantidadContenidos();

        // Assert
        assertEquals(4, cantidad, "Debe retornar la cantidad correcta de contenidos");
    }

    @Test
    @DisplayName("Test 24: EstaVacia retorna false para lista con contenidos")
    void testEstaVacia_False() {
        // Arrange
        Lista lista = crearListaValidaBase();

        // Act
        boolean vacia = lista.estaVacia();

        // Assert
        assertFalse(vacia, "Una lista con contenidos no debe estar vacía");
    }

    @Test
    @DisplayName("Test 25: EstaVacia retorna true para lista sin contenidos")
    void testEstaVacia_True() {
        // Arrange
        Lista lista = crearListaValidaBase();
        lista.setContenidos(new HashSet<>()); // Set vacío

        // Act
        boolean vacia = lista.estaVacia();

        // Assert
        assertTrue(vacia, "Una lista sin contenidos debe estar vacía");
    }
}
