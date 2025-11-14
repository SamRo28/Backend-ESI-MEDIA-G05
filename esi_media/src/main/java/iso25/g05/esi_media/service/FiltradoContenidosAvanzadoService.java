package iso25.g05.esi_media.service;

import iso25.g05.esi_media.dto.ContenidoDTO;
import iso25.g05.esi_media.dto.TagStatDTO;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.*;
import org.bson.types.ObjectId;
import java.util.stream.Collectors;

/**
 * Servicio para el filtrado avanzado de contenidos
 * Implementa las funcionalidades de TOP contenidos y TOP tags
 */
@Service
public class FiltradoContenidosAvanzadoService {
    private static final Logger logger = LoggerFactory.getLogger(FiltradoContenidosAvanzadoService.class);
    
    // Reusable string constants to avoid duplicated literals (SonarQube warnings)
    private static final String FIELD_ESTADO = "estado";
    private static final String FIELD_URL = "url";
    private static final String FIELD_MIME_TYPE = "mimeType";
    private static final String FIELD_EDAD_VISUALIZACION = "edadvisualizacion";
    private static final String FIELD_NVISUALIZACIONES = "nvisualizaciones";
    private static final String FIELD_TAGS = "tags";
    private static final String FIELD_RESOLUCION = "resolucion";
    private static final String FIELD_ID = "id";
    private static final String FIELD_UNDERSCORE_ID = "_id";
    private static final String FIELD_TITULO = "titulo";
    private static final String FIELD_DESCRIPCION = "descripcion";
    private static final String COLLECTION_CONTENIDOS = "contenidos";
    private static final String TAG_ALIAS = "tag";
    private static final String VIEWS_ALIAS = "views";
    private static final String TOTAL_VIEWS_ALIAS = "totalViews";
    private static final String TYPE_VIDEO = "video";
    private static final String TYPE_AUDIO = "audio";
    private static final String TYPE_ALL = "all";
    private static final String TYPE_CONTENIDO = "contenido";
    private final UsuarioRepository usuarioRepository;
    private final MongoTemplate mongoTemplate;
    
    public FiltradoContenidosAvanzadoService(UsuarioRepository usuarioRepository,
                                             MongoTemplate mongoTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.mongoTemplate = mongoTemplate;
    }
    
    /**
     * Obtiene los TOP N contenidos con más visualizaciones
     * 
     * @param limit Número máximo de contenidos a devolver
     * @param contentType Tipo de contenido ("video", "audio", "all")
     * @param userId ID del usuario (puede ser null para usuarios anónimos)
     * @return Lista de ContenidoDTO ordenada por visualizaciones descendente
     */
    public List<ContenidoDTO> getTopContents(int limit, String contentType, String userId) {
        
        // Determinar si el usuario puede ver contenido +18
        boolean userIsAdult = isUserAdult(userId);
        
        // Crear criterios de filtro
        List<Criteria> criteria = new ArrayList<>();
        
        // Solo contenidos visibles
        criteria.add(Criteria.where(FIELD_ESTADO).is(true));
        
        // Filtro por edad si el usuario no es adulto
        if (!userIsAdult) {
            criteria.add(Criteria.where(FIELD_EDAD_VISUALIZACION).lte(0));
        }
        
        // Filtro por tipo de contenido
        if (!TYPE_ALL.equals(contentType)) {
            if (TYPE_VIDEO.equals(contentType)) {
                criteria.add(Criteria.where(FIELD_URL).exists(true)); // Videos tienen campo url
            } else if (TYPE_AUDIO.equals(contentType)) {
                criteria.add(Criteria.where(FIELD_MIME_TYPE).exists(true)); // Audios tienen campo mimeType
            }
        }
        
        // Construir la agregación
        MatchOperation matchOperation = Aggregation.match(
            new Criteria().andOperator(criteria.toArray(new Criteria[0]))
        );
        
        SortOperation sortOperation = Aggregation.sort(
            org.springframework.data.domain.Sort.Direction.DESC, FIELD_NVISUALIZACIONES
        );
        
        LimitOperation limitOperation = Aggregation.limit(limit);
        
        ProjectionOperation projectionOperation = Aggregation.project()
            .and(FIELD_UNDERSCORE_ID).as(FIELD_ID)
            .and(FIELD_TITULO).as(FIELD_TITULO)
            .and(FIELD_DESCRIPCION).as(FIELD_DESCRIPCION)
            .and(FIELD_NVISUALIZACIONES).as(FIELD_NVISUALIZACIONES)
            .and(FIELD_EDAD_VISUALIZACION).as(FIELD_EDAD_VISUALIZACION)
            .and(FIELD_ESTADO).as(FIELD_ESTADO)
            .and(FIELD_TAGS).as(FIELD_TAGS)
            .and(FIELD_URL).as(FIELD_URL)          // Para videos
            .and(FIELD_RESOLUCION).as(FIELD_RESOLUCION) // Para videos
            .and(FIELD_MIME_TYPE).as(FIELD_MIME_TYPE); // Para audios
        
        Aggregation aggregation = Aggregation.newAggregation(
            matchOperation,
            sortOperation,
            limitOperation,
            projectionOperation
        );
        
        // Ejecutar agregación
        AggregationResults<Map> results = mongoTemplate.aggregate(
            aggregation, COLLECTION_CONTENIDOS, Map.class
        );

        // DEBUG: loguear contenido exacto devuelto por la agregación para depuración
        if (logger.isDebugEnabled()) {
            List<Map> mapped = results.getMappedResults();
            logger.debug("[Filtrado] Aggregation returned {} items", mapped.size());
            int i = 0;
            for (Map item : mapped) {
                logger.debug("[Filtrado] item[{}] keys={} => {}", i++, item.keySet(), item);
            }
        }

        // Convertir resultados a DTOs
        return results.getMappedResults().stream()
            .map(this::mapToContenidoDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene los TOP N tags con más visualizaciones acumuladas
     * 
     * @param limit Número máximo de tags a devolver
     * @param contentType Tipo de contenido ("video", "audio", "all")
     * @param userId ID del usuario (puede ser null para usuarios anónimos)
     * @return Lista de TagStatDTO ordenada por visualizaciones descendente
     */
    public List<TagStatDTO> getTopTags(int limit, String contentType, String userId) {
        
        // Determinar si el usuario puede ver contenido +18
        boolean userIsAdult = isUserAdult(userId);
        
        // Crear criterios de filtro
        List<Criteria> criteria = new ArrayList<>();
        
        // Solo contenidos visibles
        criteria.add(Criteria.where(FIELD_ESTADO).is(true));
        
        // Filtro por edad si el usuario no es adulto
        if (!userIsAdult) {
            criteria.add(Criteria.where(FIELD_EDAD_VISUALIZACION).lte(0));
        }
        
        // Filtro por tipo de contenido
        if (!TYPE_ALL.equals(contentType)) {
            if (TYPE_VIDEO.equals(contentType)) {
                criteria.add(Criteria.where(FIELD_URL).exists(true)); // Videos tienen campo url
            } else if (TYPE_AUDIO.equals(contentType)) {
                criteria.add(Criteria.where(FIELD_MIME_TYPE).exists(true)); // Audios tienen campo mimeType
            }
        }
        
        // Construir la agregación
        MatchOperation matchOperation = Aggregation.match(
            new Criteria().andOperator(criteria.toArray(new Criteria[0]))
        );
        
        // Desenrollar los tags para procesar cada tag individualmente
        UnwindOperation unwindOperation = Aggregation.unwind(FIELD_TAGS);

        // Agrupar por tag y sumar las visualizaciones
        GroupOperation groupOperation = Aggregation.group(FIELD_TAGS)
            .sum(FIELD_NVISUALIZACIONES).as(TOTAL_VIEWS_ALIAS);

        SortOperation sortOperation = Aggregation.sort(
            org.springframework.data.domain.Sort.Direction.DESC, TOTAL_VIEWS_ALIAS
        );
        
        LimitOperation limitOperation = Aggregation.limit(limit);
        
        ProjectionOperation projectionOperation = Aggregation.project()
            .and(FIELD_UNDERSCORE_ID).as(TAG_ALIAS)
            .and(TOTAL_VIEWS_ALIAS).as(VIEWS_ALIAS);
        
        Aggregation aggregation = Aggregation.newAggregation(
            matchOperation,
            unwindOperation,
            groupOperation,
            sortOperation,
            limitOperation,
            projectionOperation
        );
        
        // Ejecutar agregación
        AggregationResults<Map> results = mongoTemplate.aggregate(
            aggregation, COLLECTION_CONTENIDOS, Map.class
        );
        
        // Convertir resultados a DTOs
        return results.getMappedResults().stream()
            .map(this::mapToTagStatDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Determina si un usuario puede ver contenido +18
     * Para usuarios anónimos (userId null), se asume que no pueden ver +18
     */
    private boolean isUserAdult(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false; // Usuario anónimo, política conservadora
        }
        
        try {
            Optional<Usuario> usuario = usuarioRepository.findById(userId);
            if (usuario.isPresent()) {
                // Aquí puedes implementar tu lógica específica para determinar si es adulto
                // Por ahora asumimos que todos los usuarios registrados pueden ver +18
                // En el futuro podrías usar la fecha de nacimiento u otros campos
                return true;
            }
        } catch (Exception e) {
            // En caso de error, aplicamos la política conservadora (no adulto).
            logger.error("Error al verificar usuario: {}", e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * Convierte un Map resultado de agregación a ContenidoDTO
     */
    private ContenidoDTO mapToContenidoDTO(Map<String, Object> map) {
        ContenidoDTO dto = new ContenidoDTO();

        dto.setId(extractId(map));
        dto.setTitulo(toStringSafe(map, FIELD_TITULO));
        dto.setDescripcion(toStringSafe(map, FIELD_DESCRIPCION));

        if (map.get(FIELD_URL) != null) {
            dto.setTipo(TYPE_VIDEO);
            dto.setResolucion(toStringSafe(map, FIELD_RESOLUCION));
        } else if (map.get(FIELD_MIME_TYPE) != null) {
            dto.setTipo(TYPE_AUDIO);
        } else {
            dto.setTipo(TYPE_CONTENIDO);
        }

        dto.setNvisualizaciones(toIntSafe(map, FIELD_NVISUALIZACIONES));
        dto.setEdadvisualizacion(toIntSafe(map, FIELD_EDAD_VISUALIZACION));
        dto.setEstado(Boolean.TRUE.equals(map.get(FIELD_ESTADO)));
        dto.setTags(normalizeTags(map, FIELD_TAGS));
        dto.setThumbnailUrl(null);
        return dto;
    }

    // -- helpers to reduce cognitive complexity (preserve exact behavior) --
    private String extractId(Map<String, Object> map) {
        Object idObj = map.get(FIELD_ID);
        if (idObj == null) idObj = map.get(FIELD_UNDERSCORE_ID);
        if (idObj == null) return null;
        if (idObj instanceof ObjectId objectId) return objectId.toHexString();
        return idObj.toString();
    }

    private String toStringSafe(Map<String, Object> map, String key) {
        Object o = map.get(key);
        return o == null ? null : o.toString();
    }

    private int toIntSafe(Map<String, Object> map, String key) {
        Object o = map.get(key);
        if (o instanceof Number number) return number.intValue();
        if (o != null) {
            try {
                return Integer.parseInt(o.toString());
            } catch (NumberFormatException ex) {
                // No se pudo parsear la representación a entero; retornamos 0 como fallback.
                if (logger.isDebugEnabled()) {
                    logger.debug("toIntSafe: no se pudo parsear '{}' para la clave '{}', usando 0", o, key, ex);
                }
            }
        }
        return 0;
    }

    private List<String> normalizeTags(Map<String, Object> map, String key) {
        Object tagsObj = map.get(key);
        List<String> normalized = new ArrayList<>();
        if (tagsObj instanceof List) {
            for (Object t : (List<?>) tagsObj) if (t != null) normalized.add(t.toString());
        }
        return normalized;
    }
    
    /**
     * Convierte un Map resultado de agregación a TagStatDTO
     */
    private TagStatDTO mapToTagStatDTO(Map<String, Object> map) {
        Object tagObj = map.get(TAG_ALIAS);
        String tag = tagObj == null ? null : tagObj.toString();
        Object viewsObj = map.get(VIEWS_ALIAS);
        long views;
        if (viewsObj instanceof Number number) {
            views = number.longValue();
        } else if (viewsObj != null) {
            try {
                views = Long.parseLong(viewsObj.toString());
            } catch (NumberFormatException ex) {
                views = 0L;
            }
        } else {
            views = 0L;
        }

        return new TagStatDTO(tag, views);
    }
}