package iso25.g05.esi_media.dto;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AudioUploadDTOTest {

    @Test
    void testGettersAndSetters() {
        AudioUploadDTO dto = new AudioUploadDTO();
        dto.setTitulo("Título");
        dto.setDescripcion("Desc");
        dto.setDuracion(120.5);
        dto.setVip(Boolean.TRUE);
        dto.setEdadVisualizacion(0);
        dto.setVisible(Boolean.TRUE);
        MultipartFile archivo = mock(MultipartFile.class);
        dto.setArchivo(archivo);

        assertEquals("Título", dto.getTitulo());
        assertEquals("Desc", dto.getDescripcion());
        assertEquals(120.5, dto.getDuracion());
        assertTrue(dto.getVip());
        assertEquals(0, dto.getEdadVisualizacion());
        assertTrue(dto.getVisible());
        assertEquals(archivo, dto.getArchivo());
    }
}
