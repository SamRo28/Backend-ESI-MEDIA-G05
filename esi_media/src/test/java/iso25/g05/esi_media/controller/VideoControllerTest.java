package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.dto.VideoUploadDTO;
import iso25.g05.esi_media.model.Video;
import iso25.g05.esi_media.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoControllerTest {

    @Mock
    private VideoService videoService;

    @InjectMocks
    private VideoController videoController;

    private VideoUploadDTO videoDTO;
    private Video videoMock;

    @BeforeEach
    void setUp() {
        videoDTO = new VideoUploadDTO();
        videoDTO.setTitulo("Video Test");
        videoDTO.setDescripcion("Descripción test");
        videoDTO.setUrl("https://example.com/video.mp4");
        videoDTO.setResolucion("1080p");

        videoMock = new Video();
        videoMock.setId("video123");
        videoMock.settitulo("Video Test");
        videoMock.seturl("https://example.com/video.mp4");
    }

    @Test
    void testSubirVideoExitoso() {
        // Arrange
        when(videoService.subirVideoConToken(any(VideoUploadDTO.class), anyString()))
            .thenReturn(videoMock);

        // Act
        ResponseEntity<Map<String, Object>> response = videoController.subirVideo(videoDTO, "Bearer valid-token");

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("success"));
        assertEquals("Video subido exitosamente", response.getBody().get("message"));
        assertEquals("video123", response.getBody().get("videoId"));
        assertEquals("Video Test", response.getBody().get("titulo"));
        assertEquals("https://example.com/video.mp4", response.getBody().get("url"));

        verify(videoService, times(1)).subirVideoConToken(any(VideoUploadDTO.class), anyString());
    }

    @Test
    void testSubirVideoErrorValidacion() {
        // Arrange
        when(videoService.subirVideoConToken(any(VideoUploadDTO.class), anyString()))
            .thenThrow(new IllegalArgumentException("URL inválida"));

        // Act
        ResponseEntity<Map<String, Object>> response = videoController.subirVideo(videoDTO, "Bearer valid-token");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Error de validación: URL inválida", response.getBody().get("message"));
    }

    @Test
    void testSubirVideoErrorInterno() {
        // Arrange
        when(videoService.subirVideoConToken(any(VideoUploadDTO.class), anyString()))
            .thenThrow(new RuntimeException("Error inesperado"));

        // Act
        ResponseEntity<Map<String, Object>> response = videoController.subirVideo(videoDTO, "Bearer valid-token");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Error interno del servidor", response.getBody().get("message"));
    }

    @Test
    void testEstado() {
        // Act
        ResponseEntity<Map<String, String>> response = videoController.estado();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("VideoController", response.getBody().get("service"));
    }
}
