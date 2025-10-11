package iso25.g05.esi_media.repository;

import iso25.g05.esi_media.model.Contenido;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContenidoRepository extends MongoRepository<Contenido, String> {
    @Query("{'_titulo': {$regex: ?0, $options: 'i'}}")
    List<Contenido> findByTituloContainingIgnoreCase(String titulo);
    
    @Query("{'_tags': {$in: [?0]}}")
    List<Contenido> findByTagsContaining(String tag);
    
    @Query("{'_estado': true}")
    List<Contenido> findByEstadoTrue();
}