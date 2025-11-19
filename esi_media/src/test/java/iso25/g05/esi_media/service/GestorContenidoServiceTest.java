package iso25.g05.esi_media.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;

import iso25.g05.esi_media.dto.ContenidoDetalleDTO;
import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.dto.ContenidoUpdateDTO;
import iso25.g05.esi_media.exception.AccesoNoAutorizadoException;
import iso25.g05.esi_media.exception.PeticionInvalidaException;
import iso25.g05.esi_media.exception.RecursoNoEncontradoException;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.Contenido;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Video;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.ContenidoRepository;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;

/**
 * Tests unitarios para GestorContenidoService
 * Cubre listar, detalle, actualizar y eliminar contenidos
 * Valida permisos según tipo de gestor (video/audio)
 */
@ExtendWith(MockitoExtension.class)
class GestorContenidoServiceTest {

    @Mock
    private ContenidoRepository contenidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GestorDeContenidoRepository gestorRepository;

    @Mock
    private LogService logService;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private GestorContenidoService gestorContenidoService;

    private GestordeContenido gestorVideo;
    private GestordeContenido gestorAudio;
    private Token tokenValido;
    private Video video;
    private Audio audio;

    private static final String TOKEN_VALIDO = "Bearer token-valido-123";
    private static final String TOKEN_SIN_BEARER = "token-valido-123";
    private static final String ID_GESTOR_VIDEO = "gestor-video-id";
    private static final String ID_GESTOR_AUDIO = "gestor-audio-id";
    private static final String ID_VIDEO = "video-id-1";
    private static final String ID_AUDIO = "audio-id-1";

    @BeforeEach
    void setUp() {
        // Token válido
        tokenValido = new Token();
        tokenValido.setToken(TOKEN_SIN_BEARER);
        tokenValido.setExpirado(false);

        // Gestor de video
        gestorVideo = new GestordeContenido();
        gestorVideo.setId(ID_GESTOR_VIDEO);
        gestorVideo.setNombre("Gestor Video");
        gestorVideo.setEmail("gestorvideo@test.com");
        gestorVideo.settipocontenidovideooaudio("video");
        gestorVideo.setBloqueado(false);
        gestorVideo.setSesionstoken(tokenValido);

        // Gestor de audio
        gestorAudio = new GestordeContenido();
        gestorAudio.setId(ID_GESTOR_AUDIO);
        gestorAudio.setNombre("Gestor Audio");
        gestorAudio.setEmail("gestoraudio@test.com");
        gestorAudio.settipocontenidovideooaudio("audio");
        gestorAudio.setBloqueado(false);
        gestorAudio.setSesionstoken(tokenValido);

        // Video de prueba
        video = new Video();
        video.setId(ID_VIDEO);
        video.settitulo("Video Tutorial");
        video.setdescripcion("Tutorial de programación");
        video.setvip(false);
        video.setestado(true);
        video.setedadvisualizacion(0);
        video.setresolucion("1080p");
        video.seturl("http://example.com/video.mp4");
        Set<String> tagsVideo = new HashSet<>();
        tagsVideo.add("programacion");
        tagsVideo.add("tutorial");
        video.settags(new ArrayList<>(tagsVideo));
        video.setgestorId(ID_GESTOR_VIDEO);

        // Audio de prueba
        audio = new Audio();
        audio.setId(ID_AUDIO);
        audio.settitulo("Podcast Tech");
        audio.setdescripcion("Podcast sobre tecnología");
        audio.setvip(false);
        audio.setestado(true);
        audio.setedadvisualizacion(0);
        audio.setmimeType("audio/mpeg");
        Set<String> tagsAudio = new HashSet<>();
        tagsAudio.add("podcast");
        tagsAudio.add("tech");
        audio.settags(new ArrayList<>(tagsAudio));
        audio.setgestorId(ID_GESTOR_AUDIO);
    }

    // ==================== TESTS DE LISTADO ====================

    @Test
    void testListarContenidos_RetornaContenidosDelGestor() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contenido> pageContenidos = new PageImpl<>(Arrays.asList(video, audio));

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findAllContenidosForGestor(pageable)).thenReturn(pageContenidos);

        // Act
        Page<ContenidoResumenDTO> resultado = gestorContenidoService.listar(
            TOKEN_VALIDO, pageable, null, null
        );

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        verify(usuarioRepository).findBySesionToken(TOKEN_SIN_BEARER);
        verify(contenidoRepository).findAllContenidosForGestor(pageable);
        verify(logService).registrarAccion(eq("Listado de contenidos por gestor"), eq("gestorvideo@test.com"));
    }

    @Test
    void testListarSoloVideos_RetornaSoloVideos() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contenido> pageVideos = new PageImpl<>(Arrays.asList(video));

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findAllVideosForGestor(pageable)).thenReturn(pageVideos);

        // Act
        Page<ContenidoResumenDTO> resultado = gestorContenidoService.listar(
            TOKEN_VALIDO, pageable, "VIDEO", null
        );

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(contenidoRepository).findAllVideosForGestor(pageable);
    }

    @Test
    void testListarSoloAudios_RetornaSoloAudios() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contenido> pageAudios = new PageImpl<>(Arrays.asList(audio));

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorAudio));
        when(gestorRepository.findById(ID_GESTOR_AUDIO)).thenReturn(Optional.of(gestorAudio));
        when(contenidoRepository.findAllAudiosForGestor(pageable)).thenReturn(pageAudios);

        // Act
        Page<ContenidoResumenDTO> resultado = gestorContenidoService.listar(
            TOKEN_VALIDO, pageable, "AUDIO", null
        );

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(contenidoRepository).findAllAudiosForGestor(pageable);
    }

    @Test
    void testListarConBusqueda_RetornaResultadosFiltrados() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contenido> pageResultados = new PageImpl<>(Arrays.asList(video));

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.searchAllContenidosForGestor("tutorial", pageable))
            .thenReturn(pageResultados);

        // Act
        Page<ContenidoResumenDTO> resultado = gestorContenidoService.listar(
            TOKEN_VALIDO, pageable, null, "tutorial"
        );

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(contenidoRepository).searchAllContenidosForGestor("tutorial", pageable);
    }

    @Test
    void testListarSinToken_LanzaExcepcion() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        assertThrows(PeticionInvalidaException.class, () -> {
            gestorContenidoService.listar(null, pageable, null, null);
        });

        verify(contenidoRepository, never()).findAllContenidosForGestor(any());
    }

    @Test
    void testListarConTokenInvalido_LanzaExcepcion() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(usuarioRepository.findBySesionToken("token-invalido")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccesoNoAutorizadoException.class, () -> {
            gestorContenidoService.listar("Bearer token-invalido", pageable, null, null);
        });

        verify(contenidoRepository, never()).findAllContenidosForGestor(any());
    }

    @Test
    void testListarConVisualizador_LanzaExcepcion() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Visualizador visualizador = new Visualizador();
        visualizador.setId("visualizador-id");
        visualizador.setSesionstoken(tokenValido);

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(visualizador));

        // Act & Assert
        assertThrows(AccesoNoAutorizadoException.class, () -> {
            gestorContenidoService.listar(TOKEN_VALIDO, pageable, null, null);
        });

        verify(contenidoRepository, never()).findAllContenidosForGestor(any());
    }

    @Test
    void testListarConGestorBloqueado_LanzaExcepcion() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        gestorVideo.setBloqueado(true);

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));

        // Act & Assert
        assertThrows(AccesoNoAutorizadoException.class, () -> {
            gestorContenidoService.listar(TOKEN_VALIDO, pageable, null, null);
        });

        verify(contenidoRepository, never()).findAllContenidosForGestor(any());
    }

    // ==================== TESTS DE DETALLE ====================

    @Test
    void testDetalle_RetornaContenidoCompleto() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor(ID_VIDEO)).thenReturn(Optional.of(video));
        when(usuarioRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));

        // Act
        ContenidoDetalleDTO resultado = gestorContenidoService.detalle(ID_VIDEO, TOKEN_VALIDO);

        // Assert
        assertNotNull(resultado);
        assertEquals(ID_VIDEO, resultado.getId());
        assertEquals("Video Tutorial", resultado.getTitulo());
        assertEquals("Gestor Video", resultado.getCreadorNombre());
        verify(contenidoRepository).findByIdForGestor(ID_VIDEO);
        verify(logService).registrarAccion(
            eq("Consulta detalle contenido " + ID_VIDEO), 
            eq("gestorvideo@test.com")
        );
    }

    @Test
    void testDetalle_ContenidoNoEncontrado_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor("id-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> {
            gestorContenidoService.detalle("id-inexistente", TOKEN_VALIDO);
        });
    }

    @Test
    void testDetalle_CreadorEliminado_NoMuestraCreador() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor(ID_VIDEO)).thenReturn(Optional.of(video));
        when(usuarioRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.empty());

        // Act
        ContenidoDetalleDTO resultado = gestorContenidoService.detalle(ID_VIDEO, TOKEN_VALIDO);

        // Assert
        assertNotNull(resultado);
        assertNull(resultado.getCreadorNombre());
    }

    // ==================== TESTS DE ACTUALIZACIÓN ====================

    @Test
    void testActualizar_ActualizaCamposPermitidos() {
        // Arrange
        ContenidoUpdateDTO updateDTO = new ContenidoUpdateDTO();
        updateDTO.setTitulo("Video Actualizado");
        updateDTO.setDescripcion("Nueva descripción");
        updateDTO.setVip(true);
        updateDTO.setEstado(true);
        updateDTO.setEdadVisualizacion(13);
        Set<String> nuevosTags = new HashSet<>(Arrays.asList("java", "spring"));
        updateDTO.setTags(new ArrayList<>(nuevosTags));

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor(ID_VIDEO)).thenReturn(Optional.of(video));
        when(contenidoRepository.save(any(Video.class))).thenReturn(video);

        // Act
        ContenidoDetalleDTO resultado = gestorContenidoService.actualizar(
            ID_VIDEO, updateDTO, TOKEN_VALIDO
        );

        // Assert
        assertNotNull(resultado);
        verify(contenidoRepository).save(any(Video.class));
        verify(logService).registrarAccion(
            eq("Actualización de contenido " + ID_VIDEO), 
            eq("gestorvideo@test.com")
        );
    }

    @Test
    void testActualizar_ContenidoNoEncontrado_LanzaExcepcion() {
        // Arrange
        ContenidoUpdateDTO updateDTO = new ContenidoUpdateDTO();
        updateDTO.setTitulo("Video Actualizado");

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor("id-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> {
            gestorContenidoService.actualizar("id-inexistente", updateDTO, TOKEN_VALIDO);
        });

        verify(contenidoRepository, never()).save(any());
    }

    @Test
    void testActualizar_GestorVideoIntentaEditarAudio_LanzaExcepcion() {
        // Arrange
        ContenidoUpdateDTO updateDTO = new ContenidoUpdateDTO();
        updateDTO.setTitulo("Podcast Actualizado");

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor(ID_AUDIO)).thenReturn(Optional.of(audio));

        // Act & Assert
        assertThrows(AccesoNoAutorizadoException.class, () -> {
            gestorContenidoService.actualizar(ID_AUDIO, updateDTO, TOKEN_VALIDO);
        });

        verify(contenidoRepository, never()).save(any());
    }

    @Test
    void testActualizar_GestorAudioIntentaEditarVideo_LanzaExcepcion() {
        // Arrange
        ContenidoUpdateDTO updateDTO = new ContenidoUpdateDTO();
        updateDTO.setTitulo("Video Actualizado");

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorAudio));
        when(gestorRepository.findById(ID_GESTOR_AUDIO)).thenReturn(Optional.of(gestorAudio));
        when(contenidoRepository.findByIdForGestor(ID_VIDEO)).thenReturn(Optional.of(video));

        // Act & Assert
        assertThrows(AccesoNoAutorizadoException.class, () -> {
            gestorContenidoService.actualizar(ID_VIDEO, updateDTO, TOKEN_VALIDO);
        });

        verify(contenidoRepository, never()).save(any());
    }

    @Test
    void testActualizar_Video4KSinVip_LanzaExcepcion() {
        // Arrange
        video.setresolucion("4k");
        ContenidoUpdateDTO updateDTO = new ContenidoUpdateDTO();
        updateDTO.setTitulo("Video 4K");
        updateDTO.setDescripcion("Video en 4K");
        updateDTO.setVip(false); // No VIP
        updateDTO.setEstado(true);

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor(ID_VIDEO)).thenReturn(Optional.of(video));

        // Act & Assert
        assertThrows(PeticionInvalidaException.class, () -> {
            gestorContenidoService.actualizar(ID_VIDEO, updateDTO, TOKEN_VALIDO);
        });

        verify(contenidoRepository, never()).save(any());
    }

    @Test
    void testActualizar_CambiaEstadoANoVisible_ActualizaFecha() {
        // Arrange
        video.setestado(true); // Estado inicial visible
        ContenidoUpdateDTO updateDTO = new ContenidoUpdateDTO();
        updateDTO.setTitulo("Video Tutorial");
        updateDTO.setDescripcion("Tutorial de programación");
        updateDTO.setVip(false);
        updateDTO.setEstado(false); // Cambiar a no visible
        updateDTO.setEdadVisualizacion(0);
        updateDTO.setTags(new ArrayList<>(video.gettags()));

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor(ID_VIDEO)).thenReturn(Optional.of(video));
        when(contenidoRepository.save(any(Video.class))).thenAnswer(invocation -> {
            Video videoGuardado = invocation.getArgument(0);
            assertNotNull(videoGuardado.getfechaestadoautomatico());
            return videoGuardado;
        });

        // Act
        ContenidoDetalleDTO resultado = gestorContenidoService.actualizar(
            ID_VIDEO, updateDTO, TOKEN_VALIDO
        );

        // Assert
        assertNotNull(resultado);
        verify(contenidoRepository).save(any(Video.class));
    }

    // ==================== TESTS DE ELIMINACIÓN ====================

    @Test
    void testEliminar_EliminaContenidoCorrectamente() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor(ID_VIDEO)).thenReturn(Optional.of(video));
        doNothing().when(contenidoRepository).delete(video);

        // Act
        assertDoesNotThrow(() -> {
            gestorContenidoService.eliminar(ID_VIDEO, TOKEN_VALIDO);
        });

        // Assert
        verify(contenidoRepository).delete(video);
        verify(logService).registrarAccion(
            eq("Eliminación de contenido " + ID_VIDEO), 
            eq("gestorvideo@test.com")
        );
    }

    @Test
    void testEliminar_ContenidoNoEncontrado_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor("id-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> {
            gestorContenidoService.eliminar("id-inexistente", TOKEN_VALIDO);
        });

        verify(contenidoRepository, never()).delete(any());
    }

    @Test
    void testEliminar_GestorVideoIntentaEliminarAudio_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor(ID_AUDIO)).thenReturn(Optional.of(audio));

        // Act & Assert
        assertThrows(AccesoNoAutorizadoException.class, () -> {
            gestorContenidoService.eliminar(ID_AUDIO, TOKEN_VALIDO);
        });

        verify(contenidoRepository, never()).delete(any());
    }

    // ==================== TESTS DE OBTENER TAGS ====================

    // Test de obtenerTodosLosTags omitido debido a complejidad en mock de MongoTemplate.query() chain
    // El m\u00e9todo requiere mokear: mongoTemplate.query(Contenido.class).distinct("tags").as(String.class).all()
    // Se requiere testing de integraci\u00f3n para este caso

    @Test
    void testObtenerTodosLosTags_SinToken_LanzaExcepcion() {
        // Act & Assert
        assertThrows(PeticionInvalidaException.class, () -> {
            gestorContenidoService.obtenerTodosLosTags(null);
        });
    }

    // ==================== TESTS DE EXTRACCIÓN DE TOKEN ====================

    @Test
    void testExtraerToken_ConBearer_ExtraeCorrectamente() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        Page<Contenido> pageContenidos = new PageImpl<>(Arrays.asList(video));
        when(contenidoRepository.findAllContenidosForGestor(any(Pageable.class)))
            .thenReturn(pageContenidos);

        // Act
        assertDoesNotThrow(() -> {
            gestorContenidoService.listar(
                "Bearer " + TOKEN_SIN_BEARER, 
                PageRequest.of(0, 10), 
                null, 
                null
            );
        });

        // Assert
        verify(usuarioRepository).findBySesionToken(TOKEN_SIN_BEARER);
    }

    @Test
    void testExtraerToken_SinBearer_ExtraeCorrectamente() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        Page<Contenido> pageContenidos = new PageImpl<>(Arrays.asList(video));
        when(contenidoRepository.findAllContenidosForGestor(any(Pageable.class)))
            .thenReturn(pageContenidos);

        // Act
        assertDoesNotThrow(() -> {
            gestorContenidoService.listar(
                TOKEN_SIN_BEARER, 
                PageRequest.of(0, 10), 
                null, 
                null
            );
        });

        // Assert
        verify(usuarioRepository).findBySesionToken(TOKEN_SIN_BEARER);
    }

    @Test
    void testExtraerToken_TokenVacio_LanzaExcepcion() {
        // Act & Assert
        assertThrows(PeticionInvalidaException.class, () -> {
            gestorContenidoService.listar("", PageRequest.of(0, 10), null, null);
        });
    }

    @Test
    void testExtraerToken_TokenConEspacios_ExtraeCorrectamente() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        Page<Contenido> pageContenidos = new PageImpl<>(Arrays.asList(video));
        when(contenidoRepository.findAllContenidosForGestor(any(Pageable.class)))
            .thenReturn(pageContenidos);

        // Act
        assertDoesNotThrow(() -> {
            gestorContenidoService.listar(
                "  " + TOKEN_SIN_BEARER + "  ", 
                PageRequest.of(0, 10), 
                null, 
                null
            );
        });

        // Assert
        verify(usuarioRepository).findBySesionToken(TOKEN_SIN_BEARER);
    }

    // ==================== TESTS DE BÚSQUEDA POR TIPO ====================

    @Test
    void testBuscarVideos_RetornaSoloVideos() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contenido> pageVideos = new PageImpl<>(Arrays.asList(video));

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.searchAllVideosForGestor("tutorial", pageable))
            .thenReturn(pageVideos);

        // Act
        Page<ContenidoResumenDTO> resultado = gestorContenidoService.listar(
            TOKEN_VALIDO, pageable, "VIDEO", "tutorial"
        );

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(contenidoRepository).searchAllVideosForGestor("tutorial", pageable);
    }

    @Test
    void testBuscarAudios_RetornaSoloAudios() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contenido> pageAudios = new PageImpl<>(Arrays.asList(audio));

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorAudio));
        when(gestorRepository.findById(ID_GESTOR_AUDIO)).thenReturn(Optional.of(gestorAudio));
        when(contenidoRepository.searchAllAudiosForGestor("podcast", pageable))
            .thenReturn(pageAudios);

        // Act
        Page<ContenidoResumenDTO> resultado = gestorContenidoService.listar(
            TOKEN_VALIDO, pageable, "AUDIO", "podcast"
        );

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(contenidoRepository).searchAllAudiosForGestor("podcast", pageable);
    }

    @Test
    void testBuscarConTipoInvalido_RetornaTodos() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contenido> pageContenidos = new PageImpl<>(Arrays.asList(video, audio));

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.searchAllContenidosForGestor("test", pageable))
            .thenReturn(pageContenidos);

        // Act
        Page<ContenidoResumenDTO> resultado = gestorContenidoService.listar(
            TOKEN_VALIDO, pageable, "INVALIDO", "test"
        );

        // Assert
        assertNotNull(resultado);
        verify(contenidoRepository).searchAllContenidosForGestor("test", pageable);
    }

    // ==================== TESTS DE CONSTRUCCIÓN DE REFERENCIA ====================

    @Test
    void testConstruirReferencia_Video_RetornaUrl() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor(ID_VIDEO)).thenReturn(Optional.of(video));
        when(usuarioRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));

        // Act
        ContenidoDetalleDTO resultado = gestorContenidoService.detalle(ID_VIDEO, TOKEN_VALIDO);

        // Assert
        assertNotNull(resultado);
        assertEquals("http://example.com/video.mp4", resultado.getReferenciaReproduccion());
    }

    @Test
    void testConstruirReferencia_Audio_RetornaUrlLocalhost() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorAudio));
        when(gestorRepository.findById(ID_GESTOR_AUDIO)).thenReturn(Optional.of(gestorAudio));
        when(contenidoRepository.findByIdForGestor(ID_AUDIO)).thenReturn(Optional.of(audio));
        when(usuarioRepository.findById(ID_GESTOR_AUDIO)).thenReturn(Optional.of(gestorAudio));

        // Act
        ContenidoDetalleDTO resultado = gestorContenidoService.detalle(ID_AUDIO, TOKEN_VALIDO);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getReferenciaReproduccion().contains("localhost:8080/multimedia/audio/"));
        assertTrue(resultado.getReferenciaReproduccion().contains(ID_AUDIO));
    }

    // ==================== TESTS DE ACTUALIZACIÓN DE FECHA DISPONIBLE ====================

    @Test
    void testActualizar_ConFechaDisponibleHasta_ActualizaFecha() {
        // Arrange
        Date nuevaFecha = new Date();
        ContenidoUpdateDTO updateDTO = new ContenidoUpdateDTO();
        updateDTO.setTitulo("Video Tutorial");
        updateDTO.setDescripcion("Tutorial de programación");
        updateDTO.setVip(false);
        updateDTO.setEstado(true);
        updateDTO.setEdadVisualizacion(0);
        updateDTO.setFechaDisponibleHasta(nuevaFecha);
        updateDTO.setTags(new ArrayList<>(video.gettags()));

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor(ID_VIDEO)).thenReturn(Optional.of(video));
        when(contenidoRepository.save(any(Video.class))).thenAnswer(invocation -> {
            Video videoGuardado = invocation.getArgument(0);
            assertEquals(nuevaFecha, videoGuardado.getfechadisponiblehasta());
            return videoGuardado;
        });

        // Act
        ContenidoDetalleDTO resultado = gestorContenidoService.actualizar(
            ID_VIDEO, updateDTO, TOKEN_VALIDO
        );

        // Assert
        assertNotNull(resultado);
        verify(contenidoRepository).save(any(Video.class));
    }

    @Test
    void testActualizar_ConCaratulaActualizada_ActualizaCaratula() {
        // Arrange
        ContenidoUpdateDTO updateDTO = new ContenidoUpdateDTO();
        updateDTO.setTitulo("Video Tutorial");
        updateDTO.setDescripcion("Tutorial de programación");
        updateDTO.setVip(false);
        updateDTO.setEstado(true);
        updateDTO.setCaratula("http://example.com/nueva-caratula.jpg");
        updateDTO.setTags(new ArrayList<>(video.gettags()));

        when(usuarioRepository.findBySesionToken(TOKEN_SIN_BEARER)).thenReturn(Optional.of(gestorVideo));
        when(gestorRepository.findById(ID_GESTOR_VIDEO)).thenReturn(Optional.of(gestorVideo));
        when(contenidoRepository.findByIdForGestor(ID_VIDEO)).thenReturn(Optional.of(video));
        when(contenidoRepository.save(any(Video.class))).thenAnswer(invocation -> {
            Video videoGuardado = invocation.getArgument(0);
            assertEquals("http://example.com/nueva-caratula.jpg", videoGuardado.getcaratula());
            return videoGuardado;
        });

        // Act
        ContenidoDetalleDTO resultado = gestorContenidoService.actualizar(
            ID_VIDEO, updateDTO, TOKEN_VALIDO
        );

        // Assert
        assertNotNull(resultado);
        verify(contenidoRepository).save(any(Video.class));
    }
}
