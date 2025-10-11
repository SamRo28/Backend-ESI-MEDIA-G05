package iso25.g05.esi_media.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Codigo_recuperacion;

@Repository
public interface CodigoRecuperacionRepository extends MongoRepository<Codigo_recuperacion, String> {
    @Query("{'_codigo': ?0}")
    Optional<Codigo_recuperacion> findByCodigo(String codigo);
}
