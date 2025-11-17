package iso25.g05.esi_media.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tests del modelo Valoracion")
class ValoracionTest {

    @Test
    @DisplayName("ValoracionModeloInicialNullTest: nuevo objeto tiene valoracionFinal nulo")
    void ValoracionModeloInicialNullTest() {
        Valoracion v = new Valoracion();
        assertNull(v.getValoracionFinal(), "La valoración inicial debe ser null (visto pero no valorado)");
    }

    @Test
    @DisplayName("ValoracionGetSetTest: getters y setters funcionan")
    void ValoracionGetSetTest() {
        Valoracion v = new Valoracion();
        v.setId("vid");
        v.setVisualizadorId("U1");
        v.setContenidoId("C1");
        v.setValoracionFinal(4.5);

        assertEquals("vid", v.getId());
        assertEquals("U1", v.getVisualizadorId());
        assertEquals("C1", v.getContenidoId());
        assertEquals(4.5, v.getValoracionFinal());
    }

    @Test
    @DisplayName("ValoracionPrecisionTest: acepta valores con paso de 0.5 y mantiene precisión")
    void ValoracionPrecisionTest() {
        Valoracion v = new Valoracion();
        v.setValoracionFinal(3.5);
        // Comprobación directa del Double almacenado
        assertEquals(Double.valueOf(3.5), v.getValoracionFinal());
    }
}
