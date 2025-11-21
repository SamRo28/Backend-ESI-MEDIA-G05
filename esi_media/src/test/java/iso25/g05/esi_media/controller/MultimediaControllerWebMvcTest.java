package iso25.g05.esi_media.controller;

import java.util.List;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import iso25.g05.esi_media.dto.ContenidoDetalleDTO;
import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.exception.AccesoNoAutorizadoException;
import iso25.g05.esi_media.exception.PeticionInvalidaException;
import iso25.g05.esi_media.exception.RecursoNoEncontradoException;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.service.MultimediaService;
import iso25.g05.esi_media.service.LoggingService;

@WebMvcTest(controllers = MultimediaController.class)
@Import({GlobalExceptionHandler.class})
@DisplayName("WebMvcTest: MultimediaController + GlobalExceptionHandler")
class MultimediaControllerWebMvcTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
                private MultimediaService multimediaService;
                
        @MockitoBean
                private LoggingService loggingService;

    @Test
    @DisplayName("GET /multimedia devuelve 200 y una página de contenidos")
    void listarContenidos_ok() throws Exception {
        Page<ContenidoResumenDTO> page = new PageImpl<>(List.of(
                new ContenidoResumenDTO("c1", "Canción 1", "AUDIO", null, false, null),
                new ContenidoResumenDTO("v1", "Video 1", "VIDEO", null, true, "1080p")
        ), PageRequest.of(0, 2), 2);

        when(multimediaService.listarContenidos(
                org.mockito.ArgumentMatchers.<org.springframework.data.domain.Pageable>any(), 
                anyString(), 
                isNull(),
                isNull()))
                .thenReturn(page);

        // Verificar que el endpoint responde correctamente
        mockMvc.perform(get("/multimedia?page=0&size=2")
                        .cookie(new jakarta.servlet.http.Cookie("SESSION_TOKEN", "test-token")))
                .andExpect(status().isOk())
                // .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // El controller no especifica produces
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is("c1")))
                .andExpect(jsonPath("$.content[0].tipo", is("AUDIO")))
                .andExpect(jsonPath("$.content[1].id", is("v1")))
                .andExpect(jsonPath("$.content[1].vip", is(true)));
        
        // Verificar que el servicio fue invocado correctamente
        verify(multimediaService, times(1)).listarContenidos(any(), anyString(), isNull(), isNull());
    }

    @Test
    @DisplayName("GET /multimedia/{id} devuelve 200 con detalle")
    void obtenerDetalle_ok() throws Exception {
        ContenidoDetalleDTO detalle = new ContenidoDetalleDTO(
                                "c1", "Canción 1", "Desc", "AUDIO", null, false,
                                null, // fechadisponiblehasta
                                0,    // edadvisualizacion
                                0,    // nvisualizaciones
                                java.util.List.of("tag1"), // tags
                                "/multimedia/audio/c1", null);

        when(multimediaService.obtenerContenidoPorId(eq("c1"), anyString()))
                .thenReturn(detalle);

        mockMvc.perform(get("/multimedia/c1")
                        .cookie(new jakarta.servlet.http.Cookie("SESSION_TOKEN", "test-token")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is("c1")))
                .andExpect(jsonPath("$.tipo", is("AUDIO")))
                .andExpect(jsonPath("$.referenciaReproduccion", is("/multimedia/audio/c1")))
                .andExpect(jsonPath("$.tags[0]", is("tag1")));
    }

    @Test
    @DisplayName("GET /multimedia/audio/{id} devuelve 200 con audio completo y cabeceras MIME/Length")
    void streamAudio_ok_fullFile() throws Exception {
        byte[] bytes = new byte[]{1, 2, 3, 4, 5};
        Audio audio = new Audio();
        audio.setfichero(new Binary(BsonBinarySubType.BINARY, bytes));
        audio.setmimeType("audio/mpeg");
        audio.settamanoBytes(bytes.length);

        when(multimediaService.validarYObtenerAudioParaStreaming(eq("a1"), anyString()))
                .thenReturn(audio);

        mockMvc.perform(get("/multimedia/audio/a1")
                        .cookie(new jakarta.servlet.http.Cookie("SESSION_TOKEN", "test-token")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "audio/mpeg"))
                .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes.length)))
                .andExpect(content().bytes(bytes));
    }

    @Test
    @DisplayName("GET /multimedia/audio/{id} devuelve 404 si el binario está vacío")
    void streamAudio_notFound_whenBinaryMissing() throws Exception {
        Audio audio = new Audio();
        audio.setfichero(null); // Simula audio sin binario almacenado

        when(multimediaService.validarYObtenerAudioParaStreaming(eq("a2"), anyString()))
                .thenReturn(audio);

        mockMvc.perform(get("/multimedia/audio/a2")
                        .cookie(new jakarta.servlet.http.Cookie("SESSION_TOKEN", "test-token")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Errores se mapean a ErrorRespuestaDTO: 400 Bad Request")
    void errores_mapeo_400() throws Exception {
        when(multimediaService.validarYObtenerAudioParaStreaming(eq("bad"), anyString()))
                .thenThrow(new PeticionInvalidaException("El contenido solicitado no es de tipo audio"));

        mockMvc.perform(get("/multimedia/audio/bad")
                        .cookie(new jakarta.servlet.http.Cookie("SESSION_TOKEN", "test-token")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.mensaje", containsString("no es de tipo audio")))
                .andExpect(jsonPath("$.ruta", is("/multimedia/audio/bad")));
    }

    @Test
    @DisplayName("Errores se mapean a ErrorRespuestaDTO: 403 Forbidden")
    void errores_mapeo_403() throws Exception {
        when(multimediaService.obtenerContenidoPorId(eq("vip"), anyString()))
                .thenThrow(new AccesoNoAutorizadoException("Contenido disponible solo para usuarios VIP"));

        mockMvc.perform(get("/multimedia/vip")
                        .cookie(new jakarta.servlet.http.Cookie("SESSION_TOKEN", "test-token")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.mensaje", containsString("VIP")))
                .andExpect(jsonPath("$.ruta", is("/multimedia/vip")));
    }

    @Test
    @DisplayName("Errores se mapean a ErrorRespuestaDTO: 404 Not Found")
    void errores_mapeo_404() throws Exception {
        when(multimediaService.obtenerContenidoPorId(eq("x"), anyString()))
                .thenThrow(new RecursoNoEncontradoException("Contenido no encontrado"));

        mockMvc.perform(get("/multimedia/x")
                        .cookie(new jakarta.servlet.http.Cookie("SESSION_TOKEN", "test-token")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.mensaje", is("Contenido no encontrado")))
                .andExpect(jsonPath("$.ruta", is("/multimedia/x")));
    }
}
