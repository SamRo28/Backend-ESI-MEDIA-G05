package iso25.g05.esi_media.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import iso25.g05.esi_media.dto.AudioUploadDTO;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.AudioRepository;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AudioServiceTest {

    @Mock
    private AudioRepository audioRepository;

    @Mock
    private GestorDeContenidoRepository gestorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VideoService videoService;

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
        usuarioMock.setSesionstoken(tokenMock);

        tokenMock = new Token();
        tokenMock.setToken("valid-token");
        tokenMock.setFechaExpiracion(new Date(System.currentTimeMillis() + 3600000));

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
        
        // Mock magic bytes de MP3 (ID3 tag)
        byte[] mp3Bytes = {0x49, 0x44, 0x33, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        when(archivoMock.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(mp3Bytes));
        
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
        when(archivoMock.getOriginalFilename()).thenReturn("test.mp3");
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
        when(videoService.validarTokenYObtenerGestorId("valid-token")).thenReturn("gestor123");
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(archivoMock.isEmpty()).thenReturn(false);
        when(archivoMock.getSize()).thenReturn(1024L);
        when(archivoMock.getContentType()).thenReturn("audio/mpeg");
        when(archivoMock.getOriginalFilename()).thenReturn("test.mp3");
        when(archivoMock.getBytes()).thenReturn(new byte[]{1, 2, 3});
        
        // Mock magic bytes de MP3 (ID3 tag)
        byte[] mp3Bytes = {0x49, 0x44, 0x33, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        when(archivoMock.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(mp3Bytes));
        
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
        // Arrange
        when(videoService.validarTokenYObtenerGestorId("")).thenThrow(new IllegalArgumentException("Token vacío"));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            audioService.subirAudioConToken(audioDTO, "");
        });
    }

    @Test
    void testSubirAudioConTokenInvalido() {
        // Arrange
        when(videoService.validarTokenYObtenerGestorId("invalid-token")).thenThrow(new IllegalArgumentException("Token inválido"));

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

    @Test
    void testValidacionesDeSeguridad() throws IOException {
        // Arrange
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(audioRepository.save(any(Audio.class))).thenReturn(audioMock);
        when(archivoMock.getBytes()).thenReturn(new byte[1024]);
        when(archivoMock.getContentType()).thenReturn("audio/mpeg");
        when(archivoMock.getSize()).thenReturn(1024L);
        when(archivoMock.getOriginalFilename()).thenReturn("test.mp3");
        
        // Mock magic bytes de MP3 (ID3 tag)
        byte[] mp3Bytes = {0x49, 0x44, 0x33, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        when(archivoMock.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(mp3Bytes));

        // Act - Debe ejecutarse sin lanzar excepción
        assertDoesNotThrow(() -> audioService.subirAudio(audioDTO, "gestor123"));

        // Assert - Verificar que se guardó el audio
        verify(audioRepository, times(1)).save(any(Audio.class));
    }

    @Test
    void testValidacionesDeSeguridad_MagicBytesInvalidos() throws IOException {
        // Arrange
        when(gestorRepository.findById("gestor123")).thenReturn(Optional.of(gestorMock));
        when(archivoMock.getContentType()).thenReturn("audio/mpeg");
        when(archivoMock.getSize()).thenReturn(1024L);
        when(archivoMock.getOriginalFilename()).thenReturn("test.mp3");
        
        // Mock magic bytes de WAV (formato no válido)
        byte[] wavBytes = {0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00, 0x57, 0x41, 0x56, 0x45};
        when(archivoMock.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(wavBytes));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> audioService.subirAudio(audioDTO, "gestor123")
        );
        
        assertTrue(exception.getMessage().contains("Se requiere un archivo MP3"));
        assertTrue(exception.getMessage().contains("wav"));
        
        // Verificar que NO se guardó el audio
        verify(audioRepository, never()).save(any(Audio.class));
    }
}
