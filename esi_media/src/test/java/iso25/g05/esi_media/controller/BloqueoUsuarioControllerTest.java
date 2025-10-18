package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests para BloqueoUsuarioController siguiendo TDD
 * Historia de Usuario: Como administrador, quiero poder bloquear/desbloquear usuarios
 */
@ExtendWith(MockitoExtension.class)
public class BloqueoUsuarioControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private BloqueoUsuarioController bloqueoController;

    private Administrador adminAutenticado;
    private Visualizador usuarioABloquear;

    @BeforeEach
    void setUp() {
        // Admin autenticado
        Contrasenia contraAdmin = new Contrasenia(null, null, "admin123", null);
        adminAutenticado = new Administrador(
            "García", false, contraAdmin, "admin@test.com", null, "Admin", "TI"
        );
        adminAutenticado.setId("admin-id-123");

        // Usuario a bloquear (Visualizador)
        Contrasenia contraUsuario = new Contrasenia(null, null, "user123", null);
        usuarioABloquear = new Visualizador(
            "Pérez", false, contraUsuario, "user@test.com", null, "Usuario", "userAlias", new Date(), false
        );
        usuarioABloquear.setId("user-id-456");
    }

    // ========== TESTS PARA BLOQUEAR USUARIO ==========

    @Test
    void testBloquearUsuario_Exitoso() {
        // Given: Admin autenticado y usuario no bloqueado
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.of(adminAutenticado));
        when(usuarioRepository.findById("user-id-456")).thenReturn(Optional.of(usuarioABloquear));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioABloquear);

        // When: Bloquear usuario
        ResponseEntity<Map<String, String>> response = bloqueoController.bloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Then: Usuario bloqueado correctamente
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Usuario bloqueado correctamente", response.getBody().get("mensaje"));
        
        // Verificar que se llamó a setBloqueado(true)
        verify(usuarioRepository).save(argThat(usuario -> usuario.isBloqueado()));
        
        // Verificar auditoría
        verify(logService).registrarBloqueoUsuario(
            eq("admin-id-123"), 
            eq("admin@test.com"), 
            eq("user-id-456"), 
            eq("user@test.com")
        );
    }

    @Test
    void testBloquearUsuario_SinAdminId() {
        // When: Intentar bloquear sin Admin-ID
        ResponseEntity<Map<String, String>> response = bloqueoController.bloquearUsuario(
            "user-id-456", null
        );

        // Then: Error 401
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("autenticación de administrador"));
        
        // No se debe guardar ni registrar
        verify(usuarioRepository, never()).save(any());
        verify(logService, never()).registrarBloqueoUsuario(any(), any(), any(), any());
    }

    @Test
    void testBloquearUsuario_AdminNoExiste() {
        // Given: Admin no existe
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.empty());

        // When: Intentar bloquear
        ResponseEntity<Map<String, String>> response = bloqueoController.bloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Then: Error 403
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("administrador"));
    }

    @Test
    void testBloquearUsuario_UsuarioNoExiste() {
        // Given: Admin existe pero usuario no
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.of(adminAutenticado));
        when(usuarioRepository.findById("user-id-456")).thenReturn(Optional.empty());

        // When: Intentar bloquear
        ResponseEntity<Map<String, String>> response = bloqueoController.bloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Then: Error 404
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Usuario no encontrado"));
    }

    @Test
    void testBloquearUsuario_YaBloqueado() {
        // Given: Usuario ya está bloqueado
        usuarioABloquear.setBloqueado(true);
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.of(adminAutenticado));
        when(usuarioRepository.findById("user-id-456")).thenReturn(Optional.of(usuarioABloquear));

        // When: Intentar bloquear de nuevo
        ResponseEntity<Map<String, String>> response = bloqueoController.bloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Then: Error 400
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("ya está bloqueado"));
        
        verify(usuarioRepository, never()).save(any());
    }

    // ========== TESTS PARA DESBLOQUEAR USUARIO ==========

    @Test
    void testDesbloquearUsuario_Exitoso() {
        // Given: Admin autenticado y usuario bloqueado
        usuarioABloquear.setBloqueado(true);
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.of(adminAutenticado));
        when(usuarioRepository.findById("user-id-456")).thenReturn(Optional.of(usuarioABloquear));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioABloquear);

        // When: Desbloquear usuario
        ResponseEntity<Map<String, String>> response = bloqueoController.desbloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Then: Usuario desbloqueado correctamente
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Usuario desbloqueado correctamente", response.getBody().get("mensaje"));
        
        // Verificar que se llamó a setBloqueado(false)
        verify(usuarioRepository).save(argThat(usuario -> !usuario.isBloqueado()));
        
        // Verificar auditoría
        verify(logService).registrarDesbloqueoUsuario(
            eq("admin-id-123"), 
            eq("admin@test.com"), 
            eq("user-id-456"), 
            eq("user@test.com")
        );
    }

    @Test
    void testDesbloquearUsuario_NoEstaBloqueado() {
        // Given: Usuario NO está bloqueado
        usuarioABloquear.setBloqueado(false);
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.of(adminAutenticado));
        when(usuarioRepository.findById("user-id-456")).thenReturn(Optional.of(usuarioABloquear));

        // When: Intentar desbloquear
        ResponseEntity<Map<String, String>> response = bloqueoController.desbloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Then: Error 400
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("no está bloqueado"));
        
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void testDesbloquearUsuario_SinAdminId() {
        // When: Intentar desbloquear sin Admin-ID
        ResponseEntity<Map<String, String>> response = bloqueoController.desbloquearUsuario(
            "user-id-456", null
        );

        // Then: Error 401
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }
}
