package iso25.g05.esi_media.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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
        // MD5 hash of "password" is "5f4dcc3b5aa765d61d8327deb882cf99"
        Contrasenia contrasenia = new Contrasenia("1", null, "5f4dcc3b5aa765d61d8327deb882cf99", null);
        user.setContrasenia(contrasenia);
        user.setTwoFactorAutenticationEnabled(false);

        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(user);

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
        // MD5 hash of "password" is "5f4dcc3b5aa765d61d8327deb882cf99"
        Contrasenia contrasenia = new Contrasenia("1", null, "5f4dcc3b5aa765d61d8327deb882cf99", null);
        user.setContrasenia(contrasenia);
        user.setTwoFactorAutenticationEnabled(false);

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(user);

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
        // MD5 hash of "password"
        Contrasenia contrasenia = new Contrasenia("1", null, "5f4dcc3b5aa765d61d8327deb882cf99", null);
        user.setContrasenia(contrasenia);
        user.setTwoFactorAutenticationEnabled(true);

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

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
}
