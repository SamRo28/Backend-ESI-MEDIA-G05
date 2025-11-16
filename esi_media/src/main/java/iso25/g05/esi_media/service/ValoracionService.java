package iso25.g05.esi_media.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import iso25.g05.esi_media.exception.RecursoNoEncontradoException;
import iso25.g05.esi_media.model.Contenido;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.model.Valoracion;
import iso25.g05.esi_media.repository.ContenidoRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.repository.ValoracionRepository;

@Service
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;
    private final ContenidoRepository contenidoRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public ValoracionService(ValoracionRepository valoracionRepository,
                             ContenidoRepository contenidoRepository,
                             UsuarioRepository usuarioRepository) {
        this.valoracionRepository = valoracionRepository;
        this.contenidoRepository = contenidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Registra la reproducción (play) creando la asociación Valoracion
     * con valor null si no existía previamente. Idempotente.
     */
    public Valoracion registerPlay(String visualizadorId, String contenidoId) {
        // Verificar existencia de contenido y usuario
        Optional<Contenido> contenidoOpt = contenidoRepository.findById(contenidoId);
        if (contenidoOpt.isEmpty()) {
            throw new RecursoNoEncontradoException("Contenido no encontrado");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(visualizadorId);
        if (usuarioOpt.isEmpty()) {
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }

        Optional<Valoracion> existing = valoracionRepository.findByVisualizadorIdAndContenidoId(visualizadorId, contenidoId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Valoracion v = new Valoracion();
        v.setVisualizadorId(visualizadorId);
        v.setContenidoId(contenidoId);
        v.setValoracionFinal(null);

        try {
            return valoracionRepository.save(v);
        } catch (DuplicateKeyException ex) {
            // En caso de carrera, recuperar el existente
            return valoracionRepository.findByVisualizadorIdAndContenidoId(visualizadorId, contenidoId)
                    .orElseThrow(() -> ex);
        }
    }

    /**
     * Establece la valoración para un par visualizador-contenido.
     * Reglas principales implementadas:
     * - El contenido debe existir
     * - El usuario debe existir y ser Visualizador
     * - Debe existir la asociación previa (haber reproducido)
     * - Solo se permite valorar si la valoración actual es null (no se permite modificar)
     * - El score debe estar en [1.0,5.0] y ser múltiplo de 0.5
     */
    public Valoracion rateContent(String visualizadorId, String contenidoId, Double score) {
        // Buscar asociación existente entre visualizador y contenido
        Optional<Valoracion> valoracionOpt = valoracionRepository.findByVisualizadorIdAndContenidoId(visualizadorId, contenidoId);

        if (valoracionOpt.isPresent()) {
            // Si ya existe la asociación, validar usuario y estado de la valoración
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(visualizadorId);
            if (usuarioOpt.isEmpty()) {
                throw new IllegalStateException("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();
            if (!(usuario instanceof Visualizador)) {
                throw new IllegalStateException("Usuario no autorizado para valorar");
            }

            Valoracion valoracion = valoracionOpt.get();
        if (valoracion.getValoracionFinal() != null) {
            throw new IllegalStateException("Valoración ya realizada; no se puede modificar");
        }

        // Validar score
        if (score == null || score < 1.0 || score > 5.0) {
            throw new IllegalArgumentException("Puntuación fuera de rango (1.0 - 5.0)");
        }
        double times2 = score * 2;
        if (Math.abs(Math.rint(times2) - times2) > 1e-9) {
            throw new IllegalArgumentException("Puntuación debe ser múltiplo de 0.5");
        }

            // Guardar valoración
            valoracion.setValoracionFinal(score);
            return valoracionRepository.save(valoracion);
        }

        // Si no existe la asociación, comprobamos primero que el contenido exista
        Optional<Contenido> contenidoOpt = contenidoRepository.findById(contenidoId);
        if (contenidoOpt.isEmpty()) {
            throw new RecursoNoEncontradoException("Contenido no encontrado");
        }

        // Verificar usuario y tipo para poder informar de ausencia de asociación
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(visualizadorId);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalStateException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        if (!(usuario instanceof Visualizador)) {
            throw new IllegalStateException("Usuario no autorizado para valorar");
        }

        // No existe asociación => no puede valorar
        throw new IllegalStateException("No se puede valorar sin reproducir antes");
    }
}
