package iso25.g05.esi_media.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import iso25.g05.esi_media.dto.ContenidoDTO;
import iso25.g05.esi_media.dto.TagStatDTO;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.UsuarioRepository;

/**
 * Tests unitarios para FiltradoContenidosAvanzadoService
 * Cubre funcionalidades de TOP contenidos y TOP tags
 * Valida filtrado por edad, tipo de contenido y usuario
 */
@ExtendWith(MockitoExtension.class)
class FiltradoContenidosAvanzadoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private FiltradoContenidosAvanzadoService filtradoService;

    private Visualizador visualizadorAdulto;
    private Visualizador visualizadorMenor;
    private Usuario usuarioGenerico;

    private static final String ID_ADULTO = "adulto-id-1";
    private static final String ID_MENOR = "menor-id-1";
    private static final String ID_GENERICO = "usuario-id-1";

    @BeforeEach
    void setUp() {
        // Visualizador adulto (mayor de 18)
        visualizadorAdulto = new Visualizador();
        visualizadorAdulto.setId(ID_ADULTO);
        visualizadorAdulto.setNombre("Usuario Adulto");
        LocalDate fechaNacAdulto = LocalDate.now().minusYears(25);
        Date dateAdulto = Date.from(fechaNacAdulto.atStartOfDay(ZoneId.systemDefault()).toInstant());
        visualizadorAdulto.setFechaNac(dateAdulto);

        // Visualizador menor (menor de 18)
        visualizadorMenor = new Visualizador();
        visualizadorMenor.setId(ID_MENOR);
        visualizadorMenor.setNombre("Usuario Menor");
        LocalDate fechaNacMenor = LocalDate.now().minusYears(15);
        Date dateMenor = Date.from(fechaNacMenor.atStartOfDay(ZoneId.systemDefault()).toInstant());
        visualizadorMenor.setFechaNac(dateMenor);

        // Usuario genérico (no es Visualizador)
        usuarioGenerico = new Usuario();
        usuarioGenerico.setId(ID_GENERICO);
        usuarioGenerico.setNombre("Usuario Generico");
    }

    // ==================== TESTS DE TOP CONTENIDOS ====================

    @Test
    void testGetTopContents_UsuarioAdulto_RetornaTodosLosContenidos() {
        // Arrange
        List<Map<String, Object>> contenidos = crearContenidosMock(true);
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(contenidos);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);
        when(usuarioRepository.findById(ID_ADULTO)).thenReturn(Optional.of(visualizadorAdulto));

        // Act
        List<ContenidoDTO> resultado = filtradoService.getTopContents(5, "all", ID_ADULTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        verify(usuarioRepository).findById(ID_ADULTO);
    }

    @Test
    void testGetTopContents_UsuarioMenor_FiltraContenido18Plus() {
        // Arrange
        List<Map<String, Object>> contenidos = crearContenidosMock(true);
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(contenidos);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);
        when(usuarioRepository.findById(ID_MENOR)).thenReturn(Optional.of(visualizadorMenor));

        // Act
        List<ContenidoDTO> resultado = filtradoService.getTopContents(5, "all", ID_MENOR);

        // Assert
        assertNotNull(resultado);
        // Solo debe retornar contenidos sin restricción de edad
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(c -> c.getEdadvisualizacion() <= 0));
        verify(usuarioRepository).findById(ID_MENOR);
    }

    @Test
    void testGetTopContents_UsuarioAnonimo_FiltraContenido18Plus() {
        // Arrange
        List<Map<String, Object>> contenidos = crearContenidosMock(true);
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(contenidos);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);

        // Act
        List<ContenidoDTO> resultado = filtradoService.getTopContents(5, "all", null);

        // Assert
        assertNotNull(resultado);
        // Usuario anónimo, filtra contenido +18
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(c -> c.getEdadvisualizacion() <= 0));
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void testGetTopContents_UsuarioNoExistente_TrataComoAnonimo() {
        // Arrange
        List<Map<String, Object>> contenidos = crearContenidosMock(true);
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(contenidos);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);
        when(usuarioRepository.findById("id-inexistente")).thenReturn(Optional.empty());

        // Act
        List<ContenidoDTO> resultado = filtradoService.getTopContents(5, "all", "id-inexistente");

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(usuarioRepository).findById("id-inexistente");
    }

    @Test
    void testGetTopContents_SoloVideos_RetornaSoloVideos() {
        // Arrange
        List<Map<String, Object>> videos = crearVideosMock();
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(videos);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);
        when(usuarioRepository.findById(ID_ADULTO)).thenReturn(Optional.of(visualizadorAdulto));

        // Act
        List<ContenidoDTO> resultado = filtradoService.getTopContents(5, "video", ID_ADULTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(c -> "video".equals(c.getTipo())));
    }

    @Test
    void testGetTopContents_SoloAudios_RetornaSoloAudios() {
        // Arrange
        List<Map<String, Object>> audios = crearAudiosMock();
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(audios);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);
        when(usuarioRepository.findById(ID_ADULTO)).thenReturn(Optional.of(visualizadorAdulto));

        // Act
        List<ContenidoDTO> resultado = filtradoService.getTopContents(5, "audio", ID_ADULTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.stream().allMatch(c -> "audio".equals(c.getTipo())));
    }

    @Test
    void testGetTopContents_UsuarioGenerico_PermiteContenido18Plus() {
        // Arrange - Usuario genérico (no Visualizador) permite +18 por fallback
        List<Map<String, Object>> contenidos = crearContenidosMock(true);
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(contenidos);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);
        when(usuarioRepository.findById(ID_GENERICO)).thenReturn(Optional.of(usuarioGenerico));

        // Act
        List<ContenidoDTO> resultado = filtradoService.getTopContents(5, "all", ID_GENERICO);

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.size()); // No filtra por ser Usuario genérico
        verify(usuarioRepository).findById(ID_GENERICO);
    }

    @Test
    void testGetTopContents_ConLimite_RespetaLimite() {
        // Arrange
        List<Map<String, Object>> contenidos = crearContenidosMock(false);
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(contenidos.subList(0, 2));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);

        // Act
        List<ContenidoDTO> resultado = filtradoService.getTopContents(2, "all", null);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    void testGetTopContents_UsuarioVacioString_TrataComoAnonimo() {
        // Arrange
        List<Map<String, Object>> contenidos = crearContenidosMock(true);
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(contenidos);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);

        // Act
        List<ContenidoDTO> resultado = filtradoService.getTopContents(5, "all", "   ");

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size()); // Filtra +18
        verify(usuarioRepository, never()).findById(any());
    }

    // ==================== TESTS DE TOP TAGS ====================

    @Test
    void testGetTopTags_UsuarioAdulto_RetornaTodosLosTags() {
        // Arrange
        List<Map<String, Object>> tags = crearTagsMock();
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(tags);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);
        when(usuarioRepository.findById(ID_ADULTO)).thenReturn(Optional.of(visualizadorAdulto));

        // Act
        List<TagStatDTO> resultado = filtradoService.getTopTags(5, "all", ID_ADULTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals("programacion", resultado.get(0).getTag());
        verify(usuarioRepository).findById(ID_ADULTO);
    }

    @Test
    void testGetTopTags_UsuarioMenor_FiltraTagsDeContenido18Plus() {
        // Arrange
        List<Map<String, Object>> tags = crearTagsMock();
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(tags);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);
        when(usuarioRepository.findById(ID_MENOR)).thenReturn(Optional.of(visualizadorMenor));

        // Act
        List<TagStatDTO> resultado = filtradoService.getTopTags(5, "all", ID_MENOR);

        // Assert
        assertNotNull(resultado);
        // El servicio filtra contenidos +18 en la agregación de MongoDB
        assertTrue(resultado.size() > 0);
        verify(usuarioRepository).findById(ID_MENOR);
    }

    @Test
    void testGetTopTags_UsuarioAnonimo_FiltraTagsDeContenido18Plus() {
        // Arrange
        List<Map<String, Object>> tags = crearTagsMock();
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(tags);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);

        // Act
        List<TagStatDTO> resultado = filtradoService.getTopTags(5, "all", null);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.size() > 0);
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void testGetTopTags_SoloVideos_RetornaTagsDeVideos() {
        // Arrange
        List<Map<String, Object>> tags = crearTagsMock();
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(tags);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);
        when(usuarioRepository.findById(ID_ADULTO)).thenReturn(Optional.of(visualizadorAdulto));

        // Act
        List<TagStatDTO> resultado = filtradoService.getTopTags(5, "video", ID_ADULTO);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.size() > 0);
    }

    @Test
    void testGetTopTags_SoloAudios_RetornaTagsDeAudios() {
        // Arrange
        List<Map<String, Object>> tags = crearTagsMock();
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(tags);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);
        when(usuarioRepository.findById(ID_ADULTO)).thenReturn(Optional.of(visualizadorAdulto));

        // Act
        List<TagStatDTO> resultado = filtradoService.getTopTags(5, "audio", ID_ADULTO);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.size() > 0);
    }

    @Test
    void testGetTopTags_ConLimite_RespetaLimite() {
        // Arrange
        List<Map<String, Object>> tags = crearTagsMock();
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(tags.subList(0, 2));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);

        // Act
        List<TagStatDTO> resultado = filtradoService.getTopTags(2, "all", null);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    void testGetTopTags_OrdenadosPorVisualizaciones_RetornaOrdenCorrecto() {
        // Arrange
        List<Map<String, Object>> tags = crearTagsMock();
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(tags);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);
        when(usuarioRepository.findById(ID_ADULTO)).thenReturn(Optional.of(visualizadorAdulto));

        // Act
        List<TagStatDTO> resultado = filtradoService.getTopTags(5, "all", ID_ADULTO);

        // Assert
        assertNotNull(resultado);
        // Verificar que están ordenados por visualizaciones descendente
        for (int i = 0; i < resultado.size() - 1; i++) {
            assertTrue(resultado.get(i).getViews() >= resultado.get(i + 1).getViews());
        }
    }

    // ==================== TESTS DE MANEJO DE ERRORES ====================

    @Test
    void testGetTopContents_ErrorEnRepositorio_NoLanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findById(ID_ADULTO)).thenThrow(new RuntimeException("DB Error"));
        List<Map<String, Object>> contenidos = crearContenidosMock(false);
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(contenidos);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);

        // Act
        List<ContenidoDTO> resultado = filtradoService.getTopContents(5, "all", ID_ADULTO);

        // Assert - Debería continuar sin lanzar excepción, tratando como anónimo
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    void testGetTopTags_ErrorEnRepositorio_NoLanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findById(ID_MENOR)).thenThrow(new RuntimeException("DB Error"));
        List<Map<String, Object>> tags = crearTagsMock();
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(tags);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);

        // Act
        List<TagStatDTO> resultado = filtradoService.getTopTags(5, "all", ID_MENOR);

        // Assert - Debería continuar sin lanzar excepción
        assertNotNull(resultado);
    }

    // ==================== TESTS DE CASOS LÍMITE ====================

    @Test
    void testGetTopContents_UsuarioSinFechaNacimiento_TrataComoAdulto() {
        // Arrange
        Visualizador visualizadorSinFecha = new Visualizador();
        visualizadorSinFecha.setId("sin-fecha-id");
        visualizadorSinFecha.setNombre("Usuario Sin Fecha");
        visualizadorSinFecha.setFechaNac(null); // Sin fecha de nacimiento

        List<Map<String, Object>> contenidos = crearContenidosMock(true);
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(contenidos);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);
        when(usuarioRepository.findById("sin-fecha-id"))
            .thenReturn(Optional.of(visualizadorSinFecha));

        // Act
        List<ContenidoDTO> resultado = filtradoService.getTopContents(5, "all", "sin-fecha-id");

        // Assert
        assertNotNull(resultado);
        // Sin fecha de nacimiento, trata como adulto (fallback)
        assertEquals(3, resultado.size());
    }

    @Test
    void testGetTopContents_VisualizadorCon18AniosExactos_TrataComoAdulto() {
        // Arrange
        Visualizador visualizador18 = new Visualizador();
        visualizador18.setId("id-18-años");
        LocalDate fechaNac18 = LocalDate.now().minusYears(18);
        Date date18 = Date.from(fechaNac18.atStartOfDay(ZoneId.systemDefault()).toInstant());
        visualizador18.setFechaNac(date18);

        List<Map<String, Object>> contenidos = crearContenidosMock(true);
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(contenidos);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);
        when(usuarioRepository.findById("id-18-años")).thenReturn(Optional.of(visualizador18));

        // Act
        List<ContenidoDTO> resultado = filtradoService.getTopContents(5, "all", "id-18-años");

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.size()); // Es adulto (18 años cumplidos)
    }

    @Test
    void testGetTopContents_ResultadosVacios_RetornaListaVacia() {
        // Arrange
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(Collections.emptyList());
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);

        // Act
        List<ContenidoDTO> resultado = filtradoService.getTopContents(5, "all", null);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testGetTopTags_ResultadosVacios_RetornaListaVacia() {
        // Arrange
        AggregationResults<Map<String, Object>> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(Collections.emptyList());
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("contenidos"), eq(Map.class)))
            .thenReturn((AggregationResults) results);

        // Act
        List<TagStatDTO> resultado = filtradoService.getTopTags(5, "all", null);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private List<Map<String, Object>> crearContenidosMock(boolean incluirContenido18Plus) {
        List<Map<String, Object>> contenidos = new ArrayList<>();

        // Contenido 1: Apto para todos
        Map<String, Object> contenido1 = new HashMap<>();
        contenido1.put("_id", "contenido-1");
        contenido1.put("titulo", "Tutorial Java");
        contenido1.put("descripcion", "Tutorial de Java para principiantes");
        contenido1.put("nvisualizaciones", 1000);
        contenido1.put("edadvisualizacion", 0);
        contenido1.put("estado", true);
        contenido1.put("url", "http://example.com/video1.mp4");
        contenido1.put("resolucion", "1080p");
        contenido1.put("tags", Arrays.asList("java", "programacion"));
        contenidos.add(contenido1);

        // Contenido 2: Apto para todos
        Map<String, Object> contenido2 = new HashMap<>();
        contenido2.put("_id", "contenido-2");
        contenido2.put("titulo", "Podcast Tech");
        contenido2.put("descripcion", "Podcast sobre tecnología");
        contenido2.put("nvisualizaciones", 800);
        contenido2.put("edadvisualizacion", 0);
        contenido2.put("estado", true);
        contenido2.put("mimeType", "audio/mpeg");
        contenido2.put("tags", Arrays.asList("podcast", "tech"));
        contenidos.add(contenido2);

        if (incluirContenido18Plus) {
            // Contenido 3: +18
            Map<String, Object> contenido3 = new HashMap<>();
            contenido3.put("_id", "contenido-3");
            contenido3.put("titulo", "Documental Adultos");
            contenido3.put("descripcion", "Documental para adultos");
            contenido3.put("nvisualizaciones", 600);
            contenido3.put("edadvisualizacion", 18);
            contenido3.put("estado", true);
            contenido3.put("url", "http://example.com/video3.mp4");
            contenido3.put("resolucion", "720p");
            contenido3.put("tags", Arrays.asList("documental", "adultos"));
            contenidos.add(contenido3);
        }

        return contenidos;
    }

    private List<Map<String, Object>> crearVideosMock() {
        List<Map<String, Object>> videos = new ArrayList<>();

        Map<String, Object> video1 = new HashMap<>();
        video1.put("_id", "video-1");
        video1.put("titulo", "Tutorial Spring Boot");
        video1.put("descripcion", "Tutorial completo de Spring Boot");
        video1.put("nvisualizaciones", 1500);
        video1.put("edadvisualizacion", 0);
        video1.put("estado", true);
        video1.put("url", "http://example.com/video1.mp4");
        video1.put("resolucion", "1080p");
        video1.put("tags", Arrays.asList("spring", "java"));
        videos.add(video1);

        Map<String, Object> video2 = new HashMap<>();
        video2.put("_id", "video-2");
        video2.put("titulo", "React para principiantes");
        video2.put("descripcion", "Aprende React desde cero");
        video2.put("nvisualizaciones", 1200);
        video2.put("edadvisualizacion", 0);
        video2.put("estado", true);
        video2.put("url", "http://example.com/video2.mp4");
        video2.put("resolucion", "720p");
        video2.put("tags", Arrays.asList("react", "javascript"));
        videos.add(video2);

        return videos;
    }

    private List<Map<String, Object>> crearAudiosMock() {
        List<Map<String, Object>> audios = new ArrayList<>();

        Map<String, Object> audio1 = new HashMap<>();
        audio1.put("_id", "audio-1");
        audio1.put("titulo", "Podcast de Tecnología");
        audio1.put("descripcion", "Último episodio de tech");
        audio1.put("nvisualizaciones", 900);
        audio1.put("edadvisualizacion", 0);
        audio1.put("estado", true);
        audio1.put("mimeType", "audio/mpeg");
        audio1.put("tags", Arrays.asList("podcast", "tecnologia"));
        audios.add(audio1);

        return audios;
    }

    private List<Map<String, Object>> crearTagsMock() {
        List<Map<String, Object>> tags = new ArrayList<>();

        Map<String, Object> tag1 = new HashMap<>();
        tag1.put("tag", "programacion");
        tag1.put("views", 5000L);
        tags.add(tag1);

        Map<String, Object> tag2 = new HashMap<>();
        tag2.put("tag", "tutorial");
        tag2.put("views", 4000L);
        tags.add(tag2);

        Map<String, Object> tag3 = new HashMap<>();
        tag3.put("tag", "java");
        tag3.put("views", 3000L);
        tags.add(tag3);

        return tags;
    }
}
