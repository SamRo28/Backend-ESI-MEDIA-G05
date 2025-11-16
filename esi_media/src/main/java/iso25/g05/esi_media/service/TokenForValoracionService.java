package iso25.g05.esi_media.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.repository.TokenRepository;

@Service
public class TokenForValoracionService {

    private final TokenRepository tokenRepository;

    @Autowired
    public TokenForValoracionService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
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
        if (tokenOpt.isEmpty()) return null;
        Token t = tokenOpt.get();
        if (t.getUsuario() == null) return null;
        return t.getUsuario().getId();
    }
}
