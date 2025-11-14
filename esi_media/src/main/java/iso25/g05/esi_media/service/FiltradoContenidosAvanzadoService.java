package iso25.g05.esi_media.service;

import iso25.g05.esi_media.dto.ContenidoDTO;
import iso25.g05.esi_media.dto.TagStatDTO;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.Contenido;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Video;
import iso25.g05.esi_media.repository.ContenidoRepository;
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
    
    private final ContenidoRepository contenidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MongoTemplate mongoTemplate;
    
    public FiltradoContenidosAvanzadoService(ContenidoRepository contenidoRepository, 
                                             UsuarioRepository usuarioRepository,
                                             MongoTemplate mongoTemplate) {
        this.contenidoRepository = contenidoRepository;
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
        criteria.add(Criteria.where("estado").is(true));
        
        // Filtro por edad si el usuario no es adulto
        if (!userIsAdult) {
            criteria.add(Criteria.where("edadvisualizacion").lte(0));
        }
        
        // Filtro por tipo de contenido
        if (!"all".equals(contentType)) {
            if ("video".equals(contentType)) {
                criteria.add(Criteria.where("url").exists(true)); // Videos tienen campo url
            } else if ("audio".equals(contentType)) {
                criteria.add(Criteria.where("mimeType").exists(true)); // Audios tienen campo mimeType
            }
        }
        
        // Construir la agregación
        MatchOperation matchOperation = Aggregation.match(
            new Criteria().andOperator(criteria.toArray(new Criteria[0]))
        );
        
        SortOperation sortOperation = Aggregation.sort(
            org.springframework.data.domain.Sort.Direction.DESC, "nvisualizaciones"
        );
        
        LimitOperation limitOperation = Aggregation.limit(limit);
        
        ProjectionOperation projectionOperation = Aggregation.project()
            .and("_id").as("id")
            .and("titulo").as("titulo")
            .and("descripcion").as("descripcion")
            .and("nvisualizaciones").as("nvisualizaciones")
            .and("edadvisualizacion").as("edadvisualizacion")
            .and("estado").as("estado")
            .and("tags").as("tags")
            .and("url").as("url")          // Para videos
            .and("resolucion").as("resolucion") // Para videos
            .and("mimeType").as("mimeType"); // Para audios
        
        Aggregation aggregation = Aggregation.newAggregation(
            matchOperation,
            sortOperation,
            limitOperation,
            projectionOperation
        );
        
        // Ejecutar agregación
        AggregationResults<Map> results = mongoTemplate.aggregate(
            aggregation, "contenidos", Map.class
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
        criteria.add(Criteria.where("estado").is(true));
        
        // Filtro por edad si el usuario no es adulto
        if (!userIsAdult) {
            criteria.add(Criteria.where("edadvisualizacion").lte(0));
        }
        
        // Filtro por tipo de contenido
        if (!"all".equals(contentType)) {
            if ("video".equals(contentType)) {
                criteria.add(Criteria.where("url").exists(true)); // Videos tienen campo url
            } else if ("audio".equals(contentType)) {
                criteria.add(Criteria.where("mimeType").exists(true)); // Audios tienen campo mimeType
            }
        }
        
        // Construir la agregación
        MatchOperation matchOperation = Aggregation.match(
            new Criteria().andOperator(criteria.toArray(new Criteria[0]))
        );
        
        // Desenrollar los tags para procesar cada tag individualmente
        UnwindOperation unwindOperation = Aggregation.unwind("tags");
        
        // Agrupar por tag y sumar las visualizaciones
        GroupOperation groupOperation = Aggregation.group("tags")
            .sum("nvisualizaciones").as("totalViews");
        
        SortOperation sortOperation = Aggregation.sort(
            org.springframework.data.domain.Sort.Direction.DESC, "totalViews"
        );
        
        LimitOperation limitOperation = Aggregation.limit(limit);
        
        ProjectionOperation projectionOperation = Aggregation.project()
            .and("_id").as("tag")
            .and("totalViews").as("views");
        
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
            aggregation, "contenidos", Map.class
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
            // En caso de error, política conservadora
            System.err.println("Error al verificar usuario: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Convierte un Map resultado de agregación a ContenidoDTO
     */
    private ContenidoDTO mapToContenidoDTO(Map<String, Object> map) {
        ContenidoDTO dto = new ContenidoDTO();
        
        // El _id de Mongo puede venir como ObjectId; convertir a String de forma segura
        // Intentar 'id' (proyección), si no existe, probar '_id'
        Object idObj = map.get("id");
        if (idObj == null) {
            idObj = map.get("_id");
        }
        if (idObj == null) {
            dto.setId(null);
        } else if (idObj instanceof ObjectId) {
            dto.setId(((ObjectId) idObj).toHexString());
        } else {
            dto.setId(idObj.toString());
        }
        Object tituloObj = map.get("titulo");
        dto.setTitulo(tituloObj == null ? null : tituloObj.toString());

        Object descripcionObj = map.get("descripcion");
        dto.setDescripcion(descripcionObj == null ? null : descripcionObj.toString());
        
        // Determinar el tipo basado en los campos presentes
        if (map.get("url") != null) {
            dto.setTipo("video");
            Object resolucionObj = map.get("resolucion");
            dto.setResolucion(resolucionObj == null ? null : resolucionObj.toString());
        } else if (map.get("mimeType") != null) {
            dto.setTipo("audio");
        } else {
            dto.setTipo("contenido");
        }
        
        // Conversión segura de números
        Object nvisualizaciones = map.get("nvisualizaciones");
        if (nvisualizaciones instanceof Number) {
            dto.setNvisualizaciones(((Number) nvisualizaciones).intValue());
        } else if (nvisualizaciones != null) {
            try {
                dto.setNvisualizaciones(Integer.parseInt(nvisualizaciones.toString()));
            } catch (NumberFormatException ex) {
                dto.setNvisualizaciones(0);
            }
        } else {
            dto.setNvisualizaciones(0);
        }
        
        Object edadvisualizacion = map.get("edadvisualizacion");
        if (edadvisualizacion instanceof Number) {
            dto.setEdadvisualizacion(((Number) edadvisualizacion).intValue());
        } else if (edadvisualizacion != null) {
            try {
                dto.setEdadvisualizacion(Integer.parseInt(edadvisualizacion.toString()));
            } catch (NumberFormatException ex) {
                dto.setEdadvisualizacion(0);
            }
        } else {
            dto.setEdadvisualizacion(0);
        }
        
        Object estadoObj = map.get("estado");
        dto.setEstado(Boolean.TRUE.equals(estadoObj));

        // Tags (puede ser List de cualquier tipo) -> normalizar a List<String>
        Object tagsObj = map.get("tags");
        List<String> normalizedTags = new ArrayList<>();
        if (tagsObj instanceof List) {
            for (Object t : (List<?>) tagsObj) {
                if (t != null) normalizedTags.add(t.toString());
            }
        }
        dto.setTags(normalizedTags);
        
        // Por ahora thumbnailUrl lo dejamos null, se puede implementar más tarde
        dto.setThumbnailUrl(null);
        
        return dto;
    }
    
    /**
     * Convierte un Map resultado de agregación a TagStatDTO
     */
    private TagStatDTO mapToTagStatDTO(Map<String, Object> map) {
        Object tagObj = map.get("tag");
        String tag = tagObj == null ? null : tagObj.toString();
        Object viewsObj = map.get("views");
        long views;
        if (viewsObj instanceof Number) {
            views = ((Number) viewsObj).longValue();
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