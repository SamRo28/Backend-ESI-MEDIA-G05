package iso25.g05.esi_media.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.exception.AccesoNoAutorizadoException;
import iso25.g05.esi_media.exception.PeticionInvalidaException;
import iso25.g05.esi_media.exception.RecursoNoEncontradoException;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Video;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.ContraseniaRepository;
import iso25.g05.esi_media.repository.ContenidoRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.repository.VisualizadorRepository;
import jakarta.validation.Validator;

@DisplayName("Favoritos: visualizador gestiona su lista")
class FavoritosTest {

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
        MockitoAnnotations.openMocks(this);
        // Inyectar dependencias @Autowired que no van por constructor
        ReflectionTestUtils.setField(visualizadorService, "userService", userService);
        ReflectionTestUtils.setField(visualizadorService, "contraseniaComunRepository", contraseniaComunRepository);
        ReflectionTestUtils.setField(visualizadorService, "contenidoRepository", contenidoRepository);
        ReflectionTestUtils.setField(visualizadorService, "logService", logService);
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

    @Test
    @DisplayName("Inicializa lista de favoritos si viene null antes de agregar")
    void agregarFavorito_inicializaListaNull() {
        Visualizador visualizador = crearVisualizadorAutenticado();
        visualizador.setContenidofav(null);
        prepararToken("token-header", visualizador);
        Video contenido = crearVideo("fav-1", true);
        when(contenidoRepository.findByIdAndEstadoTrue("fav-1")).thenReturn(Optional.of(contenido));

        visualizadorService.agregarFavorito("token-header", "fav-1");

        assertNotNull(visualizador.getContenidofav());
        assertEquals(1, visualizador.getContenidofav().size());
        verify(usuarioRepository, times(1)).save(visualizador);
    }

    @Test
    @DisplayName("Obtener favoritos sin token lanza 400")
    void obtenerFavoritos_sinToken_lanza400() {
        assertThrows(PeticionInvalidaException.class, () -> visualizadorService.obtenerFavoritos("   "));
    }

    @Test
    @DisplayName("Solo un visualizador puede gestionar favoritos")
    void favoritos_restringidoASoloVisualizador() {
        Usuario admin = new Usuario();
        admin.setEmail("admin@esi.es");
        String token = "session-token";

        when(userService.extraerToken("header")).thenReturn(token);
        when(usuarioRepository.findBySesionToken(token)).thenReturn(Optional.of(admin));

        assertThrows(AccesoNoAutorizadoException.class, () -> visualizadorService.obtenerFavoritos("header"));
        assertThrows(AccesoNoAutorizadoException.class, () -> visualizadorService.agregarFavorito("header", "c1"));
        assertThrows(AccesoNoAutorizadoException.class, () -> visualizadorService.eliminarFavorito("header", "c1"));
    }

    @Test
    @DisplayName("Eliminar favorito sobre lista vacía no guarda nada")
    void eliminarFavorito_sinFavoritos_noPersiste() {
        Visualizador visualizador = crearVisualizadorAutenticado();
        prepararToken("token-header", visualizador);

        visualizadorService.eliminarFavorito("token-header", "c2");

        verify(usuarioRepository, never()).save(any());
        assertTrue(visualizador.getContenidofav().isEmpty());
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
