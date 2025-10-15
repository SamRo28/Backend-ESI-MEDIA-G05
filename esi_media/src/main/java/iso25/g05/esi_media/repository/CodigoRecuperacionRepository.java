package iso25.g05.esi_media.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Codigorecuperacion;

@Repository
public interface CodigoRecuperacionRepository extends MongoRepository<Codigorecuperacion, String> {
<<<<<<< HEAD
    @Query("{ '_codigo' : ?0 }")
=======
    @Query("{'codigo': ?0}")
>>>>>>> alvaro
    Optional<Codigorecuperacion> findByCodigo(String codigo);
}
