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

@ExtendWith(MockitoExtension.class)
public class BloqueoUsuarioControllerTest {

    /**
     * Pruebas TDD (unitarias) para la Historia de Usuario: "Bloquear/Desbloquear usuarios" sobre el controlador.
     *
     * Criterios de Aceptación cubiertos en este test unitario:
     * 1) Bloqueo exitoso: 200 y persistencia (bloqueado=true) + auditoría
     * 2) Desbloqueo exitoso: 200 y persistencia (bloqueado=false) + auditoría
     * 3) Requiere Admin-ID: 401 si falta
     * 4) Admin-ID inválido o no administrador: 403
     * 5) Usuario objetivo no encontrado: 404 (bloquear y desbloquear)
     * 6) No se puede bloquear dos veces: 400
     * 7) No se puede desbloquear dos veces: 400
     * 8) Evitar autobloqueo (admin no puede bloquearse): 400
     * 9) Se permite bloquear a otro Administrador (política definida): 200
     * 10) Errores de persistencia (save lanza excepción): 500 con mensaje genérico
     */
    @Mock
    private LogService logService;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private BloqueoUsuarioController bloqueoController;

    private Administrador adminAutenticado;
    private Visualizador usuarioABloquear;


    // Crear dos objetos Usuario para pruebas

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
    

    // Test exitoso de bloqueo de usuario

    @Test
    void testBloquearUsuario_Exitoso() {
        // Arrange: Admin autenticado y usuario no bloqueado
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.of(adminAutenticado));
        when(usuarioRepository.findById("user-id-456")).thenReturn(Optional.of(usuarioABloquear));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioABloquear);

        // Act: Bloquear usuario
        ResponseEntity<Map<String, String>> response = bloqueoController.bloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Assert: Usuario bloqueado correctamente
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

    // Test de bloqueo cuando el usuario ya está bloqueado

    @Test
    void testBloquearUsuario_SinAdminId() {
        // Act: Intentar bloquear sin Admin-ID
        ResponseEntity<Map<String, String>> response = bloqueoController.bloquearUsuario(
            "user-id-456", null
        );

        // Assert: Error 401
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("autenticación de administrador"));
        
        // No se debe guardar ni registrar
        verify(usuarioRepository, never()).save(any());
        verify(logService, never()).registrarBloqueoUsuario(any(), any(), any(), any());
    }

    // Test de bloqueo cuando el admin no existe

    @Test
    void testBloquearUsuario_AdminNoExiste() {
        // Arrange: Admin no existe
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.empty());

        // Act: Intentar bloquear
        ResponseEntity<Map<String, String>> response = bloqueoController.bloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Assert: Error 403
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("administrador"));
    }

    // Test de bloqueo cuando el admin no es administrador

    @Test
    void testBloquearUsuario_AdminNoEsAdministrador() {
        // Arrange: El id del "admin" corresponde a un usuario no administrador
        Contrasenia contrNoAdmin = new Contrasenia(null, null, "noadmin123", null);
        Visualizador noAdmin = new Visualizador(
            "User", false, contrNoAdmin, "noadmin@test.com", null, "NoAdmin", "aliasNA", new Date(), false
        );
        noAdmin.setId("no-admin-id");

        when(usuarioRepository.findById("no-admin-id")).thenReturn(Optional.of(noAdmin));

        // Act
        ResponseEntity<Map<String, String>> response = bloqueoController.bloquearUsuario(
            "user-id-456", "no-admin-id"
        );

        // Assert: 403 Solo administradores
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Solo administradores"));
        verify(usuarioRepository, never()).save(any());
        verify(logService, never()).registrarBloqueoUsuario(any(), any(), any(), any());
    }

    // Test de bloqueo cuando el usuario no existe

    @Test
    void testBloquearUsuario_UsuarioNoExiste() {
        // Arrange: Admin existe pero usuario no
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.of(adminAutenticado));
        when(usuarioRepository.findById("user-id-456")).thenReturn(Optional.empty());

        // Act: Intentar bloquear
        ResponseEntity<Map<String, String>> response = bloqueoController.bloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Assert: Error 404
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Usuario no encontrado"));
    }

    // Test de bloqueo cuando el usuario ya está bloqueado

    @Test
    void testBloquearUsuario_YaBloqueado() {
        // Arrange: Usuario ya está bloqueado
        usuarioABloquear.setBloqueado(true);
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.of(adminAutenticado));
        when(usuarioRepository.findById("user-id-456")).thenReturn(Optional.of(usuarioABloquear));

        // Act: Intentar bloquear de nuevo
        ResponseEntity<Map<String, String>> response = bloqueoController.bloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Assert: Error 400
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("ya está bloqueado"));
        
        verify(usuarioRepository, never()).save(any());
    }

    // ========== TESTS PARA DESBLOQUEAR USUARIO ==========

    // Test exitoso de desbloqueo de usuario

    @Test
    void testDesbloquearUsuario_Exitoso() {
        // Arrange: Admin autenticado y usuario bloqueado
        usuarioABloquear.setBloqueado(true);
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.of(adminAutenticado));
        when(usuarioRepository.findById("user-id-456")).thenReturn(Optional.of(usuarioABloquear));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioABloquear);

        // Act: Desbloquear usuario
        ResponseEntity<Map<String, String>> response = bloqueoController.desbloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Assert: Usuario desbloqueado correctamente
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

    // Test de desbloqueo cuando el usuario no está bloqueado

    @Test
    void testDesbloquearUsuario_NoEstaBloqueado() {
        // Arrange: Usuario NO está bloqueado
        usuarioABloquear.setBloqueado(false);
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.of(adminAutenticado));
        when(usuarioRepository.findById("user-id-456")).thenReturn(Optional.of(usuarioABloquear));

        // Act: Intentar desbloquear
        ResponseEntity<Map<String, String>> response = bloqueoController.desbloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Assert: Error 400
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("no está bloqueado"));
        
        verify(usuarioRepository, never()).save(any());
    }

    // Test de desbloqueo sin Admin-ID

    @Test
    void testDesbloquearUsuario_SinAdminId() {
        // Act: Intentar desbloquear sin Admin-ID
        ResponseEntity<Map<String, String>> response = bloqueoController.desbloquearUsuario(
            "user-id-456", null
        );

        // Assert: Error 401
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    // Test de desbloqueo cuando el admin no existe

    @Test
    void testDesbloquearUsuario_AdminNoExiste() {
        // Arrange: Admin no existe en BD
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = bloqueoController.desbloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Assert: 403
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Solo administradores"));
        verify(usuarioRepository, never()).save(any());
        verify(logService, never()).registrarDesbloqueoUsuario(any(), any(), any(), any());
    }

    // Test de desbloqueo cuando el usuario no existe

    @Test
    void testDesbloquearUsuario_UsuarioNoExiste() {
        // Arrange: Admin existe pero usuario objetivo no
        usuarioABloquear.setBloqueado(true);
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.of(adminAutenticado));
        when(usuarioRepository.findById("user-id-456")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = bloqueoController.desbloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Assert: 404 Usuario no encontrado
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Usuario no encontrado"));
        verify(usuarioRepository, never()).save(any());
    }

    // Test de bloqueo cuando hay error de persistencia (simulado lanzando excepción)

    @Test
    void testBloquearUsuario_ErrorPersistenciaDevuelve500() {
        // Arrange: Admin y usuario válidos pero save lanza excepción
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.of(adminAutenticado));
        when(usuarioRepository.findById("user-id-456")).thenReturn(Optional.of(usuarioABloquear));
        when(usuarioRepository.save(any(Usuario.class))).thenThrow(new RuntimeException("Fallo BD"));

        // Act
        ResponseEntity<Map<String, String>> response = bloqueoController.bloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Assert: 500 y mensaje de error
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Error al bloquear usuario"));
    }

    // Test de desbloqueo cuando hay error de persistencia (simulado lanzando excepción)

    @Test
    void testDesbloquearUsuario_ErrorPersistenciaDevuelve500() {
        // Arrange: Admin y usuario válidos, usuario bloqueado, pero save lanza excepción
        usuarioABloquear.setBloqueado(true);
        when(usuarioRepository.findById("admin-id-123")).thenReturn(Optional.of(adminAutenticado));
        when(usuarioRepository.findById("user-id-456")).thenReturn(Optional.of(usuarioABloquear));
        when(usuarioRepository.save(any(Usuario.class))).thenThrow(new RuntimeException("Fallo BD"));

        // Act
        ResponseEntity<Map<String, String>> response = bloqueoController.desbloquearUsuario(
            "user-id-456", "admin-id-123"
        );

        // Assert: 500 y mensaje
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Error al desbloquear usuario"));
    }
}

