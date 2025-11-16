package iso25.g05.esi_media.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.repository.TokenRepository;

@Service
public class TokenForValoracionService {

    private final TokenRepository tokenRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public TokenForValoracionService(TokenRepository tokenRepository, MongoTemplate mongoTemplate) {
        this.tokenRepository = tokenRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Resuelve el id del usuario a partir del header Authorization.
     * Acepta tanto 'Bearer <token>' como el token en crudo.
     * Retorna null si no existe o no puede resolverse.
     */
    public String resolveUsuarioIdFromAuth(String authHeader) {
        if (authHeader == null) return null;
        String token = authHeader;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        Optional<Token> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            Token t = tokenOpt.get();
            // Comprobación básica de validez
            if (t.isExpirado()) return null;
            try {
                if (t.getFechaExpiracion() != null && t.getFechaExpiracion().before(new java.util.Date())) {
                    return null;
                }
            } catch (Exception ignore) {
                // Ignorar excepciones al comprobar la fecha de expiración
            }
            if (t.getUsuario() == null) return null;
            return t.getUsuario().getId();
        }

        // Fallback: Buscamos en la colección `users` si existe.
        try {
            Query q = new Query(Criteria.where("sesionstoken.token").is(token));
            Usuario u = mongoTemplate.findOne(q, Usuario.class, "users");
            if (u != null) {
                try {
                    if (u.getSesionstoken() != null) {
                        Token st = u.getSesionstoken();
                        if (st.isExpirado()) return null;
                        if (st.getFechaExpiracion() != null && st.getFechaExpiracion().before(new java.util.Date())) {
                            return null;
                        }
                    }
                } catch (Exception ignore) {
                    // Ignorar excepciones al comprobar la sesionstoken del usuario
                }
                return u.getId();
            }
        } catch (Exception ex) {
            // noop - si falla el fallback, devolvemos null y el controlador responderá 401
        }

        return null;
    }
}
