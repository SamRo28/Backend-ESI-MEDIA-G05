package iso25.g05.esi_media.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

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
}
