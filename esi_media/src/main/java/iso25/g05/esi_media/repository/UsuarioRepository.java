package iso25.g05.esi_media.repository;

import iso25.g05.esi_media.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findBy_email(String email);
    boolean existsBy_email(String email);
}