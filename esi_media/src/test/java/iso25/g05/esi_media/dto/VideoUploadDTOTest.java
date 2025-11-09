package iso25.g05.esi_media.dto;

import org.junit.jupiter.api.Test;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class VideoUploadDTOTest {

    @Test
    void testGettersAndSetters() {
        VideoUploadDTO dto = new VideoUploadDTO();
        dto.setTitulo("Video");
        dto.setDescripcion("Desc");
        dto.setTags(Arrays.asList("a","b"));
        dto.setDuracion(60.0);
        dto.setVip(Boolean.FALSE);
        dto.setEdadVisualizacion(12);
        dto.setVisible(Boolean.TRUE);
        dto.setUrl("https://example.com/v.mp4");
        dto.setResolucion("1080p");

        assertEquals("Video", dto.getTitulo());
        assertEquals("Desc", dto.getDescripcion());
        assertEquals(2, dto.getTags().size());
        assertEquals(60.0, dto.getDuracion());
        assertFalse(dto.getVip());
        assertEquals(12, dto.getEdadVisualizacion());
        assertTrue(dto.getVisible());
        assertEquals("https://example.com/v.mp4", dto.getUrl());
        assertEquals("1080p", dto.getResolucion());
    }
}
