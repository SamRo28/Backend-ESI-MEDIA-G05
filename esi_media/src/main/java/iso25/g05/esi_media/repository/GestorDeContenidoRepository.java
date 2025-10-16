package iso25.g05.esi_media.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.GestordeContenido;

@Repository
public interface GestorDeContenidoRepository extends MongoRepository<GestordeContenido, String> {
    @Query("{'campoespecializacion': ?0}")
    List<GestordeContenido> findByCampoEspecializacion(String campo);
}
