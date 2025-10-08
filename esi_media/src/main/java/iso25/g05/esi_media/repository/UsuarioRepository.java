package iso25.g05.esi_media.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Usuario;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    @Query("{ '_email' : ?0 }")
    Optional<Usuario> findByEmail(String email);

    @Query("{ '_email' : ?0 }")
    boolean existsByEmail(String email);
}