package iso25.g05.esi_media.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import iso25.g05.esi_media.dto.VisualizadorRegistroDTO;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.service.RegistroResultado;
import iso25.g05.esi_media.service.VisualizadorService;

/**
 * Test unitarios para el controlador de registro de visualizadores.
 * Esta clase prueba todas las funcionalidades del controlador de forma aislada,
 * mockeando las dependencias para probar únicamente la lógica del controlador.
 */
@DisplayName("Tests del controlador de visualizadores")
class VisualizadorControllerTest {

    @Mock
    private VisualizadorService visualizadorService;
    
    @Mock
    private BindingResult bindingResult;
    
    @InjectMocks
    private VisualizadorController visualizadorController;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @BeforeEach
    void setUp() {
        // Inicializar los mocks
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    // Helper factory methods kept private for reuse in tests
    private VisualizadorRegistroDTO crearDTOValido() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -25);
        Date fechaNac = cal.getTime();

        return new VisualizadorRegistroDTO(
            "Juan",
            "Pérez García",
            "juan.perez@email.com",
            "juanito",
            fechaNac,
            "MiPassword123!",
            "MiPassword123!",
            false,
            "avatar.jpg"
        );
    }

    private VisualizadorRegistroDTO crearDTOInvalido() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        Date fechaFutura = cal.getTime();

        return new VisualizadorRegistroDTO(
            "",
            "",
            "email-invalido",
            "alias-muy-largo-para-12-caracteres",
            fechaFutura,
            "123",
            "456",
            false,
            null
        );
    }

    private VisualizadorRegistroDTO crearDTOContrasenasNoCoinciden() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20);
        Date fechaNac = cal.getTime();

        return new VisualizadorRegistroDTO(
            "María",
            "González López",
            "maria@email.com",
            "maria",
            fechaNac,
            "Password123!",
            "Password456!",
            true,
            "foto.png"
        );
    }

    private Visualizador crearVisualizadorRegistrado(VisualizadorRegistroDTO dto) {
        Visualizador visualizador = new Visualizador();
        visualizador.setNombre(dto.getNombre());
        visualizador.setApellidos(dto.getApellidos());
        visualizador.setEmail(dto.getEmail());
        visualizador.setAlias(dto.getAlias());
        visualizador.setBloqueado(false);
        visualizador.setFechaNac(dto.getFechaNac());
        visualizador.setVip(dto.isVip());
        visualizador.setContrasenia(new Contrasenia());
        visualizador.setId("abc123");
        return visualizador;
    }

    private String convertirAJSON(VisualizadorRegistroDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Test para verificar que los objetos DTO son serializables a JSON
     */
    @Test
    @DisplayName("Los DTOs deben ser serializables a JSON")
    void dtosDeberianSerSerializablesAJSON() {
        // Arrange
        VisualizadorRegistroDTO dto = crearDTOValido();
        
        // Act
        String json = convertirAJSON(dto);
        
        // Assert
        assertNotNull(json, "La conversión a JSON no debe devolver null");
        assertTrue(json.contains("juan.perez@email.com"), "JSON debe contener el email");
    }

    /**
     * Test para verificar el registro exitoso de un visualizador
     */
    @Test
    @DisplayName("Registro exitoso de visualizador devuelve estado HTTP 201")
    void registroExitosoDebeRetornarEstado201() {
        // Arrange - Configurar datos de prueba
        VisualizadorRegistroDTO dto = crearDTOValido();
        Visualizador visualizadorRegistrado = crearVisualizadorRegistrado(dto);
        
        // Configurar los mocks
        when(bindingResult.hasErrors()).thenReturn(false);
        when(visualizadorService.registrarVisualizador(any(VisualizadorRegistroDTO.class)))
            .thenReturn(new RegistroResultado(visualizadorRegistrado, "Visualizador registrado exitosamente"));
        
        // Act - Ejecutar el método a probar
        ResponseEntity<Map<String, Object>> respuesta = visualizadorController.registrarVisualizador(dto, bindingResult);
        
        // Assert - Verificar resultados
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode(), 
                    "El registro exitoso debe devolver estado HTTP 201 CREATED");
        
        assertNotNull(respuesta.getBody(), "La respuesta debe tener un cuerpo");
        assertTrue((Boolean) respuesta.getBody().get("exitoso"), "El campo 'exitoso' debe ser true");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> visualizadorData = (Map<String, Object>) respuesta.getBody().get("visualizador");
        assertNotNull(visualizadorData, "La respuesta debe incluir los datos del visualizador");
        assertEquals(dto.getNombre(), visualizadorData.get("nombre"), "El nombre debe coincidir");
        assertEquals(dto.getEmail(), visualizadorData.get("email"), "El email debe coincidir");
        
        // Verificar que el servicio fue llamado exactamente una vez
        verify(visualizadorService, times(1)).registrarVisualizador(any(VisualizadorRegistroDTO.class));
    }
    
    /**
     * Test para verificar el manejo de errores de validación a nivel de anotaciones
     */
    @Test
    @DisplayName("Errores de validación de DTO deben retornar estado HTTP 400")
    void erroresValidacionDebenRetornarEstado400() {
        // Arrange - Configurar datos de prueba
        VisualizadorRegistroDTO dto = crearDTOInvalido();
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("visualizadorDTO", "nombre", "El nombre es obligatorio"));
        fieldErrors.add(new FieldError("visualizadorDTO", "email", "El formato del email no es válido"));
        
        // Configurar los mocks
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        
        // Act - Ejecutar el método a probar
        ResponseEntity<Map<String, Object>> respuesta = visualizadorController.registrarVisualizador(dto, bindingResult);
        
        // Assert - Verificar resultados
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode(), 
                    "Los errores de validación deben devolver estado HTTP 400 BAD REQUEST");
        
        assertNotNull(respuesta.getBody(), "La respuesta debe tener un cuerpo");
        assertFalse((Boolean) respuesta.getBody().get("exitoso"), "El campo 'exitoso' debe ser false");
        
        @SuppressWarnings("unchecked")
        List<String> errores = (List<String>) respuesta.getBody().get("errores");
        assertNotNull(errores, "La respuesta debe incluir lista de errores");
        assertEquals(2, errores.size(), "Deben haber 2 errores de validación");
        
        // Verificar que el servicio nunca fue llamado (ya que las validaciones fallaron antes)
        verify(visualizadorService, never()).registrarVisualizador(any(VisualizadorRegistroDTO.class));
    }
    
    /**
     * Test para verificar el manejo de errores de reglas de negocio (validación en servicio)
     */
    @Test
    @DisplayName("Errores de reglas de negocio deben retornar estado HTTP 400")
    void erroresReglasNegocioDebenRetornarEstado400() {
        // Arrange - Configurar datos de prueba
        VisualizadorRegistroDTO dto = crearDTOContrasenasNoCoinciden();
        List<String> erroresNegocio = new ArrayList<>();
        erroresNegocio.add("La contraseña y su confirmación no coinciden");
        
        // Configurar los mocks
        when(bindingResult.hasErrors()).thenReturn(false);
        when(visualizadorService.registrarVisualizador(any(VisualizadorRegistroDTO.class)))
            .thenReturn(new RegistroResultado(erroresNegocio, "Registro fallido: corrija los errores indicados"));
        
        // Act - Ejecutar el método a probar
        ResponseEntity<Map<String, Object>> respuesta = visualizadorController.registrarVisualizador(dto, bindingResult);
        
        // Assert - Verificar resultados
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode(), 
                    "Los errores de reglas de negocio deben devolver estado HTTP 400 BAD REQUEST");
        
        assertNotNull(respuesta.getBody(), "La respuesta debe tener un cuerpo");
        assertFalse((Boolean) respuesta.getBody().get("exitoso"), "El campo 'exitoso' debe ser false");
        
        @SuppressWarnings("unchecked")
        List<String> errores = (List<String>) respuesta.getBody().get("errores");
        assertNotNull(errores, "La respuesta debe incluir lista de errores");
        assertEquals(1, errores.size(), "Debe haber 1 error de regla de negocio");
        assertTrue(errores.contains("La contraseña y su confirmación no coinciden"), 
                  "Debe contener el error específico de contraseñas no coincidentes");
        
        // Verificar que el servicio fue llamado exactamente una vez
        verify(visualizadorService, times(1)).registrarVisualizador(any(VisualizadorRegistroDTO.class));
    }
    
    /**
     * Test para verificar el manejo de error de email duplicado
     */
    @Test
    @DisplayName("Email duplicado debe retornar estado HTTP 400")
    void emailDuplicadoDebeRetornarEstado400() {
        // Arrange - Configurar datos de prueba
        VisualizadorRegistroDTO dto = crearDTOValido();
        
        // Configurar los mocks
        when(bindingResult.hasErrors()).thenReturn(false);
        when(visualizadorService.registrarVisualizador(any(VisualizadorRegistroDTO.class)))
            .thenReturn(new RegistroResultado("El email ya está registrado en el sistema", 
                                           "Registro fallido: email duplicado"));
        
        // Act - Ejecutar el método a probar
        ResponseEntity<Map<String, Object>> respuesta = visualizadorController.registrarVisualizador(dto, bindingResult);
        
        // Assert - Verificar resultados
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode(), 
                    "El email duplicado debe devolver estado HTTP 400 BAD REQUEST");
        
        assertNotNull(respuesta.getBody(), "La respuesta debe tener un cuerpo");
        assertFalse((Boolean) respuesta.getBody().get("exitoso"), "El campo 'exitoso' debe ser false");
        
        @SuppressWarnings("unchecked")
        List<String> errores = (List<String>) respuesta.getBody().get("errores");
        assertNotNull(errores, "La respuesta debe incluir lista de errores");
        assertEquals(1, errores.size(), "Debe haber 1 error de email duplicado");
        assertTrue(errores.contains("El email ya está registrado en el sistema"), 
                  "Debe contener el error específico de email duplicado");
    }
    
    /**
     * Test para verificar la función de activación de 2FA
     */
    @Test
    @DisplayName("Activación exitosa de 2FA debe retornar estado HTTP 200")
    void activacion2FAExitosaDebeRetornarEstado200() {
        // Arrange
        Map<String, String> request = Map.of("email", "juan.perez@email.com");
        String qrCodeUrl = "otpauth://totp/MiApp:juan.perez@email.com?secret=ABC123&issuer=MiApp";
        
        // Configurar mock
        when(visualizadorService.activar2FA(eq("juan.perez@email.com")))
            .thenReturn(qrCodeUrl);
        
        // Act
        ResponseEntity<Map<String, String>> respuesta = visualizadorController.activar2FA(request);
        
        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode(),
                    "La activación exitosa de 2FA debe devolver estado HTTP 200 OK");
        
        assertNotNull(respuesta.getBody(), "La respuesta debe tener un cuerpo");
        assertEquals(qrCodeUrl, respuesta.getBody().get("mensaje"),
                   "La respuesta debe contener la URL del código QR");
        
        // Verificar que el servicio fue llamado exactamente una vez con el email correcto
        verify(visualizadorService, times(1)).activar2FA("juan.perez@email.com");
    }
    
    /**
     * Test para verificar el manejo de error en activación de 2FA
     */
    @Test
    @DisplayName("Activación fallida de 2FA debe retornar estado HTTP 400")
    void activacion2FAFallidaDebeRetornarEstado400() {
        // Arrange
        Map<String, String> request = Map.of("email", "usuario.inexistente@email.com");
        
        // Configurar mock para devolver cadena vacía (error)
        when(visualizadorService.activar2FA(anyString())).thenReturn("");
        
        // Act
        ResponseEntity<Map<String, String>> respuesta = visualizadorController.activar2FA(request);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode(),
                    "La activación fallida de 2FA debe devolver estado HTTP 400 BAD REQUEST");
        
        assertNotNull(respuesta.getBody(), "La respuesta debe tener un cuerpo");
        assertEquals("Error al activar 2FA", respuesta.getBody().get("mensaje"),
                   "La respuesta debe contener el mensaje de error");
    }
}