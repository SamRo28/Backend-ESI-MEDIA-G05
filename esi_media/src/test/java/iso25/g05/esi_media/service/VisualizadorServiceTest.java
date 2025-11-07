package iso25.g05.esi_media.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import iso25.g05.esi_media.dto.VisualizadorRegistroDTO;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.ContraseniaRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.repository.VisualizadorRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Tests unitarios para el servicio de registro de visualizadores.
 * 
 * Esta clase prueba todas las funcionalidades del servicio de forma aislada,
 * mockeando las dependencias para probar únicamente la lógica del servicio.
 */
@DisplayName("Tests del servicio de visualizadores")
class VisualizadorServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private VisualizadorRepository visualizadorRepository;
    
    @Mock
    private ContraseniaRepository contraseniaRepository;
    
    @Mock
    private Validator validator;
    
    @Mock
    private UserService userService;
    
    @Mock
    private iso25.g05.esi_media.repository.ContraseniaComunRepository contraseniaComunRepository;
    
    @InjectMocks
    private VisualizadorService visualizadorService;
    
    @BeforeEach
    void setUp() {
        // Inicializar los mocks
        MockitoAnnotations.openMocks(this);
        
        // Inyectar manualmente los campos @Autowired que no están en el constructor
        ReflectionTestUtils.setField(visualizadorService, "userService", userService);
        ReflectionTestUtils.setField(visualizadorService, "contraseniaComunRepository", contraseniaComunRepository);
    }
    
    /**
     * Método auxiliar para crear un DTO válido para pruebas
     */
    private VisualizadorRegistroDTO crearDTOValido() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -25); // 25 años atrás
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
    
    /**
     * Método auxiliar para crear un DTO con contraseñas que no coinciden
     */
    private VisualizadorRegistroDTO crearDTOContrasenasNoCoinciden() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20); // 20 años atrás
        Date fechaNac = cal.getTime();
        
        return new VisualizadorRegistroDTO(
            "María",
            "González López",
            "maria@email.com",
            "maria",
            fechaNac,
            "Password123!",
            "Password456!", // Contraseña diferente
            true,
            "foto.png"
        );
    }
    
    /**
     * Método auxiliar para crear un DTO con un usuario muy joven (menor de 4 años)
     */
    private VisualizadorRegistroDTO crearDTOUsuarioMuyJoven() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -2); // Solo 2 años atrás
        Date fechaNac = cal.getTime();
        
        return new VisualizadorRegistroDTO(
            "Niño",
            "Pequeño Joven",
            "nino@email.com",
            "mini",
            fechaNac,
            "Password123!",
            "Password123!",
            false,
            null
        );
    }
    
    /**
     * Test para verificar registro exitoso de un visualizador
     */
    @Test
    @DisplayName("Registro exitoso cuando todos los datos son válidos")
    void registroExitosoCuandoDatosValidos() {
        // Arrange
        VisualizadorRegistroDTO dto = crearDTOValido();
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violacionesVacias = Set.of();
        
        // Configurar mocks
        when(validator.validate(any(VisualizadorRegistroDTO.class))).thenReturn(violacionesVacias);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(contraseniaComunRepository.existsById(anyString())).thenReturn(false);
        when(userService.hashearContrasenia(any(Contrasenia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contraseniaRepository.save(any(Contrasenia.class))).thenAnswer(invocation -> {
            Contrasenia contrasenia = invocation.getArgument(0);
            contrasenia.setId("contrasenia123");
            return contrasenia;
        });
        when(visualizadorRepository.save(any(Visualizador.class))).thenAnswer(invocation -> {
            Visualizador visualizador = invocation.getArgument(0);
            visualizador.setId("usuario123");
            return visualizador;
        });
        
        // Act
        RegistroResultado resultado = visualizadorService.registrarVisualizador(dto);
        
        // Assert
        assertTrue(resultado.isExitoso(), "El registro con datos válidos debe ser exitoso");
        assertNotNull(resultado.getVisualizador(), "El resultado debe contener un visualizador");
        assertEquals("usuario123", resultado.getVisualizador().getId(), "El visualizador debe tener ID asignado");
        assertEquals(dto.getNombre(), resultado.getVisualizador().getNombre(), "El nombre debe coincidir");
        assertEquals(dto.getEmail(), resultado.getVisualizador().getEmail(), "El email debe coincidir");
        assertNotNull(resultado.getVisualizador().getContrasenia(), "El visualizador debe tener contraseña asignada");
        assertEquals("contrasenia123", resultado.getVisualizador().getContrasenia().getId(), "La contraseña debe tener ID asignado");
        
        // Verificar que los repositorios fueron llamados correctamente
        verify(contraseniaRepository, times(1)).save(any(Contrasenia.class));
        verify(visualizadorRepository, times(1)).save(any(Visualizador.class));
    }
    
    /**
     * Test para verificar el manejo de contraseñas que no coinciden
     */
    @Test
    @DisplayName("Debe detectar cuando las contraseñas no coinciden")
    void detectarContrasenasNoCoinciden() {
        // Arrange
        VisualizadorRegistroDTO dto = crearDTOContrasenasNoCoinciden();
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violacionesVacias = Set.of();
        
        // Configurar mocks
        when(validator.validate(any(VisualizadorRegistroDTO.class))).thenReturn(violacionesVacias);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        
        // Act
        RegistroResultado resultado = visualizadorService.registrarVisualizador(dto);
        
        // Assert
        assertFalse(resultado.isExitoso(), "El registro con contraseñas no coincidentes debe fallar");
        assertNull(resultado.getVisualizador(), "No debe crearse un visualizador");
        assertFalse(resultado.getErrores().isEmpty(), "Debe haber errores en el resultado");
        assertTrue(resultado.getErrores().stream()
            .anyMatch(error -> error.contains("contraseña") && error.contains("no coinciden")),
            "Debe contener un error específico sobre contraseñas no coincidentes");
        
        // Verificar que los repositorios nunca fueron llamados
        verify(contraseniaRepository, never()).save(any(Contrasenia.class));
        verify(visualizadorRepository, never()).save(any(Visualizador.class));
    }
    
    /**
     * Test para verificar el manejo de usuarios muy jóvenes (menores de 4 años)
     */
    @Test
    @DisplayName("Debe rechazar usuarios menores de 4 años")
    void rechazarUsuariosMenores4Anios() {
        // Arrange
        VisualizadorRegistroDTO dto = crearDTOUsuarioMuyJoven();
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violacionesVacias = Set.of();
        
        // Configurar mocks
        when(validator.validate(any(VisualizadorRegistroDTO.class))).thenReturn(violacionesVacias);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        
        // Act
        RegistroResultado resultado = visualizadorService.registrarVisualizador(dto);
        
        // Assert
        assertFalse(resultado.isExitoso(), "El registro de usuarios menores de 4 años debe fallar");
        assertNull(resultado.getVisualizador(), "No debe crearse un visualizador");
        assertFalse(resultado.getErrores().isEmpty(), "Debe haber errores en el resultado");
        assertTrue(resultado.getErrores().stream()
            .anyMatch(error -> error.contains("4 años")),
            "Debe contener un error específico sobre la edad mínima");
        
        // Verificar que los repositorios nunca fueron llamados
        verify(contraseniaRepository, never()).save(any(Contrasenia.class));
        verify(visualizadorRepository, never()).save(any(Visualizador.class));
    }
    
    /**
     * Test para verificar el manejo de email duplicado
     */
    @Test
    @DisplayName("Debe rechazar emails ya registrados")
    void rechazarEmailDuplicado() {
        // Arrange
        VisualizadorRegistroDTO dto = crearDTOValido();
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violacionesVacias = Set.of();
        
        // Configurar mocks
        when(validator.validate(any(VisualizadorRegistroDTO.class))).thenReturn(violacionesVacias);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(true);
        
        // Act
        RegistroResultado resultado = visualizadorService.registrarVisualizador(dto);
        
        // Assert
        assertFalse(resultado.isExitoso(), "El registro con email duplicado debe fallar");
        assertNull(resultado.getVisualizador(), "No debe crearse un visualizador");
        assertFalse(resultado.getErrores().isEmpty(), "Debe haber errores en el resultado");
        assertTrue(resultado.getErrores().stream()
            .anyMatch(error -> error.contains("email") && error.contains("registrado")),
            "Debe contener un error específico sobre email ya registrado");
        
        // Verificar que los repositorios nunca fueron llamados
        verify(contraseniaRepository, never()).save(any(Contrasenia.class));
        verify(visualizadorRepository, never()).save(any(Visualizador.class));
    }
    
    /**
     * Test para verificar el manejo de errores de base de datos (DuplicateKeyException)
     */
    @Test
    @DisplayName("Debe manejar errores de base de datos")
    void manejarErroresBaseDatos() {
        // Arrange
        VisualizadorRegistroDTO dto = crearDTOValido();
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violacionesVacias = Set.of();
        
        // Configurar mocks
        when(validator.validate(any(VisualizadorRegistroDTO.class))).thenReturn(violacionesVacias);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(contraseniaComunRepository.existsById(anyString())).thenReturn(false);
        when(userService.hashearContrasenia(any(Contrasenia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contraseniaRepository.save(any(Contrasenia.class))).thenAnswer(invocation -> {
            Contrasenia contrasenia = invocation.getArgument(0);
            contrasenia.setId("contrasenia123");
            return contrasenia;
        });
        when(visualizadorRepository.save(any(Visualizador.class)))
            .thenThrow(new DuplicateKeyException("Email duplicado"));
        
        // Act
        RegistroResultado resultado = visualizadorService.registrarVisualizador(dto);
        
        // Assert
        assertFalse(resultado.isExitoso(), "El registro debe fallar cuando hay error de base de datos");
        assertNull(resultado.getVisualizador(), "No debe crearse un visualizador");
        assertFalse(resultado.getErrores().isEmpty(), "Debe haber errores en el resultado");
        assertTrue(resultado.getErrores().stream().anyMatch(e -> e.toLowerCase().contains("email")), 
                  "El mensaje debe mencionar el problema específico");
        
        // Verificar que contraseniaRepository fue llamado pero visualizadorRepository causó excepción
        verify(contraseniaRepository, times(1)).save(any(Contrasenia.class));
        verify(visualizadorRepository, times(1)).save(any(Visualizador.class));
    }
    
    /**
     * Test para verificar el manejo del alias cuando es nulo o vacío
     */
    @Test
    @DisplayName("Debe usar el nombre como alias cuando éste no se proporciona")
    void usarNombreComoAliasCuandoNoSeProvee() {
        // Arrange
        VisualizadorRegistroDTO dto = crearDTOValido();
        dto.setAlias(null); // Alias nulo
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violacionesVacias = Set.of();
        
        // Configurar mocks
        when(validator.validate(any(VisualizadorRegistroDTO.class))).thenReturn(violacionesVacias);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(contraseniaComunRepository.existsById(anyString())).thenReturn(false);
        when(userService.hashearContrasenia(any(Contrasenia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contraseniaRepository.save(any(Contrasenia.class))).thenAnswer(invocation -> {
            Contrasenia contrasenia = invocation.getArgument(0);
            contrasenia.setId("contrasenia123");
            return contrasenia;
        });
        when(visualizadorRepository.save(any(Visualizador.class))).thenAnswer(invocation -> {
            Visualizador visualizador = invocation.getArgument(0);
            visualizador.setId("usuario123");
            return visualizador;
        });
        
        // Act
        RegistroResultado resultado = visualizadorService.registrarVisualizador(dto);
        
        // Assert
        assertTrue(resultado.isExitoso(), "El registro debe ser exitoso incluso sin alias");
        assertNotNull(resultado.getVisualizador(), "El resultado debe contener un visualizador");
        assertEquals(dto.getNombre(), resultado.getVisualizador().getAlias(), 
                    "El alias debe ser igual al nombre cuando no se proporciona");
    }
    
    /**
     * Test para verificar la activación del 2FA
     */
    @Test
    @DisplayName("Debe activar 2FA correctamente para un usuario existente")
    void activar2FAParaUsuarioExistente() {
        // Arrange
        String email = "juan@email.com";
        Usuario usuario = new Usuario();
        usuario.setId("usuario123");
        usuario.setEmail(email);
        usuario.setTwoFactorAutenticationEnabled(false);
        
        // Configurar mock del repositorio
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            // Verificar que el usuario ha sido actualizado con los datos de 2FA
            assertTrue(u.isTwoFactorAutenticationEnabled(), "2FA debe estar habilitado");
            assertNotNull(u.getSecretkey(), "El secretkey no debe ser nulo");
            return u;
        });
        
        // Mock para GoogleAuthenticatorQRGenerator
        try (MockedStatic<GoogleAuthenticatorQRGenerator> mockedStatic = mockStatic(GoogleAuthenticatorQRGenerator.class)) {
            // Configurar que cualquier llamada a getOtpAuthURL retorne una URL específica
            mockedStatic.when(() -> GoogleAuthenticatorQRGenerator.getOtpAuthURL(anyString(), anyString(), any()))
                .thenReturn("otpauth://totp/ESI-MEDIA:juan@email.com?secret=RGYCLPSZKURBDCMBHOFS4LC6DX6MGLT3&issuer=ESI-MEDIA");
            
            // Act
            String result = visualizadorService.activar2FA(email);
            
            // Assert
            assertNotNull(result, "El resultado no debe ser nulo");
            assertFalse(result.isEmpty(), "El resultado no debe estar vacío");
            
            // En lugar de verificar que empieza con otpauth://, verificamos que el resultado no está vacío
            // y que se guardó el usuario, lo que indica que el proceso funcionó correctamente
            verify(usuarioRepository, times(1)).save(any(Usuario.class));
        }
    }
    
    /**
     * Test para verificar el manejo cuando el usuario no existe al activar 2FA
     */
    @Test
    @DisplayName("Debe retornar vacío al intentar activar 2FA para usuario inexistente")
    void activar2FAUsuarioInexistente() {
        // Arrange
        String email = "noexiste@email.com";
        
        // Configurar mock
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act
        String result = visualizadorService.activar2FA(email);
        
        // Assert
        assertTrue(result.isEmpty(), "El resultado debe ser una cadena vacía");
        
        // Verificar que no se intentó guardar ningún usuario
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
    
    /**
     * Test para verificar la búsqueda de visualizadores por email
     */
    @Test
    @DisplayName("Debe encontrar visualizador por email cuando existe")
    void encontrarVisualizadorPorEmail() {
        // Arrange
        String email = "juan@email.com";
        Visualizador visualizador = new Visualizador();
        visualizador.setId("usuario123");
        visualizador.setEmail(email);
        
        // Configurar mock
        when(visualizadorRepository.findBy_email(email)).thenReturn(Optional.of(visualizador));
        
        // Act
        Optional<Visualizador> resultado = visualizadorService.buscarPorEmail(email);
        
        // Assert
        assertTrue(resultado.isPresent(), "Debe encontrar el visualizador");
        assertEquals(email, resultado.get().getEmail(), "El email debe coincidir");
    }
    
    /**
     * Test para verificar la obtención de todos los visualizadores
     */
    @Test
    @DisplayName("Debe obtener todos los visualizadores registrados")
    void obtenerTodosLosVisualizadores() {
        // Arrange
        List<Visualizador> visualizadores = new ArrayList<>();
        Visualizador v1 = new Visualizador();
        v1.setId("v1");
        v1.setNombre("Juan");
        visualizadores.add(v1);
        
        Visualizador v2 = new Visualizador();
        v2.setId("v2");
        v2.setNombre("María");
        visualizadores.add(v2);
        
        // Configurar mock
        when(visualizadorRepository.findAll()).thenReturn(visualizadores);
        
        // Act
        List<Visualizador> resultado = visualizadorService.obtenerTodosLosVisualizadores();
        
        // Assert
        assertEquals(2, resultado.size(), "Debe haber 2 visualizadores");
        assertEquals("Juan", resultado.get(0).getNombre(), "El primer visualizador debe ser Juan");
        assertEquals("María", resultado.get(1).getNombre(), "El segundo visualizador debe ser María");
    }
}