package iso25.g05.esi_media.service;

import iso25.g05.esi_media.model.Administrador;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.dto.VisualizadorGestionDTO;
import iso25.g05.esi_media.dto.AdministradorGestionDTO;
import iso25.g05.esi_media.dto.GestorGestionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioGestionServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private iso25.g05.esi_media.repository.TokenRepository tokenRepository;

    @InjectMocks
    private UsuarioGestionService usuarioGestionService;

    private Token token;
    private Administrador admin;

    @BeforeEach
    void setUp() {
        admin = new Administrador();
        admin.setNombre("Admin");
        admin.setApellidos("Principal");
        admin.setEmail("admin@example.com");
        token = new Token();
        token.setToken("token123");
        token.setFechaExpiracion(new Date(System.currentTimeMillis() + 3600_000));
        token.setUsuario(admin);
    }

    @Test
    void testObtenerAdministradores_Success() {
        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));
        when(mongoTemplate.find(any(), eq(Administrador.class), eq("users"))).thenReturn(List.of(admin));

        List<AdministradorGestionDTO> result = usuarioGestionService.obtenerAdministradores("Bearer token123");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Admin", result.get(0).getNombre());
        verify(mongoTemplate, times(1)).find(any(), eq(Administrador.class), eq("users"));
    }

    @Test
    void testObtenerAdministradores_TokenMissing() {
        // header not starting with Bearer should throw
        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioGestionService.obtenerAdministradores(null));
        assertTrue(ex.getMessage().toLowerCase().contains("token de autorización"));
    }

    @Test
    void testValidarAdministrador_NotAdmin_Throws() {
        Usuario user = new Usuario();
        token.setUsuario(user);
        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioGestionService.obtenerAdministradores("Bearer token123"));
        assertTrue(ex.getMessage().contains("Solo los administradores"));
    }

    @Test
    void testObtenerVisualizadorPorId_NotFound() {
        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));
        when(mongoTemplate.findOne(any(), eq(Visualizador.class), eq("users"))).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioGestionService.obtenerVisualizadorPorId("v1", "Bearer token123"));
        assertTrue(ex.getMessage().contains("Visualizador no encontrado"));
    }

    @Test
    void testObtenerGestorPorId_NotFound() {
        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));
        when(mongoTemplate.findOne(any(), eq(GestordeContenido.class), eq("users"))).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioGestionService.obtenerGestorPorId("g1", "Bearer token123"));
        assertTrue(ex.getMessage().contains("Gestor no encontrado"));
    }

    @Test
    void testObtenerGestores_Success() {
        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));
        GestordeContenido g = new GestordeContenido();
        g.setId("g1");
        when(mongoTemplate.find(any(), eq(GestordeContenido.class), eq("users"))).thenReturn(List.of(g));

        var res = usuarioGestionService.obtenerGestores("Bearer token123");
        assertNotNull(res);
        assertEquals(1, res.size());
    }

    @Test
    void testModificarVisualizador_Success() {
        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        Visualizador v = new Visualizador();
        v.setId("v1");
        v.setNombre("Old");
        v.setApellidos("OldA");
        v.setAlias("oldalias");

        when(mongoTemplate.findOne(any(), eq(Visualizador.class), eq("users"))).thenReturn(v);

        VisualizadorGestionDTO dto = new VisualizadorGestionDTO();
        dto.setNombre("New");
        dto.setApellidos("NewA");
        dto.setAlias("newalias");
        dto.setFoto("foto.png");
        dto.setFechanac(new Date(631152000000L));

        VisualizadorGestionDTO res = usuarioGestionService.modificarVisualizador("v1", dto, "Bearer token123");

        assertEquals("New", res.getNombre());
        assertEquals("NewA", res.getApellidos());
        assertEquals("newalias", res.getAlias());
        verify(mongoTemplate, times(1)).save(any(), eq("users"));
    }

    @Test
    void testModificarGestor_Success() {
        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

    GestordeContenido g = new GestordeContenido();
        g.setId("gest1");
        g.setNombre("OldG");
        g.setApellidos("OldGA");
        g.setalias("oldalias");
        g.setcampoespecializacion("oldesp");
        g.setdescripcion("olddesc");
    when(mongoTemplate.findOne(any(), eq(GestordeContenido.class), eq("users"))).thenReturn(g);

    GestorGestionDTO dto = new GestorGestionDTO();
        dto.setNombre("NewG");
        dto.setApellidos("NewGA");
        dto.setAlias("newalias");
        dto.setCampoespecializacion("newesp");
        dto.setDescripcion("newdesc");
        dto.setFoto("foto_g.png");

    GestorGestionDTO res = usuarioGestionService.modificarGestor("gest1", dto, "Bearer token123");

        assertEquals("NewG", res.getNombre());
        assertEquals("NewGA", res.getApellidos());
        assertEquals("newalias", res.getAlias());
        assertEquals("newesp", res.getCampoespecializacion());
        assertEquals("newdesc", res.getDescripcion());
        verify(mongoTemplate, times(1)).save(any(), eq("users"));
    }

    @Test
    void testModificarGestor_NotFound() {
        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));
        when(mongoTemplate.findOne(any(), eq(GestordeContenido.class), eq("users"))).thenReturn(null);

        GestorGestionDTO dto = new GestorGestionDTO();
        dto.setNombre("X");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioGestionService.modificarGestor("noex", dto, "Bearer token123"));
        assertTrue(ex.getMessage().contains("Gestor no encontrado"));
    }

    @Test
    void testObtenerVisualizadores_Success() {
        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));
        Visualizador v = new Visualizador();
        v.setId("v1");
        when(mongoTemplate.find(any(), eq(Visualizador.class), eq("users"))).thenReturn(List.of(v));

        var res = usuarioGestionService.obtenerVisualizadores("Bearer token123");
        assertNotNull(res);
        assertEquals(1, res.size());
    }

    @Test
    void testModificarAdministrador_Success() {
        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        Administrador adm = new Administrador();
        adm.setId("adm1");
        adm.setNombre("OldA");
        adm.setApellidos("OldAA");
        adm.setDepartamento("OldDept");

        when(mongoTemplate.findOne(any(), eq(Administrador.class), eq("users"))).thenReturn(adm);

        AdministradorGestionDTO dto = new AdministradorGestionDTO();
        dto.setNombre("NewA");
        dto.setApellidos("NewAA");
        dto.setDepartamento("NewDept");
        dto.setFoto("foto_admin.png");

        AdministradorGestionDTO res = usuarioGestionService.modificarAdministrador("adm1", dto, "Bearer token123");

        assertEquals("NewA", res.getNombre());
        assertEquals("NewAA", res.getApellidos());
        assertEquals("NewDept", res.getDepartamento());
        verify(mongoTemplate, times(1)).save(any(), eq("users"));
    }

    @Test
    void testModificarAdministrador_NotFound() {
        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));
        when(mongoTemplate.findOne(any(), eq(Administrador.class), eq("users"))).thenReturn(null);

        AdministradorGestionDTO dto = new AdministradorGestionDTO();
        dto.setNombre("X");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioGestionService.modificarAdministrador("noex", dto, "Bearer token123"));
        assertTrue(ex.getMessage().contains("Administrador no encontrado"));
    }

    @Test
    void testObtenerAdministradorPorId_NotFound() {
        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));
        when(mongoTemplate.findOne(any(), eq(Administrador.class), eq("users"))).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioGestionService.obtenerAdministradorPorId("adm1", "Bearer token123"));
        assertTrue(ex.getMessage().contains("Administrador no encontrado"));
    }
}
