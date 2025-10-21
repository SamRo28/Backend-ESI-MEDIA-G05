package iso25.g05.esi_media.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas para la validación del DTO de registro de visualizadores.
 * 
 * Estas pruebas comprueban que las anotaciones de validación aplicadas 
 * al DTO funcionan correctamente para detectar datos inválidos.
 */
@DisplayName("Pruebas de validación de DTO de registro de visualizadores")
public class VisualizadorRegistroDTOTest {

    private Validator validator;
    private VisualizadorRegistroDTO dtoValido;
    
    @BeforeEach
    void setUp() {
        // Configurar el validador de Bean Validation
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Crear un DTO válido para cada prueba
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20); // 20 años
        Date fechaNacimiento = cal.getTime();
        
        dtoValido = new VisualizadorRegistroDTO(
            "Juan",
            "Pérez López",
            "juan.perez@example.com",
            "juanpl",
            fechaNacimiento,
            "Password123!",
            "Password123!",
            false,
            "perfil1.png"
        );
    }
    
    @Test
    @DisplayName("DTO con todos los datos válidos no genera violaciones")
    void dtoValido() {
        // Act
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violaciones = validator.validate(dtoValido);
        
        // Assert
        assertTrue(violaciones.isEmpty(), 
                 "No deben existir violaciones para un DTO válido");
    }
    
    @Test
    @DisplayName("Nombre vacío genera violación")
    void nombreVacioInvalido() {
        // Arrange
        dtoValido.setNombre("");
        
        // Act
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violaciones = validator.validate(dtoValido);
        
        // Assert
        assertFalse(violaciones.isEmpty(), "Debe haber al menos una violación");
        assertTrue(violaciones.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("nombre")),
            "Debe haber una violación para el campo nombre");
    }
    
    @Test
    @DisplayName("Apellidos vacíos generan violación")
    void apellidosVaciosInvalidos() {
        // Arrange
        dtoValido.setApellidos("");
        
        // Act
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violaciones = validator.validate(dtoValido);
        
        // Assert
        assertFalse(violaciones.isEmpty(), "Debe haber al menos una violación");
        assertTrue(violaciones.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("apellidos")),
            "Debe haber una violación para el campo apellidos");
    }
    
    @Test
    @DisplayName("Email con formato inválido genera violación")
    void emailFormatoInvalido() {
        // Arrange
        dtoValido.setEmail("correo-invalido");
        
        // Act
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violaciones = validator.validate(dtoValido);
        
        // Assert
        assertFalse(violaciones.isEmpty(), "Debe haber al menos una violación");
        assertTrue(violaciones.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email")),
            "Debe haber una violación para el campo email");
    }
    
    @Test
    @DisplayName("Alias vacío es válido (es opcional)")
    void aliasVacioInvalido() {
        // Arrange
        dtoValido.setAlias("");
        
        // Act
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violaciones = validator.validate(dtoValido);
        
        // Assert - El alias es opcional, así que un alias vacío no debería generar violaciones
        assertTrue(violaciones.isEmpty(), "No debe haber violaciones para un alias vacío");
    }
    
    @Test
    @DisplayName("Fecha de nacimiento null genera violación")
    void fechaNacimientoNullInvalido() {
        // Arrange
        dtoValido.setFechaNac(null);
        
        // Act
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violaciones = validator.validate(dtoValido);
        
        // Assert
        assertFalse(violaciones.isEmpty(), "Debe haber al menos una violación");
        assertTrue(violaciones.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("fecha_nac")),
            "Debe haber una violación para el campo fecha_nac");
    }
    
    @Test
    @DisplayName("Fecha de nacimiento en el futuro genera violación")
    void fechaNacimientoFuturoInvalido() {
        // Arrange
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1); // Un año en el futuro
        dtoValido.setFechaNac(cal.getTime());
        
        // Act
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violaciones = validator.validate(dtoValido);
        
        // Assert
        assertFalse(violaciones.isEmpty(), "Debe haber al menos una violación");
        assertTrue(violaciones.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("fecha_nac")),
            "Debe haber una violación para el campo fecha_nac");
    }
    
    @Test
    @DisplayName("Contraseña vacía genera violación")
    void passwordVacioInvalido() {
        // Arrange
        dtoValido.setContrasenia("");
        
        // Act
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violaciones = validator.validate(dtoValido);
        
        // Assert
        assertFalse(violaciones.isEmpty(), "Debe haber al menos una violación");
        assertTrue(violaciones.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("contrasenia")),
            "Debe haber una violación para el campo contrasenia");
    }
    
    @Test
    @DisplayName("Confirmación de contraseña vacía genera violación")
    void confirmacionPasswordVacioInvalido() {
        // Arrange
        dtoValido.setConfirmacionContrasenia("");
        
        // Act
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violaciones = validator.validate(dtoValido);
        
        // Assert
        assertFalse(violaciones.isEmpty(), "Debe haber al menos una violación");
        assertTrue(violaciones.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("confirmacion_contrasenia")),
            "Debe haber una violación para el campo confirmacion_contrasenia");
    }
    
    @Test
    @DisplayName("Contraseña demasiado corta genera violación")
    void passwordDemasiadoCorto() {
        // Arrange
        dtoValido.setContrasenia("Abc12!");
        dtoValido.setConfirmacionContrasenia("Abc12!");
        
        // Act
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violaciones = validator.validate(dtoValido);
        
        // Assert
        assertFalse(violaciones.isEmpty(), "Debe haber al menos una violación");
        assertTrue(violaciones.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("contrasenia")),
            "Debe haber una violación para el campo contrasenia");
    }
    
    @Test
    @DisplayName("Constructor completo asigna valores correctamente")
    void constructorCompletoAsignaValores() {
        // Arrange
        String nombre = "Test";
        String apellidos = "Usuario";
        String email = "test@example.com";
        String alias = "testusr";
        Date fechaNacimiento = new Date();
        String contrasenia = "Password123!";
        String confirmacionContrasenia = "Password123!";
        boolean vip = true;
        String foto = "perfil1.png";
        
        // Act
        VisualizadorRegistroDTO dto = new VisualizadorRegistroDTO(
            nombre, apellidos, email, alias, fechaNacimiento,
            contrasenia, confirmacionContrasenia, vip, foto
        );
        
        // Assert
        assertEquals(nombre, dto.getNombre(), "El nombre debe coincidir");
        assertEquals(apellidos, dto.getApellidos(), "Los apellidos deben coincidir");
        assertEquals(email, dto.getEmail(), "El email debe coincidir");
        assertEquals(alias, dto.getAlias(), "El alias debe coincidir");
        assertEquals(fechaNacimiento, dto.getFechaNac(), "La fecha de nacimiento debe coincidir");
        assertEquals(contrasenia, dto.getContrasenia(), "La contraseña debe coincidir");
        assertEquals(confirmacionContrasenia, dto.getConfirmacionContrasenia(), "La confirmación debe coincidir");
        assertEquals(vip, dto.isVip(), "El flag de VIP debe coincidir");
        assertEquals(foto, dto.getFoto(), "El avatar debe coincidir");
    }
    
    @Test
    @DisplayName("Constructor vacío y setters funcionan correctamente")
    void constructorVacioYSetters() {
        // Arrange
        VisualizadorRegistroDTO dto = new VisualizadorRegistroDTO();
        String nombre = "Test";
        String apellidos = "Usuario";
        String email = "test@example.com";
        
        // Act
        dto.setNombre(nombre);
        dto.setApellidos(apellidos);
        dto.setEmail(email);
        
        // Assert
        assertEquals(nombre, dto.getNombre(), "El nombre debe coincidir");
        assertEquals(apellidos, dto.getApellidos(), "Los apellidos deben coincidir");
        assertEquals(email, dto.getEmail(), "El email debe coincidir");
    }
    
    @Test
    @DisplayName("Método toString no es nulo")
    void toStringNoNulo() {
        // Act
        String resultado = dtoValido.toString();
        
        // Assert
        assertNotNull(resultado, "toString() no debe ser nulo");
        assertTrue(resultado.contains(dtoValido.getNombre()), "toString() debe contener el nombre");
        assertTrue(resultado.contains(dtoValido.getEmail()), "toString() debe contener el email");
    }
}