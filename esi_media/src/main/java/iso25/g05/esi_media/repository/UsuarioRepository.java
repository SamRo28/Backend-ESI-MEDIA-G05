package iso25.g05.esi_media.repository;

import iso25.g05.esi_media.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    @Query("{'email': ?0}")
    Optional<Usuario> findByEmail(String email);
    
    @Query(value = "{'email': ?0}", exists = true)
    boolean existsByEmail(String email);
}