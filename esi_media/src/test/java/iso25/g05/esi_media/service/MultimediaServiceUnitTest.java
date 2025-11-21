package iso25.g05.esi_media.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.Video;

@DisplayName("Unit tests: MultimediaService helpers")
class MultimediaServiceUnitTest {

    @Test
    @DisplayName("construirReferenciaReproduccion devuelve URL del video")
    void construirReferencia_video() {
        MultimediaService service = new MultimediaService();
        Video v = new Video();
        v.setId("vid1");
        v.seturl("https://youtu.be/xxxxx");

        String ref = service.construirReferenciaReproduccion(v);

        assertEquals("https://youtu.be/xxxxx", ref);
    }

    @Test
    @DisplayName("construirReferenciaReproduccion devuelve endpoint de audio")
    void construirReferencia_audio() {
        MultimediaService service = new MultimediaService();
        Audio a = new Audio();
        a.setId("aud1");

        String ref = service.construirReferenciaReproduccion(a);

        assertEquals("http://localhost:8080/multimedia/audio/aud1", ref);
    }
}
