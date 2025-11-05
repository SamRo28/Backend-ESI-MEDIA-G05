package iso25.g05.esi_media.service;

import iso25.g05.esi_media.dto.AudioUploadDTO;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.AudioRepository;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioServiceTest {

    @Mock
    private AudioRepository audioRepository;

    @Mock
    private GestorDeContenidoRepository gestorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MultipartFile archivoMock;

    @InjectMocks
    private AudioService audioService;

    private GestordeContenido gestorMock;
    private AudioUploadDTO audioDTO;
    private Audio audioMock;
    private Usuario usuarioMock;
    private Token tokenMock;

    @BeforeEach
    void setUp() {
        gestorMock = new GestordeContenido();
        gestorMock.setId("gestor123");
        gestorMock.settipocontenidovideooaudio("audio");
        gestorMock.setContenidos_subidos(new ArrayList<>());

        audioDTO = new AudioUploadDTO();
        audioDTO.setTitulo("Audio Test");
        audioDTO.setDescripcion("Descripción test");
        audioDTO.setTags(new ArrayList<>());
        audioDTO.setDuracion(180.0);
        audioDTO.setVip(false);
        audioDTO.setVisible(true);
        audioDTO.setEdadVisualizacion(0);
        audioDTO.setArchivo(archivoMock);

        audioMock = new Audio();
        audioMock.setId("audio123");
        audioMock.settitulo("Audio Test");

        usuarioMock = new Usuario();
        usuarioMock.setId("gestor123");
        usuarioMock.setSesionstoken(new ArrayList<>());

        tokenMock = new Token();
        tokenMock.setToken("valid-token");
        tokenMock.setFechaExpiracion(new Date(System.currentTimeMillis() + 3600000));
        usuarioMock.getSesionstoken().add(tokenMock);
    }

    @Test
    void testSubirAudioExitoso() throws IOException {
        // Arrange
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(archivoMock.isEmpty()).thenReturn(false);
        when(archivoMock.getSize()).thenReturn(1024L);
        when(archivoMock.getContentType()).thenReturn("audio/mpeg");
        when(archivoMock.getOriginalFilename()).thenReturn("test.mp3");
        when(archivoMock.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(audioRepository.save(any(Audio.class))).thenReturn(audioMock);
        when(gestorRepository.save(any(GestordeContenido.class))).thenReturn(gestorMock);

        // Act
        Audio resultado = audioService.subirAudio(audioDTO, "gestor123");

        // Assert
        assertNotNull(resultado);
        assertEquals("audio123", resultado.getId());
        verify(audioRepository, times(1)).save(any(Audio.class));
        verify(gestorRepository, times(1)).save(any(GestordeContenido.class));
    }

    @Test
    void testSubirAudioGestorNoEncontrado() {
        // Arrange
        when(gestorRepository.findById("noexiste")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            audioService.subirAudio(audioDTO, "noexiste");
        });
    }

    @Test
    void testSubirAudioGestorNoAutorizado() {
        // Arrange
        gestorMock.settipocontenidovideooaudio("video");
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            audioService.subirAudio(audioDTO, "gestor123");
        });
        assertTrue(exception.getMessage().contains("no está autorizado"));
    }

    @Test
    void testSubirAudioArchivoVacio() {
        // Arrange
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(archivoMock.isEmpty()).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            audioService.subirAudio(audioDTO, "gestor123");
        });
    }

    @Test
    void testSubirAudioArchivoMuyGrande() {
        // Arrange
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(archivoMock.isEmpty()).thenReturn(false);
        when(archivoMock.getSize()).thenReturn(3 * 1024 * 1024L); // 3MB

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            audioService.subirAudio(audioDTO, "gestor123");
        });
        assertTrue(exception.getMessage().contains("excede el tamaño máximo"));
    }

    @Test
    void testSubirAudioMimeTypeInvalido() {
        // Arrange
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(archivoMock.isEmpty()).thenReturn(false);
        when(archivoMock.getSize()).thenReturn(1024L);
        when(archivoMock.getContentType()).thenReturn("video/mp4");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            audioService.subirAudio(audioDTO, "gestor123");
        });
    }

    @Test
    void testSubirAudioExtensionInvalida() {
        // Arrange
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(archivoMock.isEmpty()).thenReturn(false);
        when(archivoMock.getSize()).thenReturn(1024L);
        when(archivoMock.getContentType()).thenReturn("audio/mpeg");
        when(archivoMock.getOriginalFilename()).thenReturn("test.wav");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            audioService.subirAudio(audioDTO, "gestor123");
        });
    }

    @Test
    void testSubirAudioConTokenExitoso() throws IOException {
        // Arrange
        when(usuarioRepository.findBySesionToken("valid-token")).thenReturn(Optional.of(usuarioMock));
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(archivoMock.isEmpty()).thenReturn(false);
        when(archivoMock.getSize()).thenReturn(1024L);
        when(archivoMock.getContentType()).thenReturn("audio/mpeg");
        when(archivoMock.getOriginalFilename()).thenReturn("test.mp3");
        when(archivoMock.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(audioRepository.save(any(Audio.class))).thenReturn(audioMock);
        when(gestorRepository.save(any(GestordeContenido.class))).thenReturn(gestorMock);

        // Act
        Audio resultado = audioService.subirAudioConToken(audioDTO, "valid-token");

        // Assert
        assertNotNull(resultado);
        verify(audioRepository, times(1)).save(any(Audio.class));
    }

    @Test
    void testSubirAudioConTokenVacio() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            audioService.subirAudioConToken(audioDTO, "");
        });
    }

    @Test
    void testSubirAudioConTokenInvalido() {
        // Arrange
        when(usuarioRepository.findBySesionToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            audioService.subirAudioConToken(audioDTO, "invalid-token");
        });
    }

    @Test
    void testObtenerAudiosPorGestorExitoso() {
        // Arrange
        gestorMock.getContenidos_subidos().add("audio123");
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(audioRepository.findAllById(anyList())).thenReturn(new ArrayList<>());

        // Act
        Iterable<Audio> resultado = audioService.obtenerAudiosPorGestor("gestor123");

        // Assert
        assertNotNull(resultado);
        verify(audioRepository, times(1)).findAllById(anyList());
    }

    @Test
    void testObtenerAudiosPorGestorNoEncontrado() {
        // Arrange
        when(gestorRepository.findById("noexiste")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            audioService.obtenerAudiosPorGestor("noexiste");
        });
    }
}
