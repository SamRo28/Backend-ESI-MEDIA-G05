package iso25.g05.esi_media.model;

import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AudioTest {

    private Audio audio;
    private List<String> tags;
    private Date fechaEstadoAutomatico;
    private Date fechaDisponibleHasta;
    private Binary fichero;

    @BeforeEach
    void setUp() {
        tags = Arrays.asList("música", "rock");
        fechaEstadoAutomatico = new Date();
        fechaDisponibleHasta = new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000));
        fichero = new Binary(new byte[]{1, 2, 3, 4, 5});
        
        audio = new Audio(
            "audio123",
            "Título del Audio",
            "Descripción del audio",
            tags,
            180.5,
            true,
            true,
            fechaEstadoAutomatico,
            fechaDisponibleHasta,
            0,
            "caratula.jpg",
            500,
            fichero,
            "audio/mpeg",
            1024000L,
            "gestor789"
        );
    }

    @Test
    void testConstructorCompleto() {
        assertNotNull(audio);
        assertEquals("audio123", audio.getId());
        assertEquals("Título del Audio", audio.gettitulo());
        assertEquals(fichero, audio.getfichero());
        assertEquals("audio/mpeg", audio.getmimeType());
        assertEquals(1024000L, audio.gettamanoBytes());
        assertEquals("gestor789", audio.getgestorId());
    }

    @Test
    void testConstructorVacio() {
        Audio audioVacio = new Audio();
        assertNotNull(audioVacio);
    }

    @Test
    void testFicheroSetterGetter() {
        Binary nuevoFichero = new Binary(new byte[]{10, 20, 30});
        audio.setfichero(nuevoFichero);
        assertEquals(nuevoFichero, audio.getfichero());
    }

    @Test
    void testMimeTypeSetterGetter() {
        audio.setmimeType("audio/wav");
        assertEquals("audio/wav", audio.getmimeType());
        
        audio.setmimeType("audio/ogg");
        assertEquals("audio/ogg", audio.getmimeType());
    }

    @Test
    void testTamanoBytesSetterGetter() {
        audio.settamanoBytes(2048000L);
        assertEquals(2048000L, audio.gettamanoBytes());
        
        audio.settamanoBytes(512000L);
        assertEquals(512000L, audio.gettamanoBytes());
    }

    @Test
    void testHeredaDeContenido() {
        assertTrue(audio instanceof Contenido);
        
        audio.settitulo("Nuevo Título de Audio");
        assertEquals("Nuevo Título de Audio", audio.gettitulo());
        
        audio.setnvisualizaciones(1000);
        assertEquals(1000, audio.getnvisualizaciones());
    }

    @Test
    void testMimeTypesVariados() {
        String[] mimeTypes = {"audio/mpeg", "audio/wav", "audio/ogg", "audio/flac", "audio/aac"};
        
        for (String mimeType : mimeTypes) {
            audio.setmimeType(mimeType);
            assertEquals(mimeType, audio.getmimeType());
        }
    }

    @Test
    void testTamanoDiferentes() {
        long[] tamanos = {512000L, 1024000L, 2048000L, 4096000L};
        
        for (long tamano : tamanos) {
            audio.settamanoBytes(tamano);
            assertEquals(tamano, audio.gettamanoBytes());
        }
    }

    @Test
    void testPropiedadesContenidoEnAudio() {
        audio.setestado(false);
        assertFalse(audio.isestado());
        
        audio.setvip(false);
        assertFalse(audio.isvip());
        
        audio.setduracion(200.0);
        assertEquals(200.0, audio.getduracion());
    }

    @Test
    void testFicheroNulo() {
        audio.setfichero(null);
        assertNull(audio.getfichero());
    }
}
