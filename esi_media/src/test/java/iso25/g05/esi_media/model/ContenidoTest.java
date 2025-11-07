package iso25.g05.esi_media.model;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContenidoTest {

    private Contenido contenido;
    private List<String> tags;
    private Date fechaEstadoAutomatico;
    private Date fechaDisponibleHasta;

    @BeforeEach
    void setUp() {
        tags = Arrays.asList("acción", "aventura", "ciencia ficción");
        fechaEstadoAutomatico = new Date();
        fechaDisponibleHasta = new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000));
        
        contenido = new Contenido(
            "contenido123",
            "Título de Prueba",
            "Descripción del contenido",
            tags,
            120.5,
            true,
            true,
            fechaEstadoAutomatico,
            fechaDisponibleHasta,
            18,
            "caratula.jpg",
            1000,
            "gestor456"
        );
    }

    @Test
    void testConstructorCompleto() {
        assertNotNull(contenido);
        assertEquals("contenido123", contenido.getId());
        assertEquals("Título de Prueba", contenido.gettitulo());
        assertEquals("Descripción del contenido", contenido.getdescripcion());
        assertEquals(tags, contenido.gettags());
        assertEquals(120.5, contenido.getduracion());
        assertTrue(contenido.isvip());
        assertTrue(contenido.isestado());
        assertEquals(fechaEstadoAutomatico, contenido.getfechaestadoautomatico());
        assertEquals(fechaDisponibleHasta, contenido.getfechadisponiblehasta());
        assertEquals(18, contenido.getedadvisualizacion());
        assertEquals("caratula.jpg", contenido.getcaratula());
        assertEquals(1000, contenido.getnvisualizaciones());
        assertEquals("gestor456", contenido.getgestorId());
    }

    @Test
    void testConstructorVacio() {
        Contenido contenidoVacio = new Contenido();
        assertNotNull(contenidoVacio);
    }

    @Test
    void testSettersYGettersBasicos() {
        contenido.setId("nuevo123");
        assertEquals("nuevo123", contenido.getId());
        
        contenido.settitulo("Nuevo Título");
        assertEquals("Nuevo Título", contenido.gettitulo());
        
        contenido.setdescripcion("Nueva descripción");
        assertEquals("Nueva descripción", contenido.getdescripcion());
    }

    @Test
    void testDuracion() {
        contenido.setduracion(90.0);
        assertEquals(90.0, contenido.getduracion());
        
        contenido.setduracion(150.75);
        assertEquals(150.75, contenido.getduracion());
    }

    @Test
    void testEstadoVip() {
        contenido.setvip(false);
        assertFalse(contenido.isvip());
        
        contenido.setvip(true);
        assertTrue(contenido.isvip());
    }

    @Test
    void testEstadoVisibilidad() {
        contenido.setestado(false);
        assertFalse(contenido.isestado());
        
        contenido.setestado(true);
        assertTrue(contenido.isestado());
    }

    @Test
    void testTags() {
        List<String> nuevosTags = Arrays.asList("drama", "suspense");
        contenido.settags(nuevosTags);
        
        assertEquals(2, contenido.gettags().size());
        assertTrue(contenido.gettags().contains("drama"));
        assertTrue(contenido.gettags().contains("suspense"));
    }

    @Test
    void testFechas() {
        Date nuevaFechaEstado = new Date();
        contenido.setfechaestadoautomatico(nuevaFechaEstado);
        assertEquals(nuevaFechaEstado, contenido.getfechaestadoautomatico());
        
        Date nuevaFechaDisponible = new Date(System.currentTimeMillis() + (60L * 24 * 60 * 60 * 1000));
        contenido.setfechadisponiblehasta(nuevaFechaDisponible);
        assertEquals(nuevaFechaDisponible, contenido.getfechadisponiblehasta());
    }

    @Test
    void testEdadVisualizacion() {
        contenido.setedadvisualizacion(13);
        assertEquals(13, contenido.getedadvisualizacion());
        
        contenido.setedadvisualizacion(0);
        assertEquals(0, contenido.getedadvisualizacion());
        
        contenido.setedadvisualizacion(21);
        assertEquals(21, contenido.getedadvisualizacion());
    }

    @Test
    void testCaratula() {
        contenido.setcaratula("nueva_caratula.png");
        assertEquals("nueva_caratula.png", contenido.getcaratula());
    }

    @Test
    void testNumeroVisualizaciones() {
        contenido.setnvisualizaciones(0);
        assertEquals(0, contenido.getnvisualizaciones());
        
        contenido.setnvisualizaciones(5000);
        assertEquals(5000, contenido.getnvisualizaciones());
    }

    @Test
    void testGestorId() {
        contenido.setgestorId("nuevoGestor789");
        assertEquals("nuevoGestor789", contenido.getgestorId());
    }

    @Test
    void testContenidoNoVipYNoVisible() {
        contenido.setvip(false);
        contenido.setestado(false);
        
        assertFalse(contenido.isvip());
        assertFalse(contenido.isestado());
    }
}
