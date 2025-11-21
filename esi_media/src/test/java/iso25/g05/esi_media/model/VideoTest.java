package iso25.g05.esi_media.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VideoTest {

    private Video video;
    private List<String> tags;
    private Date fechaEstadoAutomatico;
    private Date fechaDisponibleHasta;

    @BeforeEach
    void setUp() {
        tags = Arrays.asList("acción", "aventura");
        fechaEstadoAutomatico = new Date();
        fechaDisponibleHasta = new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000));
        
        video = new Video(
            "video123",
            "Título del Video",
            "Descripción del video",
            tags,
            120.5,
            true,
            true,
            fechaEstadoAutomatico,
            fechaDisponibleHasta,
            18,
            "caratula.jpg",
            1000,
            "https://example.com/video.mp4",
            "1080p",
            "gestor456"
        );
    }

    @Test
    void testConstructorCompleto() {
        assertNotNull(video);
        assertEquals("video123", video.getId());
        assertEquals("Título del Video", video.gettitulo());
        assertEquals("https://example.com/video.mp4", video.geturl());
        assertEquals("1080p", video.getresolucion());
        assertEquals("gestor456", video.getgestorId());
    }

    @Test
    void testConstructorVacio() {
        Video videoVacio = new Video();
        assertNotNull(videoVacio);
    }

    @Test
    void testUrlSetterGetter() {
        video.seturl("https://example.com/nuevo-video.mp4");
        assertEquals("https://example.com/nuevo-video.mp4", video.geturl());
    }

    @Test
    void testResolucionSetterGetter() {
        video.setresolucion("4K");
        assertEquals("4K", video.getresolucion());
        
        video.setresolucion("720p");
        assertEquals("720p", video.getresolucion());
    }

    @Test
    void testHeredaDeContenido() {
        assertTrue(video instanceof Contenido);
        
        video.settitulo("Nuevo Título");
        assertEquals("Nuevo Título", video.gettitulo());
        
        video.setnvisualizaciones(5000);
        assertEquals(5000, video.getnvisualizaciones());
    }

    @Test
    void testResolucionesVariadas() {
        String[] resoluciones = {"480p", "720p", "1080p", "4K", "8K"};
        
        for (String resolucion : resoluciones) {
            video.setresolucion(resolucion);
            assertEquals(resolucion, video.getresolucion());
        }
    }

    @Test
    void testUrlsVariadas() {
        video.seturl("https://cdn1.example.com/video.mp4");
        assertEquals("https://cdn1.example.com/video.mp4", video.geturl());
        
        video.seturl("https://cdn2.example.com/video.webm");
        assertEquals("https://cdn2.example.com/video.webm", video.geturl());
    }

    @Test
    void testPropiedadesContenidoEnVideo() {
        video.setestado(false);
        assertFalse(video.isestado());
        
        video.setvip(false);
        assertFalse(video.isvip());
        
        video.setduracion(150.0);
        assertEquals(150.0, video.getduracion());
    }
}
