package iso25.g05.esi_media.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import iso25.g05.esi_media.exception.RecursoNoEncontradoException;
import iso25.g05.esi_media.model.Valoracion;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Contenido;
import iso25.g05.esi_media.repository.ContenidoRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.repository.ValoracionRepository;

@DisplayName("Tests de ValoracionService (TDD)")
class ValoracionServiceTest {

    @Mock
    private ValoracionRepository valoracionRepository;

    @Mock
    private ContenidoRepository contenidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ValoracionService valoracionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("NoPuedeValorarSiTokenGestorTest: debe rechazar valoración cuando el token corresponde a Gestor")
    void NoPuedeValorarSiTokenGestorTest() {
        String vid = "U1";
        String cid = "C1";

        // Simular que existe una asociación previa (visto pero no valorado)
        Valoracion v = new Valoracion();
        v.setVisualizadorId(vid);
        v.setContenidoId(cid);
        v.setValoracionFinal(null);
        when(valoracionRepository.findByVisualizadorIdAndContenidoId(vid, cid)).thenReturn(Optional.of(v));

        // El usuario no es Visualizador (es Gestor)
        GestordeContenido gestor = new GestordeContenido();
        gestor.setId(vid);
        when(usuarioRepository.findById(vid)).thenReturn(Optional.of(gestor));

        // Al intentar valorar debe lanzarse IllegalStateException por tipo de usuario
        assertThrows(IllegalStateException.class, () -> valoracionService.rateContent(vid, cid, 4.0));
    }

    @Test
    @DisplayName("NoPuedeVolverAValorarContenidoTest: no se puede modificar una valoración ya realizada")
    void NoPuedeVolverAValorarContenidoTest() {
        String vid = "U2";
        String cid = "C2";

        Valoracion v = new Valoracion();
        v.setVisualizadorId(vid);
        v.setContenidoId(cid);
        v.setValoracionFinal(3.5); // ya valorado

        when(valoracionRepository.findByVisualizadorIdAndContenidoId(vid, cid)).thenReturn(Optional.of(v));

        // Usuario es Visualizador
        Visualizador vis = new Visualizador();
        vis.setId(vid);
        when(usuarioRepository.findById(vid)).thenReturn(Optional.of(vis));

        // Intento de revaloración -> debe lanzar IllegalStateException
        assertThrows(IllegalStateException.class, () -> valoracionService.rateContent(vid, cid, 4.0));
    }

    @Test
    @DisplayName("NoPuedeValorarContenidoInexistenteTest: no se puede valorar un contenido que no existe")
    void NoPuedeValorarContenidoInexistenteTest() {
        String vid = "U3";
        String cid = "C999"; // contenido inexistente

        when(contenidoRepository.findById(cid)).thenReturn(Optional.empty());

        // Usuario es Visualizador
        Visualizador vis = new Visualizador();
        vis.setId(vid);
        when(usuarioRepository.findById(vid)).thenReturn(Optional.of(vis));

        // No debe poder valorar un contenido que no existe -> lanzar RecursoNoEncontradoException
        assertThrows(RecursoNoEncontradoException.class, () -> valoracionService.rateContent(vid, cid, 5.0));
    }

    @Test
    @DisplayName("ValoracionMenorQueMinimoTest: debe rechazar puntuaciones menores que 1.0")
    void ValoracionMenorQueMinimoTest() {
        String vid = "U4";
        String cid = "C4";

        // Simular contenido y usuario existentes y asociación previa
        when(contenidoRepository.findById(cid)).thenReturn(Optional.of(new Contenido()));
        Visualizador vis = new Visualizador(); vis.setId(vid);
        when(usuarioRepository.findById(vid)).thenReturn(Optional.of(vis));
        when(valoracionRepository.findByVisualizadorIdAndContenidoId(vid, cid)).thenReturn(Optional.of(new Valoracion()));

        assertThrows(IllegalArgumentException.class, () -> valoracionService.rateContent(vid, cid, 0.5));
    }

    @Test
    @DisplayName("ValoracionMayorQueMaximoTest: debe rechazar puntuaciones mayores que 5.0")
    void ValoracionMayorQueMaximoTest() {
        String vid = "U5";
        String cid = "C5";

        when(contenidoRepository.findById(cid)).thenReturn(Optional.of(new Contenido()));
        Visualizador vis = new Visualizador(); vis.setId(vid);
        when(usuarioRepository.findById(vid)).thenReturn(Optional.of(vis));
        when(valoracionRepository.findByVisualizadorIdAndContenidoId(vid, cid)).thenReturn(Optional.of(new Valoracion()));

        assertThrows(IllegalArgumentException.class, () -> valoracionService.rateContent(vid, cid, 5.5));
    }

    @Test
    @DisplayName("NoPuedeValorarSiTokenAdministradorTest: debe rechazar valoración cuando el token corresponde a Administrador")
    void NoPuedeValorarSiTokenAdministradorTest() {
        String vid = "ADMIN1";
        String cid = "C6";

        Valoracion v = new Valoracion();
        v.setVisualizadorId(vid);
        v.setContenidoId(cid);
        v.setValoracionFinal(null);

        when(valoracionRepository.findByVisualizadorIdAndContenidoId(vid, cid)).thenReturn(Optional.of(v));

        // Usuario es Administrador
        iso25.g05.esi_media.model.Administrador admin = new iso25.g05.esi_media.model.Administrador();
        admin.setId(vid);
        when(usuarioRepository.findById(vid)).thenReturn(Optional.of(admin));

        assertThrows(IllegalStateException.class, () -> valoracionService.rateContent(vid, cid, 4.0));
    }

    @Test
    @DisplayName("NoPuedeValorarSiNoHaVistoContenidoTest: no se puede valorar si no se ha reproducido antes (no existe asociación)")
    void NoPuedeValorarSiNoHaVistoContenidoTest() {
        String vid = "U7";
        String cid = "C7";

        // Contenido y usuario existen
        when(contenidoRepository.findById(cid)).thenReturn(Optional.of(new Contenido()));
        Visualizador vis = new Visualizador(); vis.setId(vid);
        when(usuarioRepository.findById(vid)).thenReturn(Optional.of(vis));

        // No existe asociación valoracion (no ha visto)
        when(valoracionRepository.findByVisualizadorIdAndContenidoId(vid, cid)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> valoracionService.rateContent(vid, cid, 4.0));
    }
}
