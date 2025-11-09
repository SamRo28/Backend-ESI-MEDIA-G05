package iso25.g05.esi_media.service;

import iso25.g05.esi_media.dto.ContenidoDetalleDTO;
import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.exception.AccesoNoAutorizadoException;
import iso25.g05.esi_media.exception.PeticionInvalidaException;
import iso25.g05.esi_media.exception.RecursoNoEncontradoException;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.Contenido;
import iso25.g05.esi_media.model.Video;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.ContenidoRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests: MultimediaService reglas de negocio")
class MultimediaServiceBehaviourTest {

    @Mock
    private ContenidoRepository contenidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MultimediaService multimediaService;

    private Visualizador buildViz(boolean vip, Integer yearsAgoOrNull) {
        Visualizador v = new Visualizador();
        v.setVip(vip);
        if (yearsAgoOrNull == null) {
            v.setFechaNac(null);
        } else {
            LocalDate birth = LocalDate.now().minusYears(yearsAgoOrNull).minusDays(1);
            v.setFechaNac(Date.from(birth.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        return v;
    }

    private Video buildVideo(String id, boolean vip, boolean visible, int edadMin) {
        Video v = new Video();
        v.setId(id);
        v.seturl("https://example.com/video/" + id);
        v.setvip(vip);
        v.setestado(visible);
        v.setedadvisualizacion(edadMin);
        v.settitulo("Video-" + id);
        v.setdescripcion("desc");
        return v;
    }

    private Audio buildAudio(String id, boolean vip, boolean visible, int edadMin, byte[] data) {
        Audio a = new Audio();
        a.setId(id);
        a.setvip(vip);
        a.setestado(visible);
        a.setedadvisualizacion(edadMin);
        a.settitulo("Audio-" + id);
        a.setdescripcion("desc");
        if (data != null) {
            a.setfichero(new Binary(BsonBinarySubType.BINARY, data));
            a.setmimeType("audio/mpeg");
            a.settamanoBytes(data.length);
        }
        return a;
    }

    @Test
    @DisplayName("listarContenidos: VIP usa repo sin filtro VIP")
    void listarContenidos_vip_ok() {
        when(usuarioRepository.findBySesionToken(eq("tok"))).thenReturn(Optional.of(buildViz(true, 30)));

        Page<Contenido> page = new PageImpl<>(List.of(buildVideo("v1", false, true, 0)));
        when(contenidoRepository.findByEstadoTrueAndEdadvisualizacionLessThanEqual(anyInt(), any())).thenReturn(page);

        Page<ContenidoResumenDTO> out = multimediaService.listarContenidos(PageRequest.of(0, 10), "tok");

        assertEquals(1, out.getTotalElements());
        verify(contenidoRepository, times(1)).findByEstadoTrueAndEdadvisualizacionLessThanEqual(anyInt(), any());
        verify(contenidoRepository, never()).findByEstadoTrueAndVipFalseAndEdadvisualizacionLessThanEqual(anyInt(), any());
    }

    @Test
    @DisplayName("listarContenidos: NO VIP usa repo con filtro VIP=false")
    void listarContenidos_noVip_ok() {
        when(usuarioRepository.findBySesionToken(eq("tok"))).thenReturn(Optional.of(buildViz(false, 25)));

        Page<Contenido> page = new PageImpl<>(List.of(buildAudio("a1", false, true, 0, new byte[]{1}))); 
        when(contenidoRepository.findByEstadoTrueAndVipFalseAndEdadvisualizacionLessThanEqual(anyInt(), any())).thenReturn(page);

        Page<ContenidoResumenDTO> out = multimediaService.listarContenidos(PageRequest.of(0, 10), "tok");

        assertEquals(1, out.getTotalElements());
        verify(contenidoRepository, times(1)).findByEstadoTrueAndVipFalseAndEdadvisualizacionLessThanEqual(anyInt(), any());
        verify(contenidoRepository, never()).findByEstadoTrueAndEdadvisualizacionLessThanEqual(anyInt(), any());
    }

    @Test
    @DisplayName("listarContenidos extrae token de 'Bearer x'")
    void listarContenidos_tokenBearer() {
        when(usuarioRepository.findBySesionToken(anyString())).thenReturn(Optional.of(buildViz(true, 20)));
        when(contenidoRepository.findByEstadoTrueAndEdadvisualizacionLessThanEqual(anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        multimediaService.listarContenidos(PageRequest.of(0, 1), "Bearer abc");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(usuarioRepository).findBySesionToken(captor.capture());
        assertEquals("abc", captor.getValue(), "Debe extraer el token sin el prefijo Bearer");
    }

    @Test
    @DisplayName("obtenerDetalle: 400 si id vacío")
    void obtenerDetalle_idVacio_400() {
        assertThrows(PeticionInvalidaException.class, () -> multimediaService.obtenerContenidoPorId("", "tok"));
    }

    @Test
    @DisplayName("obtenerDetalle: 404 si no existe o no visible")
    void obtenerDetalle_404() {
        when(usuarioRepository.findBySesionToken(anyString())).thenReturn(Optional.of(buildViz(true, 30)));
        when(contenidoRepository.findByIdAndEstadoTrue(eq("x"))).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> multimediaService.obtenerContenidoPorId("x", "tok"));
    }

    @Test
    @DisplayName("obtenerDetalle: 403 por edad mínima")
    void obtenerDetalle_403_edad() {
        when(usuarioRepository.findBySesionToken(anyString())).thenReturn(Optional.of(buildViz(true, 16)));
        when(contenidoRepository.findByIdAndEstadoTrue(eq("v1"))).thenReturn(Optional.of(buildVideo("v1", false, true, 18)));

        assertThrows(AccesoNoAutorizadoException.class, () -> multimediaService.obtenerContenidoPorId("v1", "tok"));
    }

    @Test
    @DisplayName("obtenerDetalle: 403 por VIP")
    void obtenerDetalle_403_vip() {
        when(usuarioRepository.findBySesionToken(anyString())).thenReturn(Optional.of(buildViz(false, 30)));
        when(contenidoRepository.findByIdAndEstadoTrue(eq("v2"))).thenReturn(Optional.of(buildVideo("v2", true, true, 0)));

        assertThrows(AccesoNoAutorizadoException.class, () -> multimediaService.obtenerContenidoPorId("v2", "tok"));
    }

    @Test
    @DisplayName("obtenerDetalle: 200 AUDIO devuelve endpoint interno")
    void obtenerDetalle_audio_ok() {
        when(usuarioRepository.findBySesionToken(anyString())).thenReturn(Optional.of(buildViz(true, 30)));
        when(contenidoRepository.findByIdAndEstadoTrue(eq("a1"))).thenReturn(Optional.of(buildAudio("a1", false, true, 0, new byte[]{1,2})));

        ContenidoDetalleDTO dto = multimediaService.obtenerContenidoPorId("a1", "tok");
        assertEquals("/multimedia/audio/a1", dto.getReferenciaReproduccion());
    }

    @Test
    @DisplayName("validarYObtenerAudio: 400 si id vacío")
    void validarAudio_idVacio_400() {
        assertThrows(PeticionInvalidaException.class, () -> multimediaService.validarYObtenerAudioParaStreaming("", "tok"));
    }

    @Test
    @DisplayName("validarYObtenerAudio: 404 si no existe")
    void validarAudio_noExiste_404() {
        when(usuarioRepository.findBySesionToken(anyString())).thenReturn(Optional.of(buildViz(true, 30)));
        when(contenidoRepository.findByIdAndEstadoTrue(eq("no"))).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> multimediaService.validarYObtenerAudioParaStreaming("no", "tok"));
    }

    @Test
    @DisplayName("validarYObtenerAudio: 400 si el contenido no es audio")
    void validarAudio_noEsAudio_400() {
        when(usuarioRepository.findBySesionToken(anyString())).thenReturn(Optional.of(buildViz(true, 30)));
        when(contenidoRepository.findByIdAndEstadoTrue(eq("v1"))).thenReturn(Optional.of(buildVideo("v1", false, true, 0)));

        assertThrows(PeticionInvalidaException.class, () -> multimediaService.validarYObtenerAudioParaStreaming("v1", "tok"));
    }

    @Test
    @DisplayName("validarYObtenerAudio: 403 por VIP o edad")
    void validarAudio_403() {
        when(usuarioRepository.findBySesionToken(anyString())).thenReturn(Optional.of(buildViz(false, 16)));
        when(contenidoRepository.findByIdAndEstadoTrue(eq("a1"))).thenReturn(Optional.of(buildAudio("a1", true, true, 18, new byte[]{1})));

        assertThrows(AccesoNoAutorizadoException.class, () -> multimediaService.validarYObtenerAudioParaStreaming("a1", "tok"));
    }

    @Test
    @DisplayName("validarYObtenerAudio: 200 cuando usuario autorizado")
    void validarAudio_ok() {
        when(usuarioRepository.findBySesionToken(anyString())).thenReturn(Optional.of(buildViz(true, 30)));
        Audio audio = buildAudio("a2", false, true, 0, new byte[]{1,2,3});
        when(contenidoRepository.findByIdAndEstadoTrue(eq("a2"))).thenReturn(Optional.of(audio));

        Audio res = multimediaService.validarYObtenerAudioParaStreaming("a2", "Bearer abc");
        assertNotNull(res);
        assertEquals("a2", res.getId());
    }

    @Test
    @DisplayName("obtenerDetalle: si fechaNac es null, no bloquea por edad")
    void obtenerDetalle_fechaNull_noBloquea() {
        when(usuarioRepository.findBySesionToken(anyString())).thenReturn(Optional.of(buildViz(true, null)));
        when(contenidoRepository.findByIdAndEstadoTrue(eq("v3"))).thenReturn(Optional.of(buildVideo("v3", false, true, 200)));

        // No debe lanzar excepción (edad por defecto 200)
        ContenidoDetalleDTO dto = multimediaService.obtenerContenidoPorId("v3", "tok");
        assertEquals("https://example.com/video/v3", dto.getReferenciaReproduccion());
    }
}
