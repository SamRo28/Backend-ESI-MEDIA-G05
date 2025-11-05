package iso25.g05.esi_media.dto;

import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VisualizadorGestionDTOTest {

    @Test
    void testConstructorAndGetters() {
        Date nac = new Date(631152000000L); // 1990-01-01
        VisualizadorGestionDTO dto = new VisualizadorGestionDTO("id1","Juan","Perez","juan@example.com", "foto.png", false, new Date(), "alias", nac, true);

        assertEquals("id1", dto.getId());
        assertEquals("Juan", dto.getNombre());
        assertEquals("Perez", dto.getApellidos());
        assertEquals("juan@example.com", dto.getEmail());
        assertEquals("foto.png", dto.getFoto());
        assertFalse(dto.isBloqueado());
        assertEquals("alias", dto.getAlias());
        assertEquals(nac, dto.getFechanac());
        assertTrue(dto.isVip());
    }
}
