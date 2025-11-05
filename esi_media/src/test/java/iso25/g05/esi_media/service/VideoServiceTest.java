package iso25.g05.esi_media.service;

import iso25.g05.esi_media.dto.VideoUploadDTO;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Video;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private GestorDeContenidoRepository gestorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VideoService videoService;

    private GestordeContenido gestorMock;
    private VideoUploadDTO videoDTO;
    private Video videoMock;
    private Usuario usuarioMock;
    private Token tokenMock;

    @BeforeEach
    void setUp() {
        gestorMock = new GestordeContenido();
        gestorMock.setId("gestor123");
        gestorMock.settipocontenidovideooaudio("video");
        gestorMock.setContenidos_subidos(new ArrayList<>());

        videoDTO = new VideoUploadDTO();
        videoDTO.setTitulo("Video Test");
        videoDTO.setDescripcion("Descripción test");
        videoDTO.setTags(new ArrayList<>());
        videoDTO.setDuracion(120.0);
        videoDTO.setVip(false);
        videoDTO.setVisible(true);
        videoDTO.setEdadVisualizacion(0);
        videoDTO.setUrl("https://example.com/video.mp4");
        videoDTO.setResolucion("1080p");

        videoMock = new Video();
        videoMock.setId("video123");
        videoMock.settitulo("Video Test");

        usuarioMock = new Usuario();
        usuarioMock.setId("gestor123");
        usuarioMock.setSesionstoken(new ArrayList<>());

        tokenMock = new Token();
        tokenMock.setToken("valid-token");
        tokenMock.setFechaExpiracion(new Date(System.currentTimeMillis() + 3600000));
        usuarioMock.getSesionstoken().add(tokenMock);
    }

    @Test
    void testSubirVideoExitoso() {
        // Arrange
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(videoRepository.save(any(Video.class))).thenReturn(videoMock);
        when(gestorRepository.save(any(GestordeContenido.class))).thenReturn(gestorMock);

        // Act
        Video resultado = videoService.subirVideo(videoDTO, "gestor123");

        // Assert
        assertNotNull(resultado);
        assertEquals("video123", resultado.getId());
        verify(videoRepository, times(1)).save(any(Video.class));
        verify(gestorRepository, times(1)).save(any(GestordeContenido.class));
    }

    @Test
    void testSubirVideoGestorNoEncontrado() {
        // Arrange
        when(gestorRepository.findById("noexiste")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            videoService.subirVideo(videoDTO, "noexiste");
        });
    }

    @Test
    void testSubirVideoGestorNoAutorizado() {
        // Arrange
        gestorMock.settipocontenidovideooaudio("audio");
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            videoService.subirVideo(videoDTO, "gestor123");
        });
        assertTrue(exception.getMessage().contains("no está autorizado"));
    }

    @Test
    void testSubirVideoUrlVacia() {
        // Arrange
        videoDTO.setUrl("");
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            videoService.subirVideo(videoDTO, "gestor123");
        });
    }

    @Test
    void testSubirVideoUrlNull() {
        // Arrange
        videoDTO.setUrl(null);
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            videoService.subirVideo(videoDTO, "gestor123");
        });
    }

    @Test
    void testSubirVideoUrlMalformada() {
        // Arrange
        videoDTO.setUrl("no-es-una-url-valida");
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            videoService.subirVideo(videoDTO, "gestor123");
        });
    }

    @Test
    void testSubirVideoProtocoloInvalido() {
        // Arrange
        videoDTO.setUrl("ftp://example.com/video.mp4");
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            videoService.subirVideo(videoDTO, "gestor123");
        });
    }

    @Test
    void testSubirVideoUrlHTTPS() {
        // Arrange
        videoDTO.setUrl("https://example.com/video.mp4");
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(videoRepository.save(any(Video.class))).thenReturn(videoMock);
        when(gestorRepository.save(any(GestordeContenido.class))).thenReturn(gestorMock);

        // Act
        Video resultado = videoService.subirVideo(videoDTO, "gestor123");

        // Assert
        assertNotNull(resultado);
    }

    @Test
    void testSubirVideoConTokenExitoso() {
        // Arrange
        when(usuarioRepository.findBySesionToken("valid-token")).thenReturn(Optional.of(usuarioMock));
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(videoRepository.save(any(Video.class))).thenReturn(videoMock);
        when(gestorRepository.save(any(GestordeContenido.class))).thenReturn(gestorMock);

        // Act
        Video resultado = videoService.subirVideoConToken(videoDTO, "valid-token");

        // Assert
        assertNotNull(resultado);
        verify(videoRepository, times(1)).save(any(Video.class));
    }

    @Test
    void testSubirVideoConTokenVacio() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            videoService.subirVideoConToken(videoDTO, "");
        });
    }

    @Test
    void testSubirVideoConTokenInvalido() {
        // Arrange
        when(usuarioRepository.findBySesionToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            videoService.subirVideoConToken(videoDTO, "invalid-token");
        });
    }

    @Test
    void testSubirVideoConTokenExpirado() {
        // Arrange
        tokenMock.setFechaExpiracion(new Date(System.currentTimeMillis() - 3600000));
        when(usuarioRepository.findBySesionToken("valid-token")).thenReturn(Optional.of(usuarioMock));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            videoService.subirVideoConToken(videoDTO, "valid-token");
        });
    }

    @Test
    void testObtenerVideosPorGestorExitoso() {
        // Arrange
        gestorMock.getContenidos_subidos().add("video123");
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(videoRepository.findAllById(anyList())).thenReturn(new ArrayList<>());

        // Act
        Iterable<Video> resultado = videoService.obtenerVideosPorGestor("gestor123");

        // Assert
        assertNotNull(resultado);
        verify(videoRepository, times(1)).findAllById(anyList());
    }

    @Test
    void testObtenerVideosPorGestorNoEncontrado() {
        // Arrange
        when(gestorRepository.findById("noexiste")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            videoService.obtenerVideosPorGestor("noexiste");
        });
    }
}
