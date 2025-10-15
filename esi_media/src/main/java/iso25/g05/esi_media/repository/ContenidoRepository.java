package iso25.g05.esi_media.repository;

<<<<<<< HEAD
=======
import iso25.g05.esi_media.model.Contenido;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
>>>>>>> alvaro
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Contenido;

@Repository
public interface ContenidoRepository extends MongoRepository<Contenido, String> {
<<<<<<< HEAD
    @Query(value = "{ '_titulo': { $regex: ?0, $options: 'i' } }")
    List<Contenido> findByTituloContainingIgnoreCase(String titulo);

    @Query("{ '_tags' : ?0 }")
    List<Contenido> findByTagsContaining(String tag);

    @Query("{ '_estado' : true }")
=======
    @Query("{'titulo': {$regex: ?0, $options: 'i'}}")
    List<Contenido> findByTituloContainingIgnoreCase(String titulo);
    
    @Query("{'tags': {$in: [?0]}}")
    List<Contenido> findByTagsContaining(String tag);

    @Query("{'estado': true}")
>>>>>>> alvaro
    List<Contenido> findByEstadoTrue();
}