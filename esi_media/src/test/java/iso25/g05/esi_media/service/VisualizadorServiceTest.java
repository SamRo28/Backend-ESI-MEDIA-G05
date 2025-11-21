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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.dto.VisualizadorRegistroDTO;
import iso25.g05.esi_media.exception.PeticionInvalidaException;
import iso25.g05.esi_media.exception.RecursoNoEncontradoException;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.Contenido;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Video;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.ContenidoRepository;
import iso25.g05.esi_media.repository.ContraseniaRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.repository.VisualizadorRepository;
import iso25.g05.esi_media.service.LogService;
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

    @Mock
    private ContenidoRepository contenidoRepository;

    @Mock
    private LogService logService;
    
    @InjectMocks
    private VisualizadorService visualizadorService;
    
    @BeforeEach
    void setUp() {
        // Inicializar los mocks
        MockitoAnnotations.openMocks(this);
        
        // Inyectar manualmente los campos @Autowired que no están en el constructor
        ReflectionTestUtils.setField(visualizadorService, "userService", userService);
        ReflectionTestUtils.setField(visualizadorService, "contraseniaComunRepository", contraseniaComunRepository);
        ReflectionTestUtils.setField(visualizadorService, "contenidoRepository", contenidoRepository);
        ReflectionTestUtils.setField(visualizadorService, "logService", logService);
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
        
        // Capturar el usuario guardado para verificar después
        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        when(usuarioRepository.save(usuarioCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        
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
            
            // Verificar que el resultado es la URL de OTP Auth esperada
            assertTrue(result.contains("otpauth://"), "El resultado debe contener la URL OTP Auth");
            
            // Verificar que se guardó el usuario
            verify(usuarioRepository, times(1)).save(any(Usuario.class));
            
            // Verificar que el usuario guardado tiene la clave secreta configurada
            Usuario usuarioGuardado = usuarioCaptor.getValue();
            assertNotNull(usuarioGuardado, "El usuario guardado no debe ser nulo");
            assertNotNull(usuarioGuardado.getSecretkey(), "El secretkey no debe ser nulo");
            assertFalse(usuarioGuardado.getSecretkey().isEmpty(), "El secretkey no debe estar vacío");
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

    @Test
    @DisplayName("Obtiene favoritos visibles y filtra los inactivos")
    void obtenerFavoritos_filtraContenidosInvisibles() {
        Video normal = crearVideo("video-1", true);
        Video oculto = crearVideo("video-2", false);
        Visualizador visualizador = crearVisualizadorAutenticado();
        visualizador.setContenidofav(new ArrayList<>(List.of(normal, oculto)));

        prepararToken("token-header", visualizador);

        List<ContenidoResumenDTO> favoritos = visualizadorService.obtenerFavoritos("token-header");

        assertEquals(1, favoritos.size(), "Solo debe incluir el contenido visible");
        assertEquals("video-1", favoritos.get(0).getId());
    }

    @Test
    @DisplayName("Agrega un favorito nuevo y no duplica guardados")
    void agregarFavorito_registraAccionYSoloUnaVez() {
        Visualizador visualizador = crearVisualizadorAutenticado();
        prepararToken("token-header", visualizador);
        Video contenido = crearVideo("c1", true);
        when(contenidoRepository.findByIdAndEstadoTrue("c1")).thenReturn(Optional.of(contenido));

        visualizadorService.agregarFavorito("token-header", "c1");
        visualizadorService.agregarFavorito("token-header", "c1");

        assertEquals(1, visualizador.getContenidofav().size(), "No debe duplicar el contenido favorito");
        verify(usuarioRepository, times(1)).save(visualizador);
        verify(logService, times(1)).registrarAccion("Favorito añadido: " + contenido.gettitulo(), visualizador.getEmail());
    }

    @Test
    @DisplayName("Elimina un favorito existente y registra la acción")
    void eliminarFavorito_eliminaContenidoExistente() {
        Visualizador visualizador = crearVisualizadorAutenticado();
        Video contenido = crearVideo("c2", true);
        visualizador.setContenidofav(new ArrayList<>(List.of(contenido)));
        prepararToken("token-header", visualizador);

        visualizadorService.eliminarFavorito("token-header", "c2");

        assertTrue(visualizador.getContenidofav().isEmpty(), "El favorito debe ser removido");
        verify(usuarioRepository, times(1)).save(visualizador);
        verify(logService, times(1)).registrarAccion("Favorito eliminado: c2", visualizador.getEmail());
    }

    @Test
    @DisplayName("Agregar favorito lanza 404 si no existe el contenido")
    void agregarFavorito_conContenidoInexistente_lanza404() {
        Visualizador visualizador = crearVisualizadorAutenticado();
        prepararToken("token-header", visualizador);
        when(contenidoRepository.findByIdAndEstadoTrue("missing")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> visualizadorService.agregarFavorito("token-header", "missing"));
    }

    @Test
    @DisplayName("Agregar favorito lanza 400 si falta el ID")
    void agregarFavorito_idVacio_lanza400() {
        assertThrows(PeticionInvalidaException.class, () -> visualizadorService.agregarFavorito("token-header", ""));
    }

    private void prepararToken(String header, Visualizador visualizador) {
        String token = "session-token";
        when(userService.extraerToken(header)).thenReturn(token);
        when(usuarioRepository.findBySesionToken(token)).thenReturn(Optional.of(visualizador));
    }

    private Visualizador crearVisualizadorAutenticado() {
        Visualizador visualizador = new Visualizador();
        visualizador.setId("visu-1");
        visualizador.setEmail("user@esi.es");
        visualizador.setContenidofav(new ArrayList<>());
        return visualizador;
    }

    private Video crearVideo(String id, boolean estado) {
        Video video = new Video();
        video.setId(id);
        video.setestado(estado);
        video.settitulo("Video " + id);
        return video;
    }
}
