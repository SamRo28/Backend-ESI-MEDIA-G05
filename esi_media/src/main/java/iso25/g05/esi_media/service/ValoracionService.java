package iso25.g05.esi_media.service;

import java.util.List;
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
import iso25.g05.esi_media.dto.ShowRatingDTO;
import iso25.g05.esi_media.dto.AverageRatingDTO;

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
     * Registra la reproducción para crear la instancia de clase asociación Valoracion
     * con valor null si no existía previamente.
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
     * Muestra información agregada de valoración para un contenido y el estado
     * de la instancia del usuario (si se proporciona visualizadorId).
     * Devuelve el promedio de valoraciones existentes (null si no hay ninguna),
     * cantidad de valoraciones y, si el usuario proporcionado tiene una
     * instancia Valoracion, su valor (null si aún no valoró).
     */
    public ShowRatingDTO showRating(String visualizadorId, String contenidoId) {
        // Mostrar únicamente la valoración del usuario indicado (myRating)
        if (visualizadorId == null) {
            // Este método asume que el controlador ya verificó autenticación; si no, lanzamos una excepción
            throw new IllegalStateException("visualizadorId requerido");
        }

        Optional<Contenido> contenidoOpt = contenidoRepository.findById(contenidoId);
        if (contenidoOpt.isEmpty()) {
            throw new RecursoNoEncontradoException("Contenido no encontrado");
        }

        Optional<Valoracion> opt = valoracionRepository.findByVisualizadorIdAndContenidoId(visualizadorId, contenidoId);
        if (opt.isEmpty()) {
            // No hay instancia => el controlador decidirá devolver 404
            return new ShowRatingDTO(null);
        }

        return new ShowRatingDTO(opt.get().getValoracionFinal());
    }

    /**
     * Obtiene el promedio y cantidad de valoraciones para un contenido (solo agregados).
     */
    public AverageRatingDTO getAverageRating(String contenidoId) {
        Optional<Contenido> contenidoOpt = contenidoRepository.findById(contenidoId);
        if (contenidoOpt.isEmpty()) {
            throw new RecursoNoEncontradoException("Contenido no encontrado");
        }

        List<Valoracion> todas = valoracionRepository.findByContenidoId(contenidoId);
        long count = todas.stream().filter(v -> v.getValoracionFinal() != null).count();
        Double average = null;
        if (count > 0) {
            double sum = todas.stream()
                    .filter(v -> v.getValoracionFinal() != null)
                    .mapToDouble(v -> v.getValoracionFinal())
                    .sum();
            average = sum / count;
        }

        return new AverageRatingDTO(average, count);
    }

    /**
     * Devuelve la instancia Valoracion del usuario para el contenido si existe.
     * - Si el contenido no existe lanza RecursoNoEncontradoException
     * - Si no existe la instancia devuelve Optional.empty()
     */
    public Optional<Valoracion> getMyValoracionInstance(String visualizadorId, String contenidoId) {
        Optional<Contenido> contenidoOpt = contenidoRepository.findById(contenidoId);
        if (contenidoOpt.isEmpty()) {
            throw new RecursoNoEncontradoException("Contenido no encontrado");
        }

        return valoracionRepository.findByVisualizadorIdAndContenidoId(visualizadorId, contenidoId);
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
