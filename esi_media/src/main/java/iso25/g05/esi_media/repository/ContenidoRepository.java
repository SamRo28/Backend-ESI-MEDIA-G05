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

    // Listado paginado de contenidos visibles para una edad máxima (independiente de VIP)
    Page<Contenido> findByEstadoTrueAndEdadvisualizacionLessThanEqual(int edadvisualizacion, Pageable pageable);

    // Listado paginado de contenidos visibles y NO VIP, para usuarios no VIP
    Page<Contenido> findByEstadoTrueAndVipFalseAndEdadvisualizacionLessThanEqual(int edadvisualizacion, Pageable pageable);

    // Búsqueda por id solo si está visible
    Optional<Contenido> findByIdAndEstadoTrue(String id);
}