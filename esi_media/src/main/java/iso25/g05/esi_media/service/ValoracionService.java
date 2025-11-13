package iso25.g05.esi_media.service;

import org.springframework.stereotype.Service;

import iso25.g05.esi_media.model.Valoracion;

@Service
public class ValoracionService {

    public ValoracionService() {
        // Stub constructor
    }

    public Valoracion registerFirstPlay(String visualizadorId, String contenidoId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Valoracion rateContent(String visualizadorId, String contenidoId, Double score) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
