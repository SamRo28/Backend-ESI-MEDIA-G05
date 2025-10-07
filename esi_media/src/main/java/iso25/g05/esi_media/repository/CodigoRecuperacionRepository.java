package iso25.g05.esi_media.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Codigo_recuperacion;

@Repository
public interface CodigoRecuperacionRepository extends MongoRepository<Codigo_recuperacion, String> {
    Optional<Codigo_recuperacion> findBy_codigo(String codigo);
}
