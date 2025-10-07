package iso25.g05.esi_media.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import iso25.g05.esi_media.model.Gestor_de_Contenido;

@Repository
public interface GestorDeContenidoRepository extends MongoRepository<Gestor_de_Contenido, String> {
    List<Gestor_de_Contenido> findBy_campo_especializacion(String campo);
}
