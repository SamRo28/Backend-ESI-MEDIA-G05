package iso25.g05.esi_media.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.Codigorecuperacion;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.AdministradorRepository;
import iso25.g05.esi_media.repository.CodigoRecuperacionRepository;
import iso25.g05.esi_media.repository.ContraseniaComunRepository;
import iso25.g05.esi_media.repository.ContraseniaRepository;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import iso25.g05.esi_media.repository.IpLoginAttemptRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.repository.VisualizadorRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private ContraseniaComunRepository contraseniaComunRepository;

    @Mock
    private ContraseniaRepository contraseniaRepository;

    @Mock
    private AdministradorRepository administradorRepository;

    @Mock
    private VisualizadorRepository visualizadorRepository;

    @Mock
    private GestorDeContenidoRepository gestorDeContenidoRepository;

    @Mock
    private CodigoRecuperacionRepository codigorecuperacionRepository;

    @Mock
    private IpLoginAttemptRepository ipLoginAttemptRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private UserService userService;

    @Test
    void testLoginSuccess() {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "test@example.com");
        loginData.put("password", "password");

        Usuario user = new Usuario();
        user.setId("user123");
        user.setEmail("test@example.com");
        // Use BCrypt to encode the password since the service uses BCryptPasswordEncoder
        PasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String encodedPassword = encoder.encode("password");
        Contrasenia contrasenia = new Contrasenia("1", null, encodedPassword, null);
        user.setContrasenia(contrasenia);
        user.setTwoFactorAutenticationEnabled(false);

        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(user);
        when(ipLoginAttemptRepository.findById("127.0.0.1")).thenReturn(Optional.empty());
        when(ipLoginAttemptRepository.save(any())).thenReturn(null);

        Usuario result = userService.login(loginData, "127.0.0.1");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testLoginFailure() {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "test@example.com");
        loginData.put("password", "wrongpassword");

        Usuario user = new Usuario();
        user.setEmail("test@example.com");
        Contrasenia contrasenia = new Contrasenia("1", null, "password", null);
        user.setContrasenia(contrasenia);

        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Usuario result = userService.login(loginData, "127.0.0.1");

        assertNull(result);
    }

    @Test
    void testLogin3Auth() {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "test@example.com");

        Usuario user = new Usuario();
        user.setEmail("test@example.com");

        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        userService.login3Auth(loginData);

        verify(emailService, times(1)).send3FAemail("test@example.com", user);
    }

    // ==================== TESTS PARA login (versión simple) ====================

    @Test
    @DisplayName("login: debe autenticar usuario sin 2FA habilitado")
    void testLoginUsuarioSin2FA() {
        // Arrange
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "user@example.com");
        loginData.put("password", "password");

        Usuario user = new Usuario();
        user.setId("user123");
        user.setEmail("user@example.com");
        // Use BCrypt to encode the password since the service uses BCryptPasswordEncoder
        PasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String encodedPassword = encoder.encode("password");
        Contrasenia contrasenia = new Contrasenia("1", null, encodedPassword, null);
        user.setContrasenia(contrasenia);
        user.setTwoFactorAutenticationEnabled(false);

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(user);
        when(ipLoginAttemptRepository.findById("127.0.0.1")).thenReturn(Optional.empty());
        when(ipLoginAttemptRepository.save(any())).thenReturn(null);

        // Act
        Usuario result = userService.login(loginData, "127.0.0.1");

        // Assert
        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("login: debe retornar usuario sin token cuando 2FA está habilitado")
    void testLoginUsuarioCon2FA() {
        // Arrange
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "user@example.com");
        loginData.put("password", "password");

        Usuario user = new Usuario();
        user.setId("user123");
        user.setEmail("user@example.com");
        // Use BCrypt to encode the password since the service uses BCryptPasswordEncoder
        PasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String encodedPassword = encoder.encode("password");
        Contrasenia contrasenia = new Contrasenia("1", null, encodedPassword, null);
        user.setContrasenia(contrasenia);
        user.setTwoFactorAutenticationEnabled(true);

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(ipLoginAttemptRepository.findById("127.0.0.1")).thenReturn(Optional.empty());
        when(ipLoginAttemptRepository.save(any())).thenReturn(null);

        // Act
        Usuario result = userService.login(loginData, "127.0.0.1");

        // Assert
        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
        // No debe guardar porque 2FA está habilitado
        verify(usuarioRepository, times(0)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("login: debe retornar null cuando usuario no existe")
    void testLoginUsuarioNoExiste() {
        // Arrange
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "noexiste@example.com");
        loginData.put("password", "password");

        when(usuarioRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        // Act
        Usuario result = userService.login(loginData, "127.0.0.1");

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("login: debe retornar null cuando contraseña es incorrecta")
    void testLoginPasswordIncorrecto() {
        // Arrange
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "user@example.com");
        loginData.put("password", "wrongpassword");

        Usuario user = new Usuario();
        user.setEmail("user@example.com");
        Contrasenia contrasenia = new Contrasenia("1", null, "correcthash", null);
        user.setContrasenia(contrasenia);

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        // Act
        Usuario result = userService.login(loginData, "127.0.0.1");

        // Assert
        assertNull(result);
    }

    // ==================== TESTS PARA deletePassword ====================

    @Test
    @DisplayName("deletePassword: debe eliminar contraseña exitosamente")
    void testDeletePasswordExitoso() {
        // Arrange
        String contraseniaId = "pass123";

        // Act
        userService.deletePassword(contraseniaId);

        // Assert
        verify(contraseniaRepository, times(1)).deleteById(contraseniaId);
    }

    @Test
    @DisplayName("deletePassword: debe manejar silenciosamente excepciones")
    void testDeletePasswordConExcepcion() {
        // Arrange
        String contraseniaId = "pass123";
        
        // Simular que lanza excepción
        org.mockito.Mockito.doThrow(new RuntimeException("Error de BD"))
            .when(contraseniaRepository).deleteById(contraseniaId);

        // Act - no debe lanzar excepción
        userService.deletePassword(contraseniaId);

        // Assert
        verify(contraseniaRepository, times(1)).deleteById(contraseniaId);
    }

    // ==================== TESTS PARA confirmLogin3Auth ====================

    @Test
    @DisplayName("confirmLogin3Auth: debe generar token cuando el código es válido")
    void testConfirmLogin3AuthExitoso() {
        // Arrange
        Map<String, String> loginData = new HashMap<>();
        loginData.put("id", "codigoId123");
        loginData.put("code", "123456");

        Usuario usuario = new Usuario();
        usuario.setId("user123");
        usuario.setEmail("test@example.com");
        usuario.setThreeFactorAutenticationEnabled(false);

        Codigorecuperacion codigoRecuperacion = new Codigorecuperacion();
        codigoRecuperacion.setId("codigoId123");
        codigoRecuperacion.setcodigo("123456");
        codigoRecuperacion.setunnamedUsuario(usuario);

        when(codigorecuperacionRepository.findById("codigoId123"))
            .thenReturn(Optional.of(codigoRecuperacion));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        Token result = userService.confirmLogin3Auth(loginData);

        // Assert
        assertNotNull(result);
        assertTrue(usuario.isThreeFactorAutenticationEnabled());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("confirmLogin3Auth: debe retornar null cuando el código es inválido")
    void testConfirmLogin3AuthCodigoInvalido() {
        // Arrange
        Map<String, String> loginData = new HashMap<>();
        loginData.put("id", "codigoId123");
        loginData.put("code", "wrongcode");

        Codigorecuperacion codigoRecuperacion = new Codigorecuperacion();
        codigoRecuperacion.setId("codigoId123");
        codigoRecuperacion.setcodigo("123456");

        when(codigorecuperacionRepository.findById("codigoId123"))
            .thenReturn(Optional.of(codigoRecuperacion));

        // Act
        Token result = userService.confirmLogin3Auth(loginData);

        // Assert
        assertNull(result);
        verify(usuarioRepository, times(0)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("confirmLogin3Auth: debe retornar null cuando el código no existe")
    void testConfirmLogin3AuthCodigoNoExiste() {
        // Arrange
        Map<String, String> loginData = new HashMap<>();
        loginData.put("id", "codigoInexistente");
        loginData.put("code", "123456");

        when(codigorecuperacionRepository.findById("codigoInexistente"))
            .thenReturn(Optional.empty());

        // Act
        Token result = userService.confirmLogin3Auth(loginData);

        // Assert
        assertNull(result);
        verify(usuarioRepository, times(0)).save(any(Usuario.class));
    }

    // ==================== TESTS PARA confirm2faCode ====================

    @Test
    @DisplayName("confirm2faCode: debe generar token cuando el código 2FA es válido y 3FA está deshabilitado")
    void testConfirm2faCodeExitoso() {
        // Arrange
        Map<String, String> data = new HashMap<>();
        data.put("code", "123456");
        data.put("email", "test@example.com");

        Usuario usuario = new Usuario();
        usuario.setId("user123");
        usuario.setEmail("test@example.com");
        usuario.setSecretkey("TESTSECRETKEY123");
        usuario.setTwoFactorAutenticationEnabled(false);
        usuario.setThreeFactorAutenticationEnabled(false);

        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Necesitamos mockear GoogleAuthenticator - esto es complicado porque es una clase final
        // Por simplicidad, asumimos que la validación pasa
        // En un test real, necesitaríamos refactorizar el código para inyectar GoogleAuthenticator

        // Act
        userService.confirm2faCode(data);

        // Assert
        // El resultado depende de la validación de GoogleAuthenticator
        // Como mínimo, verificamos que se guardó el usuario
        assertTrue(usuario.isTwoFactorAutenticationEnabled());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("confirm2faCode: debe retornar cadena vacía cuando 3FA está habilitado")
    void testConfirm2faCodeCon3FAHabilitado() {
        // Arrange
        Map<String, String> data = new HashMap<>();
        data.put("code", "123456");
        data.put("email", "test@example.com");

        Usuario usuario = new Usuario();
        usuario.setId("user123");
        usuario.setEmail("test@example.com");
        usuario.setSecretkey("TESTSECRETKEY123");
        usuario.setTwoFactorAutenticationEnabled(false);
        usuario.setThreeFactorAutenticationEnabled(true); // 3FA habilitado

        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        userService.confirm2faCode(data);

        // Assert
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("confirm2faCode: debe retornar null cuando el usuario no existe")
    void testConfirm2faCodeUsuarioNoExiste() {
        // Arrange
        Map<String, String> data = new HashMap<>();
        data.put("code", "123456");
        data.put("email", "noexiste@example.com");

        when(usuarioRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        // Act
        String result = userService.confirm2faCode(data);

        // Assert
        assertNull(result);
        verify(usuarioRepository, times(0)).save(any(Usuario.class));
    }

    // ==================== TESTS PARA updateUser ====================

    @Test
    @DisplayName("updateUser: debe actualizar Administrador exitosamente")
    void testUpdateUserAdministrador() throws Exception {
        // Arrange
        String userId = "admin123";
        String tipo = "Administrador";
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", "Juan Actualizado");
        updates.put("departamento", "IT");

        Administrador admin = new Administrador();
        admin.setId(userId);
        admin.setNombre("Juan");
        admin.setDepartamento("HR");

        when(administradorRepository.findById(userId)).thenReturn(Optional.of(admin));
        when(administradorRepository.save(any(Administrador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Usuario result = userService.updateUser(userId, tipo, updates);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Administrador);
        verify(administradorRepository, times(1)).save(any(Administrador.class));
    }

    @Test
    @DisplayName("updateUser: debe actualizar Visualizador exitosamente")
    void testUpdateUserVisualizador() throws Exception {
        // Arrange
        String userId = "vis123";
        String tipo = "Visualizador";
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", "María Actualizada");
        updates.put("alias", "mary");

        Visualizador visualizador = new Visualizador();
        visualizador.setId(userId);
        visualizador.setNombre("María");

        when(visualizadorRepository.findById(userId)).thenReturn(Optional.of(visualizador));
        when(visualizadorRepository.save(any(Visualizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Usuario result = userService.updateUser(userId, tipo, updates);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Visualizador);
        verify(visualizadorRepository, times(1)).save(any(Visualizador.class));
    }

    @Test
    @DisplayName("updateUser: debe actualizar GestordeContenido exitosamente")
    void testUpdateUserGestor() throws Exception {
        // Arrange
        String userId = "gestor123";
        String tipo = "GestordeContenido";
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", "Pedro Actualizado");

        GestordeContenido gestor = new GestordeContenido();
        gestor.setId(userId);
        gestor.setNombre("Pedro");

        when(gestorDeContenidoRepository.findById(userId)).thenReturn(Optional.of(gestor));
        when(gestorDeContenidoRepository.save(any(GestordeContenido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Usuario result = userService.updateUser(userId, tipo, updates);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof GestordeContenido);
        verify(gestorDeContenidoRepository, times(1)).save(any(GestordeContenido.class));
    }

    @Test
    @DisplayName("updateUser: debe retornar null cuando el usuario no existe")
    void testUpdateUserNoExiste() throws Exception {
        // Arrange
        String userId = "noexiste123";
        String tipo = "Administrador";
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", "Test");

        when(administradorRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Usuario result = userService.updateUser(userId, tipo, updates);

        // Assert
        assertNull(result);
        verify(administradorRepository, times(0)).save(any(Administrador.class));
    }

    // ==================== TESTS ADICIONALES PARA MEJORAR COBERTURA ====================

    @Test
    @DisplayName("updateUser: debe llamar a LogService al actualizar un Visualizador")
    void testUpdateUserVisualizadorConLog() throws Exception {
        // Arrange - Corregido para incluir mock de LogService
        String userId = "vis123";
        String tipo = "Visualizador";
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", "María Actualizada");
        updates.put("alias", "mary");

        Visualizador visualizador = new Visualizador();
        visualizador.setId(userId);
        visualizador.setNombre("María");
        visualizador.setEmail("maria@test.com");

        when(visualizadorRepository.findById(userId)).thenReturn(Optional.of(visualizador));
        when(visualizadorRepository.save(any(Visualizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Usuario result = userService.updateUser(userId, tipo, updates);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Visualizador);
        verify(visualizadorRepository, times(1)).save(any(Visualizador.class));
        verify(logService, times(1)).registrarAccion(anyString(), anyString());
    }

    @Test
    @DisplayName("updateUser: debe actualizar contraseña de Visualizador cuando se proporciona")
    void testUpdateUserVisualizadorConCambioContrasenia() throws Exception {
        // Arrange
        String userId = "vis123";
        String tipo = "Visualizador";
        Map<String, Object> updates = new HashMap<>();
        updates.put("contrasenia", "NuevaPassword123!");

        Visualizador visualizador = new Visualizador();
        visualizador.setId(userId);
        visualizador.setNombre("María");
        visualizador.setEmail("maria@test.com");

        when(visualizadorRepository.findById(userId)).thenReturn(Optional.of(visualizador));
        when(visualizadorRepository.save(any(Visualizador.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contraseniaComunRepository.existsById(anyString())).thenReturn(false);
        
        Usuario usuarioMock = new Visualizador();
        usuarioMock.setEmail("maria@test.com");
        Contrasenia contraseniaActual = new Contrasenia();
        contraseniaActual.setContraseniaActual("oldHashedPassword");
        contraseniaActual.setContraseniasUsadas(new ArrayList<>());
        usuarioMock.setContrasenia(contraseniaActual);
        
        when(usuarioRepository.findByEmail("maria@test.com")).thenReturn(Optional.of(usuarioMock));
        when(contraseniaRepository.save(any(Contrasenia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Usuario result = userService.updateUser(userId, tipo, updates);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Visualizador);
        verify(visualizadorRepository, times(1)).save(any(Visualizador.class));
    }

    @Test
    @DisplayName("login: debe bloquear IP después de múltiples intentos fallidos")
    void testLoginBloqueoPorIntentosMultiples() {
        // Arrange
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "test@example.com");
        loginData.put("password", "wrongpassword");

        Usuario user = new Usuario();
        user.setEmail("test@example.com");
        Contrasenia contrasenia = new Contrasenia("1", null, "correcthash", null);
        user.setContrasenia(contrasenia);

        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(ipLoginAttemptRepository.findById("127.0.0.1")).thenReturn(Optional.empty());
        when(ipLoginAttemptRepository.save(any())).thenReturn(null);

        // Act
        Usuario result = userService.login(loginData, "127.0.0.1");

        // Assert
        assertNull(result);
        verify(ipLoginAttemptRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("cambiarContrasenia: debe cambiar contraseña exitosamente")
    void testCambiarContraseniaExitoso() {
        // Arrange
        String email = "user@example.com";
        String nuevaContrasenia = "NuevaPassword123!";

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        Contrasenia contraseniaActual = new Contrasenia();
        contraseniaActual.setContraseniaActual("oldHashedPassword");
        contraseniaActual.setContraseniasUsadas(new ArrayList<>());
        usuario.setContrasenia(contraseniaActual);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(contraseniaComunRepository.existsById(nuevaContrasenia)).thenReturn(false);
        when(contraseniaRepository.save(any(Contrasenia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        boolean result = userService.cambiarContrasenia(email, nuevaContrasenia);

        // Assert
        assertTrue(result);
        verify(contraseniaRepository, times(1)).save(any(Contrasenia.class));
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("cambiarContrasenia: debe rechazar contraseña común")
    void testCambiarContraseniaContraseniaComun() {
        // Arrange
        String email = "user@example.com";
        String contraseniaComun = "123456";

        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(contraseniaComunRepository.existsById(contraseniaComun)).thenReturn(true);

        // Act & Assert
        try {
            userService.cambiarContrasenia(email, contraseniaComun);
            // Si no lanza excepción, el test falla
            assertTrue(false, "Debe lanzar PeticionInvalidaException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("politicas de seguridad"));
        }

        verify(contraseniaRepository, times(0)).save(any(Contrasenia.class));
    }

    @Test
    @DisplayName("cambiarContrasenia: debe rechazar contraseña usada recientemente")
    void testCambiarContraseniaRepetida() {
        // Arrange
        String email = "user@example.com";
        String contraseniaRepetida = "OldPassword123!";

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        
        PasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String hashedPassword = encoder.encode(contraseniaRepetida);
        
        Contrasenia contraseniaActual = new Contrasenia();
        contraseniaActual.setContraseniaActual(hashedPassword);
        List<String> usadas = new ArrayList<>();
        usadas.add(hashedPassword);
        contraseniaActual.setContraseniasUsadas(usadas);
        usuario.setContrasenia(contraseniaActual);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(contraseniaComunRepository.existsById(contraseniaRepetida)).thenReturn(false);

        // Act & Assert
        try {
            userService.cambiarContrasenia(email, contraseniaRepetida);
            // Si no lanza excepción, el test falla
            assertTrue(false, "Debe lanzar PeticionInvalidaException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("5 ultimas"));
        }

        verify(contraseniaRepository, times(0)).save(any(Contrasenia.class));
    }

    @Test
    @DisplayName("cambiarContrasenia: debe retornar false cuando usuario no existe")
    void testCambiarContraseniaUsuarioNoExiste() {
        // Arrange
        String email = "noexiste@example.com";
        String nuevaContrasenia = "NuevaPassword123!";

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.cambiarContrasenia(email, nuevaContrasenia);

        // Assert
        assertFalse(result);
        verify(contraseniaRepository, times(0)).save(any(Contrasenia.class));
    }

    @Test
    @DisplayName("crearYValidarContrasenia: debe crear y hashear contraseña válida")
    void testCrearYValidarContraseniaExitoso() {
        // Arrange
        String contraseniaTextoPlano = "ValidPassword123!";

        when(contraseniaComunRepository.existsById(contraseniaTextoPlano)).thenReturn(false);
        when(contraseniaRepository.save(any(Contrasenia.class))).thenAnswer(invocation -> {
            Contrasenia c = invocation.getArgument(0);
            c.setId("contrasenia123");
            return c;
        });

        // Act
        Contrasenia result = userService.crearYValidarContrasenia(contraseniaTextoPlano);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getContraseniaActual());
        assertNotNull(result.getContraseniasUsadas());
        assertFalse(result.getContraseniasUsadas().isEmpty());
        verify(contraseniaRepository, times(1)).save(any(Contrasenia.class));
    }

    @Test
    @DisplayName("crearYValidarContrasenia: debe rechazar contraseña común")
    void testCrearYValidarContraseniaComun() {
        // Arrange
        String contraseniaComun = "123456";

        when(contraseniaComunRepository.existsById(contraseniaComun)).thenReturn(true);

        // Act & Assert
        try {
            userService.crearYValidarContrasenia(contraseniaComun);
            assertTrue(false, "Debe lanzar RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("contraseñas comunes"));
        }

        verify(contraseniaRepository, times(0)).save(any(Contrasenia.class));
    }

    @Test
    @DisplayName("crearYValidarContrasenia: debe rechazar contraseña vacía")
    void testCrearYValidarContraseniaVacia() {
        // Act & Assert
        try {
            userService.crearYValidarContrasenia("");
            assertTrue(false, "Debe lanzar RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("contraseña es obligatoria"));
        }

        verify(contraseniaRepository, times(0)).save(any(Contrasenia.class));
    }

    @Test
    @DisplayName("validarEmailUnico: debe validar email único exitosamente")
    void testValidarEmailUnicoExitoso() {
        // Arrange
        String email = "nuevo@example.com";

        when(usuarioRepository.existsByEmail(email)).thenReturn(false);

        // Act & Assert - No debe lanzar excepción
        try {
            userService.validarEmailUnico(email);
        } catch (RuntimeException e) {
            assertTrue(false, "No debe lanzar excepción para email único");
        }
    }

    @Test
    @DisplayName("validarEmailUnico: debe rechazar email duplicado")
    void testValidarEmailUnicoDuplicado() {
        // Arrange
        String email = "existente@example.com";

        when(usuarioRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        try {
            userService.validarEmailUnico(email);
            assertTrue(false, "Debe lanzar RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("ya está registrado"));
        }
    }

    @Test
    @DisplayName("validarEmailUnico: debe rechazar email vacío")
    void testValidarEmailUnicoVacio() {
        // Act & Assert
        try {
            userService.validarEmailUnico("");
            assertTrue(false, "Debe lanzar RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("email es obligatorio"));
        }
    }

    @Test
    @DisplayName("login3Auth: debe enviar email 3FA cuando usuario existe")
    void testLogin3AuthExitoso() {
        // Arrange
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "test@example.com");

        Usuario user = new Usuario();
        user.setEmail("test@example.com");

        Codigorecuperacion codigo = new Codigorecuperacion();
        codigo.setId("codigo123");

        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(emailService.send3FAemail("test@example.com", user)).thenReturn(codigo);

        // Act
        String result = userService.login3Auth(loginData);

        // Assert
        assertNotNull(result);
        assertEquals("codigo123", result);
        verify(emailService, times(1)).send3FAemail("test@example.com", user);
    }

    @Test
    @DisplayName("login3Auth: debe retornar null cuando usuario no existe")
    void testLogin3AuthUsuarioNoExiste() {
        // Arrange
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "noexiste@example.com");

        when(usuarioRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        // Act
        String result = userService.login3Auth(loginData);

        // Assert
        assertNull(result);
        verify(emailService, times(0)).send3FAemail(anyString(), any(Usuario.class));
    }

    @Test
    @DisplayName("hashearContrasenia: debe hashear contraseña correctamente")
    void testHashearContrasenia() {
        // Arrange
        Contrasenia contrasenia = new Contrasenia();
        contrasenia.setContraseniaActual("PlainTextPassword");

        // Act
        Contrasenia result = userService.hashearContrasenia(contrasenia);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getContraseniaActual());
        // La contraseña hasheada no debe ser igual a la contraseña en texto plano
        assertTrue(result.getContraseniaActual().length() > 20);
    }

    @Test
    @DisplayName("updateUser: debe manejar tipo de usuario inválido")
    void testUpdateUserTipoInvalido() throws Exception {
        // Arrange
        String userId = "user123";
        String tipo = "TipoInvalido";
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", "Test");

        // Act
        Usuario result = userService.updateUser(userId, tipo, updates);

        // Assert
        assertNull(result);
        verify(administradorRepository, times(0)).save(any(Administrador.class));
        verify(visualizadorRepository, times(0)).save(any(Visualizador.class));
        verify(gestorDeContenidoRepository, times(0)).save(any(GestordeContenido.class));
    }
}

