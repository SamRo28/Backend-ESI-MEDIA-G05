package iso25.g05.esi_media.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import iso25.g05.esi_media.model.Codigorecuperacion;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.CodigoRecuperacionRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;

/**
 * Tests unitarios para EmailService
 * Valida el envío de emails a través de API HTTP (sin JavaMailSender)
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CodigoRecuperacionRepository codigoRecuperacionRepository;

    @Spy
    @InjectMocks
    private EmailService emailService;

    @Mock
    private RestTemplate restTemplate;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Configurar valores de properties usando ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "apiUrl", "https://api.test.com/email");
        ReflectionTestUtils.setField(emailService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(emailService, "senderName", "ESIMedia Test");
        ReflectionTestUtils.setField(emailService, "senderAddress", "noreply@esimedia.test");
        ReflectionTestUtils.setField(emailService, "backendUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:3000");
        
        // Inyectar el RestTemplate mockeado
        ReflectionTestUtils.setField(emailService, "restTemplate", restTemplate);

        // Usuario de prueba
        usuario = new Usuario();
        usuario.setId("test-user-id");
        usuario.setEmail("test@example.com");
        usuario.setNombre("Test User");
    }

    @Test
    void testSend3FAEmail_EnviaCorreoYGuardaCodigo() {
        // Arrange
        Codigorecuperacion codigoMock = new Codigorecuperacion(usuario);
        when(codigoRecuperacionRepository.save(any(Codigorecuperacion.class))).thenReturn(codigoMock);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        // Act
        Codigorecuperacion resultado = emailService.send3FAemail("test@example.com", usuario);

        // Assert
        assertNotNull(resultado);
        verify(codigoRecuperacionRepository, times(1)).save(any(Codigorecuperacion.class));
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void testSendActivationEmail_GeneraTokenYEnviaCorreo() {
        // Arrange
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        // Act
        String token = emailService.sendActivationEmail(usuario);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        
        // Verificar que el usuario tiene el token asignado
        assertEquals(token, usuario.getActivationToken());
        assertFalse(usuario.isHasActivated());
        
        // Verificar llamada a RestTemplate
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void testSendPasswordResetEmail_EnviaCorreoConToken() {
        // Arrange
        String resetToken = "reset-token-123";
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        // Act & Assert - No debe lanzar excepción
        assertDoesNotThrow(() -> 
            emailService.sendPasswordResetEmail("test@example.com", resetToken)
        );

        // Verificar llamada a RestTemplate
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void testSendPasswordChangedEmail_EnviaCorreoConfirmacion() {
        // Arrange
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        // Act & Assert - No debe lanzar excepción
        assertDoesNotThrow(() -> 
            emailService.sendPasswordChangedEmail("test@example.com")
        );

        // Verificar llamada a RestTemplate
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void testSend3FAEmail_ErrorEnAPI_LanzaExcepcion() {
        // Arrange
        when(codigoRecuperacionRepository.save(any(Codigorecuperacion.class)))
            .thenReturn(new Codigorecuperacion(usuario));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            emailService.send3FAemail("test@example.com", usuario)
        );

        // Verificar que se lanzó la excepción (el mensaje puede variar según la implementación)
        assertNotNull(exception);
        assertNotNull(exception.getMessage());
    }

    @Test
    void testSend3FAEmail_ExcepcionDeRed_LanzaExcepcion() {
        // Arrange
        when(codigoRecuperacionRepository.save(any(Codigorecuperacion.class)))
            .thenReturn(new Codigorecuperacion(usuario));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new RestClientException("Connection timeout"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            emailService.send3FAemail("test@example.com", usuario)
        );

        assertTrue(exception.getMessage().contains("Error al enviar el correo de confirmación vía API"));
    }

    @Test
    void testGenerateConfirmationToken_GeneraTokenValido() {
        // Act
        String token1 = emailService.generateConfirmationToken();
        String token2 = emailService.generateConfirmationToken();

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertFalse(token1.isEmpty());
        assertFalse(token2.isEmpty());
        // Cada token debe ser único
        assertNotEquals(token1, token2);
    }
}
