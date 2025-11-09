package iso25.g05.esi_media.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.ContraseniaComunRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private ContraseniaComunRepository contraseniaComunRepository;

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

        Usuario result = userService.login(loginData);

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

        Usuario result = userService.login(loginData);

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

    // ==================== TESTS PARA cambiarContrasenia ====================

    @Test
    @DisplayName("cambiarContrasenia: debe cambiar la contraseña exitosamente cuando el usuario existe y la contraseña es válida")
    void testCambiarContraseniaExitoso() {
        // Arrange
        String email = "user@example.com";
        String nuevaContrasenia = "NuevaPassword123!";
        
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        
        List<String> contraseniasUsadas = new ArrayList<>(Arrays.asList("hash1", "hash2", "hash3"));
        Contrasenia contraseniaActual = new Contrasenia("pass1", null, "hash3", contraseniasUsadas);
        usuario.setContrasenia(contraseniaActual);
        
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(contraseniaComunRepository.existsById(anyString())).thenReturn(false);
        
        // Act
        boolean resultado = userService.cambiarContrasenia(email, nuevaContrasenia);
        
        // Assert
        assertTrue(resultado, "El cambio de contraseña debe ser exitoso");
        assertNotNull(usuario.getContrasenia(), "La contraseña no debe ser nula");
        assertEquals(4, usuario.getContrasenia().getContraseniasUsadas().size(), 
                    "Debe tener 4 contraseñas en el historial");
    }

    @Test
    @DisplayName("cambiarContrasenia: debe retornar false cuando el usuario no existe")
    void testCambiarContraseniaUsuarioNoExiste() {
        // Arrange
        String email = "noexiste@example.com";
        String nuevaContrasenia = "NuevaPassword123!";
        
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act
        boolean resultado = userService.cambiarContrasenia(email, nuevaContrasenia);
        
        // Assert
        assertFalse(resultado, "Debe retornar false cuando el usuario no existe");
    }

    @Test
    @DisplayName("cambiarContrasenia: debe lanzar excepción cuando la contraseña es común")
    void testCambiarContraseniaPasswordComun() {
        // Arrange
        String email = "user@example.com";
        String nuevaContrasenia = "password123"; // Contraseña común
        
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        
        List<String> contraseniasUsadas = new ArrayList<>(Arrays.asList("hash1"));
        Contrasenia contraseniaActual = new Contrasenia("pass1", null, "hash1", contraseniasUsadas);
        usuario.setContrasenia(contraseniaActual);
        
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        // Simular que la contraseña está en la lista de contraseñas comunes
        when(contraseniaComunRepository.existsById(anyString())).thenReturn(true);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.cambiarContrasenia(email, nuevaContrasenia));
        
        assertTrue(exception.getMessage().contains("contraseñas comunes"),
                  "El mensaje debe indicar que es una contraseña común");
    }

    // ==================== TESTS PARA comprobarContraseniasAntiguas ====================

    @Test
    @DisplayName("comprobarContraseniasAntiguas: debe agregar nueva contraseña al historial cuando hay menos de 5")
    void testComprobarContraseniasAntiguasMenosDeCinco() {
        // Arrange
        List<String> contraseniasUsadas = new ArrayList<>(Arrays.asList("hash1", "hash2", "hash3"));
        Contrasenia contrasenia = new Contrasenia("pass1", null, "hash3", contraseniasUsadas);
        String nuevaContraseniaHash = "hashNueva";
        
        // Act
        Contrasenia resultado = userService.comprobarContraseniasAntiguas(contrasenia, nuevaContraseniaHash);
        
        // Assert
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(4, resultado.getContraseniasUsadas().size(), 
                    "Debe tener 4 contraseñas en el historial");
        assertTrue(resultado.getContraseniasUsadas().contains(nuevaContraseniaHash),
                  "Debe contener la nueva contraseña");
        assertTrue(resultado.getContraseniasUsadas().contains("hash1"),
                  "Debe mantener las contraseñas antiguas");
        assertEquals(nuevaContraseniaHash, resultado.getContraseniaActual(),
                    "La contraseña actual debe ser la nueva");
    }

    @Test
    @DisplayName("comprobarContraseniasAntiguas: debe eliminar la más antigua cuando hay 5 contraseñas")
    void testComprobarContraseniasAntiguasCincoContrasenias() {
        // Arrange
        List<String> contraseniasUsadas = new ArrayList<>(Arrays.asList(
            "hash1", "hash2", "hash3", "hash4", "hash5"
        ));
        Contrasenia contrasenia = new Contrasenia("pass1", null, "hash5", contraseniasUsadas);
        String nuevaContraseniaHash = "hashNueva";
        
        // Act
        Contrasenia resultado = userService.comprobarContraseniasAntiguas(contrasenia, nuevaContraseniaHash);
        
        // Assert
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(5, resultado.getContraseniasUsadas().size(), 
                    "Debe mantener solo 5 contraseñas en el historial");
        assertTrue(resultado.getContraseniasUsadas().contains(nuevaContraseniaHash),
                  "Debe contener la nueva contraseña");
        assertFalse(resultado.getContraseniasUsadas().contains("hash1"),
                   "NO debe contener la contraseña más antigua (hash1)");
        assertTrue(resultado.getContraseniasUsadas().contains("hash2"),
                  "Debe mantener hash2");
        assertTrue(resultado.getContraseniasUsadas().contains("hash5"),
                  "Debe mantener hash5");
        assertEquals(nuevaContraseniaHash, resultado.getContraseniaActual(),
                    "La contraseña actual debe ser la nueva");
    }

    @Test
    @DisplayName("comprobarContraseniasAntiguas: debe lanzar excepción cuando la contraseña ya fue usada")
    void testComprobarContraseniasAntiguasPasswordYaUsada() {
        // Arrange
        String contraseniaRepetida = "hash3";
        List<String> contraseniasUsadas = new ArrayList<>(Arrays.asList(
            "hash1", "hash2", contraseniaRepetida, "hash4"
        ));
        Contrasenia contrasenia = new Contrasenia("pass1", null, "hash4", contraseniasUsadas);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.comprobarContraseniasAntiguas(contrasenia, contraseniaRepetida));
        
        assertTrue(exception.getMessage().contains("ya ha sido usada"),
                  "El mensaje debe indicar que la contraseña ya fue usada");
    }

    @Test
    @DisplayName("comprobarContraseniasAntiguas: debe manejar correctamente el historial vacío")
    void testComprobarContraseniasAntiguasHistorialVacio() {
        // Arrange
        List<String> contraseniasUsadas = new ArrayList<>();
        Contrasenia contrasenia = new Contrasenia("pass1", null, "hashActual", contraseniasUsadas);
        String nuevaContraseniaHash = "hashNueva";
        
        // Act
        Contrasenia resultado = userService.comprobarContraseniasAntiguas(contrasenia, nuevaContraseniaHash);
        
        // Assert
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1, resultado.getContraseniasUsadas().size(), 
                    "Debe tener 1 contraseña en el historial");
        assertTrue(resultado.getContraseniasUsadas().contains(nuevaContraseniaHash),
                  "Debe contener la nueva contraseña");
        assertEquals(nuevaContraseniaHash, resultado.getContraseniaActual(),
                    "La contraseña actual debe ser la nueva");
    }

    @Test
    @DisplayName("comprobarContraseniasAntiguas: debe mantener el orden correcto al eliminar la más antigua")
    void testComprobarContraseniasAntiguasOrdenCorrecto() {
        // Arrange
        List<String> contraseniasUsadas = new ArrayList<>(Arrays.asList(
            "oldest", "old", "medium", "recent", "newest"
        ));
        Contrasenia contrasenia = new Contrasenia("pass1", null, "newest", contraseniasUsadas);
        String nuevaContraseniaHash = "brandNew";
        
        // Act
        Contrasenia resultado = userService.comprobarContraseniasAntiguas(contrasenia, nuevaContraseniaHash);
        
        // Assert
        List<String> historialFinal = resultado.getContraseniasUsadas();
        assertEquals(5, historialFinal.size(), "Debe tener exactamente 5 contraseñas");
        assertEquals("old", historialFinal.get(0), "La primera debe ser 'old' (se eliminó 'oldest')");
        assertEquals("medium", historialFinal.get(1), "La segunda debe ser 'medium'");
        assertEquals("recent", historialFinal.get(2), "La tercera debe ser 'recent'");
        assertEquals("newest", historialFinal.get(3), "La cuarta debe ser 'newest'");
        assertEquals("brandNew", historialFinal.get(4), "La última debe ser la nueva contraseña");
    }
}
