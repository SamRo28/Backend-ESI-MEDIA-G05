package iso25.g05.esi_media.repository;

import iso25.g05.esi_media.model.Contenido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContenidoRepository extends MongoRepository<Contenido, String> {
    @Query("{'titulo': {$regex: ?0, $options: 'i'}}")
    List<Contenido> findByTituloContainingIgnoreCase(String titulo);
    
    @Query("{'tags': {$in: [?0]}}")
    List<Contenido> findByTagsContaining(String tag);

    @Query("{'estado': true}")
    List<Contenido> findByEstadoTrue();

    // Listado paginado de contenidos visibles para una edad máxima (independiente de VIP).
    // Importante: si 'edadvisualizacion' no existe en el documento, tratamos como sin restricción.
    @Query("{'estado': true, $or:[ {'edadvisualizacion': {$lte: ?0}}, {'edadvisualizacion': {$exists:false}} ] }")
    Page<Contenido> findByEstadoTrueAndEdadAllowed(int edadvisualizacion, Pageable pageable);

    // Listado paginado de contenidos visibles y NO VIP, para usuarios no VIP.
    @Query("{'estado': true, 'vip': false, $or:[ {'edadvisualizacion': {$lte: ?0}}, {'edadvisualizacion': {$exists:false}} ] }")
    Page<Contenido> findByEstadoTrueAndVipFalseAndEdadAllowed(int edadvisualizacion, Pageable pageable);

    // Métodos legacy conservados para compatibilidad con tests/mocks antiguos
    @Query("{'estado': true, $or:[ {'edadvisualizacion': {$lte: ?0}}, {'edadvisualizacion': {$exists:false}} ] }")
    Page<Contenido> findByEstadoTrueAndEdadvisualizacionLessThanEqual(int edadvisualizacion, Pageable pageable);

    @Query("{'estado': true, 'vip': false, $or:[ {'edadvisualizacion': {$lte: ?0}}, {'edadvisualizacion': {$exists:false}} ] }")
    Page<Contenido> findByEstadoTrueAndVipFalseAndEdadvisualizacionLessThanEqual(int edadvisualizacion, Pageable pageable);

    // Filtrado por tipo usando el campo interno _class (nombre de la clase Java persistida)
    @Query("{'estado': true, $or:[ {'edadvisualizacion': {$lte: ?0}}, {'edadvisualizacion': {$exists:false}} ], '_class': ?1}")
    Page<Contenido> findByEstadoTrueAndEdadvisualizacionLessThanEqualAndClass(int edadvisualizacion, String className, Pageable pageable);

    @Query("{'estado': true, 'vip': false, $or:[ {'edadvisualizacion': {$lte: ?0}}, {'edadvisualizacion': {$exists:false}} ], '_class': ?1}")
    Page<Contenido> findByEstadoTrueAndVipFalseAndEdadvisualizacionLessThanEqualAndClass(int edadvisualizacion, String className, Pageable pageable);

    // Alternativas robustas por existencia de campos característicos
    @Query("{'estado': true, $or:[ {'edadvisualizacion': {$lte: ?0}}, {'edadvisualizacion': {$exists:false}} ], 'url': {$exists: true}}")
    Page<Contenido> findVideos(int edadvisualizacion, Pageable pageable);

    @Query("{'estado': true, 'vip': false, $or:[ {'edadvisualizacion': {$lte: ?0}}, {'edadvisualizacion': {$exists:false}} ], 'url': {$exists: true}}")
    Page<Contenido> findVideosNoVip(int edadvisualizacion, Pageable pageable);

    @Query("{'estado': true, $or:[ {'edadvisualizacion': {$lte: ?0}}, {'edadvisualizacion': {$exists:false}} ], 'mimeType': {$exists: true}}")
    Page<Contenido> findAudios(int edadvisualizacion, Pageable pageable);

    @Query("{'estado': true, 'vip': false, $or:[ {'edadvisualizacion': {$lte: ?0}}, {'edadvisualizacion': {$exists:false}} ], 'mimeType': {$exists: true}}")
    Page<Contenido> findAudiosNoVip(int edadvisualizacion, Pageable pageable);

    // Búsqueda por id solo si está visible
    Optional<Contenido> findByIdAndEstadoTrue(String id);

    // Métodos de búsqueda por texto (título y descripción) con filtros
    
    // Búsqueda general en contenidos (todos los tipos)
    @Query("{'estado': true, 'edadvisualizacion': {$lte: ?1}, $or: [{'titulo': {$regex: ?0, $options: 'i'}}, {'descripcion': {$regex: ?0, $options: 'i'}}]}")
    Page<Contenido> searchContenidos(String query, int edadvisualizacion, Pageable pageable);

    @Query("{'estado': true, 'vip': false, 'edadvisualizacion': {$lte: ?1}, $or: [{'titulo': {$regex: ?0, $options: 'i'}}, {'descripcion': {$regex: ?0, $options: 'i'}}]}")
    Page<Contenido> searchContenidosNoVip(String query, int edadvisualizacion, Pageable pageable);

    // Búsqueda específica en videos
    @Query("{'estado': true, 'edadvisualizacion': {$lte: ?1}, 'url': {$exists: true}, $or: [{'titulo': {$regex: ?0, $options: 'i'}}, {'descripcion': {$regex: ?0, $options: 'i'}}]}")
    Page<Contenido> searchVideos(String query, int edadvisualizacion, Pageable pageable);

    @Query("{'estado': true, 'vip': false, 'edadvisualizacion': {$lte: ?1}, 'url': {$exists: true}, $or: [{'titulo': {$regex: ?0, $options: 'i'}}, {'descripcion': {$regex: ?0, $options: 'i'}}]}")
    Page<Contenido> searchVideosNoVip(String query, int edadvisualizacion, Pageable pageable);

    // Búsqueda específica en audios
    @Query("{'estado': true, 'edadvisualizacion': {$lte: ?1}, 'mimeType': {$exists: true}, $or: [{'titulo': {$regex: ?0, $options: 'i'}}, {'descripcion': {$regex: ?0, $options: 'i'}}]}")
    Page<Contenido> searchAudios(String query, int edadvisualizacion, Pageable pageable);

    @Query("{'estado': true, 'vip': false, 'edadvisualizacion': {$lte: ?1}, 'mimeType': {$exists: true}, $or: [{'titulo': {$regex: ?0, $options: 'i'}}, {'descripcion': {$regex: ?0, $options: 'i'}}]}")
    Page<Contenido> searchAudiosNoVip(String query, int edadvisualizacion, Pageable pageable);
}