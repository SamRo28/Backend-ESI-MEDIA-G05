package iso25.g05.esi_media.controller;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import iso25.g05.esi_media.dto.AudioUploadDTO;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.service.AudioService;

@ExtendWith(MockitoExtension.class)
class AudioControllerTest {

    @Mock
    private AudioService audioService;

    @Mock
    private MultipartFile archivoMock;

    @InjectMocks
    private AudioController audioController;

    private AudioUploadDTO audioDTO;
    private Audio audioMock;

    @BeforeEach
    void setUp() {
        audioDTO = new AudioUploadDTO();
        audioDTO.setTitulo("Audio Test");
        audioDTO.setDescripcion("Descripción test");
        audioDTO.setArchivo(archivoMock);

        audioMock = new Audio();
        audioMock.setId("audio123");
        audioMock.settitulo("Audio Test");
    }

    @Test
    void testSubirAudioExitoso() throws IOException {
        // Arrange
        when(audioService.subirAudioConToken(any(AudioUploadDTO.class), anyString()))
            .thenReturn(audioMock);

        // Act
        ResponseEntity<Map<String, Object>> response = audioController.subirAudio(audioDTO, "Bearer valid-token");

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals("Audio subido exitosamente", body.get("message"));
        assertEquals("audio123", body.get("audioId"));
        assertEquals(true, body.get("success"));

        verify(audioService, times(1)).subirAudioConToken(any(AudioUploadDTO.class), anyString());
    }

    @Test
    void testSubirAudioErrorValidacion() throws IOException {
        // Arrange
        when(audioService.subirAudioConToken(any(AudioUploadDTO.class), anyString()))
            .thenThrow(new IllegalArgumentException("El archivo está vacío."));

        // Act
        ResponseEntity<Map<String, Object>> response = audioController.subirAudio(audioDTO, "Bearer valid-token");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals(false, body.get("success"));
        assertTrue(body.get("message").toString().contains("El archivo está vacío."));
    }

    @Test
    void testSubirAudioErrorIO() throws IOException {
        // Arrange
        when(audioService.subirAudioConToken(any(AudioUploadDTO.class), anyString()))
            .thenThrow(new IOException("Error al procesar archivo"));

        // Act
        ResponseEntity<Map<String, Object>> response = audioController.subirAudio(audioDTO, "Bearer valid-token");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals(false, body.get("success"));
        assertTrue(body.get("message").toString().contains("Error procesando el archivo"));
    }

    @Test
    void testEstado() {
        // Act
        ResponseEntity<Map<String, String>> response = audioController.estado();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, String> body = response.getBody();
        assertEquals("UP", body.get("status"));
        assertEquals("AudioController", body.get("service"));
    }
}
