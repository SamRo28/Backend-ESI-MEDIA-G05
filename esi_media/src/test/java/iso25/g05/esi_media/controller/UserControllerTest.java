package iso25.g05.esi_media.controller;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import iso25.g05.esi_media.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UsuarioController userController;

    /*@Test
    void testLoginSuccess() {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "test@example.com");
        loginData.put("password", "password");

        Usuario user = new Usuario();
        user.setEmail("test@example.com");

        when(userService.login(loginData)).thenReturn(user);

        ResponseEntity<?> response = userController.login(loginData);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }*/

    @Test
    void testLoginFailure() {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "test@example.com");
        loginData.put("password", "wrongpassword");

        when(userService.login(loginData)).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThrows(ResponseStatusException.class, () -> {
            userController.login(loginData, request);
        });
    }

    @Test
    void testLogin3Auth() {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "test@example.com");

        userController.login3Auth(loginData);

        verify(userService, times(1)).login3Auth(loginData);
    }
}
