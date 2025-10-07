package iso25.g05.esi_media.repository;

import iso25.g05.esi_media.model.Contenido;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContenidoRepository extends MongoRepository<Contenido, String> {
    List<Contenido> findBy_tituloContainingIgnoreCase(String titulo);
    List<Contenido> findBy_tagsContaining(String tag);
    List<Contenido> findBy_estadoTrue();
}