package iso25.g05.esi_media.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import iso25.g05.esi_media.dto.ContenidoDetalleDTO;
import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.dto.ContenidoUpdateDTO;
import iso25.g05.esi_media.exception.AccesoNoAutorizadoException;
import iso25.g05.esi_media.exception.PeticionInvalidaException;
import iso25.g05.esi_media.exception.RecursoNoEncontradoException;
import iso25.g05.esi_media.mapper.ContenidoMapper;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.Contenido;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Video;
import iso25.g05.esi_media.repository.ContenidoRepository;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;

/**
 * Servicio de gestión de contenidos específico para Gestores de Contenido.
 *
 * Permite listar, consultar detalle, actualizar y eliminar contenidos
 * respetando las restricciones de edición y permisos del rol Gestor.
 */
@Service
public class GestorContenidoService {

    private static final String TIPO_VIDEO = "VIDEO";
    private static final String TIPO_AUDIO = "AUDIO";
    private static final String CONTENIDO_NO_ENCONTRADO = "Contenido no encontrado";
    private static final String OPERACION_NO_PERMITIDA = "Operación no permitida";
    private static final String TOKEN_REQUERIDO = "Token de autorización requerido";

    @Autowired
    private ContenidoRepository contenidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GestorDeContenidoRepository gestorRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Lista todos los contenidos gestionables por el Gestor autenticado.
     * Incluye contenidos visibles y no visibles, de audio y vídeo.
     */
    public Page<ContenidoResumenDTO> listar(String authHeaderOrToken,
                                            Pageable pageable,
                                            String tipo,
                                            String query) {
        GestordeContenido gestor = validarYObtenerGestor(authHeaderOrToken);

        Page<Contenido> pagina;
        if (query != null && !query.isBlank()) {
            pagina = buscarContenidosGestorPorTipo(tipo, query, pageable);
        } else {
            pagina = listarContenidosGestorPorTipo(tipo, pageable);
        }

        logService.registrarAccion("Listado de contenidos por gestor", gestor.getEmail());
        return pagina.map(ContenidoMapper::aResumen);
    }

    /**
     * Obtiene el detalle completo de un contenido para gestión.
     */
    public ContenidoDetalleDTO detalle(String id, String authHeaderOrToken) {
        GestordeContenido gestor = validarYObtenerGestor(authHeaderOrToken);

        Contenido contenido = contenidoRepository.findByIdForGestor(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(CONTENIDO_NO_ENCONTRADO));

        String referencia = construirReferenciaReproduccion(contenido);
        ContenidoDetalleDTO dto = ContenidoMapper.aDetalle(contenido, referencia);

        // Añadir información del creador
        if (contenido.getcreadorId() != null) {
            usuarioRepository.findById(contenido.getcreadorId()).ifPresentOrElse(creador -> {
                dto.setCreadorNombre(creador.getNombre());
                dto.setCreadorApellidos(creador.getApellidos());
            }, () -> {
                dto.setCreadorNombre(null); // El creador fue eliminado
            });
        }

        // Añadir fecha de creación
        dto.setFechaCreacion(contenido.getfechaCreacion());

        logService.registrarAccion("Consulta detalle contenido " + contenido.getId(), gestor.getEmail());
        return dto;
    }

    /**
     * Actualiza los campos editables de un contenido.
     */
    public ContenidoDetalleDTO actualizar(String id,
                                          ContenidoUpdateDTO dto,
                                          String authHeaderOrToken) {
        GestordeContenido gestor = validarYObtenerGestor(authHeaderOrToken);

        Contenido contenido = contenidoRepository.findByIdForGestor(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(CONTENIDO_NO_ENCONTRADO));

        validarPermisosTipo(gestor, contenido);

        // Aplicar cambios permitidos
        contenido.settitulo(dto.getTitulo());
        contenido.setdescripcion(dto.getDescripcion());
        contenido.settags(dto.getTags());
        contenido.setvip(Boolean.TRUE.equals(dto.getVip()));

        boolean estadoActual = contenido.isestado();
        boolean nuevoEstado = Boolean.TRUE.equals(dto.getEstado());
        contenido.setestado(nuevoEstado);

        if (dto.getEdadVisualizacion() != null) {
            contenido.setedadvisualizacion(dto.getEdadVisualizacion());
        }
        contenido.setfechadisponiblehasta(dto.getFechaDisponibleHasta());

        if (dto.getCaratula() != null) {
            contenido.setcaratula(dto.getCaratula());
        }

        // Si pasa a no visible, actualizar fechaestadoautomatico
        if (estadoActual && !nuevoEstado) {
            contenido.setfechaestadoautomatico(new Date());
        }

        // Regla: contenido estándar no puede tener resolución 4K
        if (contenido instanceof Video v && !contenido.isvip()) {
            String resolucion = v.getresolucion();
            if (resolucion != null && "4k".equalsIgnoreCase(resolucion)) {
                throw new PeticionInvalidaException(OPERACION_NO_PERMITIDA);
            }
        }

        contenidoRepository.save(contenido);

        logService.registrarAccion("Actualización de contenido " + contenido.getId(), gestor.getEmail());

        String referencia = construirReferenciaReproduccion(contenido);
        return ContenidoMapper.aDetalle(contenido, referencia);
    }

    /**
     * Elimina un contenido gestionable por el Gestor.
     */
    public void eliminar(String id, String authHeaderOrToken) {
        GestordeContenido gestor = validarYObtenerGestor(authHeaderOrToken);

        Contenido contenido = contenidoRepository.findByIdForGestor(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(CONTENIDO_NO_ENCONTRADO));

        validarPermisosTipo(gestor, contenido);

        contenidoRepository.delete(contenido);

        // Registrar eliminación en log para trazabilidad
        logService.registrarAccion("Eliminación de contenido " + contenido.getId(), gestor.getEmail());
    }

    /**
     * Devuelve una lista única y ordenada de todos los tags existentes en la plataforma.
     */
    public List<String> obtenerTodosLosTags(String authHeaderOrToken) {
        validarYObtenerGestor(authHeaderOrToken);
        List<String> tags = mongoTemplate.query(Contenido.class).distinct("tags").as(String.class).all();
        return tags.stream().sorted().collect(Collectors.toList());
    }



    // ====================== MÉTODOS PRIVADOS =========================

    private GestordeContenido validarYObtenerGestor(String authHeaderOrToken) {
        String token = extraerToken(authHeaderOrToken);
        if (token == null || token.isBlank()) {
            throw new PeticionInvalidaException(TOKEN_REQUERIDO);
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findBySesionToken(token);
        if (usuarioOpt.isEmpty()) {
            throw new AccesoNoAutorizadoException(OPERACION_NO_PERMITIDA);
        }

        Usuario usuario = usuarioOpt.get();
        if (!(usuario instanceof GestordeContenido gestor)) {
            throw new AccesoNoAutorizadoException(OPERACION_NO_PERMITIDA);
        }

        if (gestor.isBloqueado()) {
            throw new AccesoNoAutorizadoException(OPERACION_NO_PERMITIDA);
        }

        // Asegurarse de tener la versión más reciente del gestor
        return gestorRepository.findById(gestor.getId()).orElse(gestor);
    }

    private void validarPermisosTipo(GestordeContenido gestor, Contenido contenido) {
        String tipoGestor = gestor.gettipocontenidovideooaudio();

        if (contenido instanceof Video && !"video".equalsIgnoreCase(tipoGestor)) {
            throw new AccesoNoAutorizadoException(OPERACION_NO_PERMITIDA);
        }
        if (contenido instanceof Audio && !"audio".equalsIgnoreCase(tipoGestor)) {
            throw new AccesoNoAutorizadoException(OPERACION_NO_PERMITIDA);
        }
    }

    private String extraerToken(String headerOrToken) {
        if (headerOrToken == null) {
            return null;
        }
        String v = headerOrToken.trim();
        if (v.toLowerCase().startsWith("bearer ")) {
            return v.substring(7).trim();
        }
        return v;
    }

    private Page<Contenido> listarContenidosGestorPorTipo(String tipo, Pageable pageable) {
        if (tipo == null || tipo.isBlank()) {
            return contenidoRepository.findAllContenidosForGestor(pageable);
        }
        if (TIPO_VIDEO.equalsIgnoreCase(tipo)) {
            return contenidoRepository.findAllVideosForGestor(pageable);
        }
        if (TIPO_AUDIO.equalsIgnoreCase(tipo)) {
            return contenidoRepository.findAllAudiosForGestor(pageable);
        }
        return contenidoRepository.findAllContenidosForGestor(pageable);
    }

    private Page<Contenido> buscarContenidosGestorPorTipo(String tipo, String query, Pageable pageable) {
        if (tipo == null || tipo.isBlank()) {
            return contenidoRepository.searchAllContenidosForGestor(query, pageable);
        }
        if (TIPO_VIDEO.equalsIgnoreCase(tipo)) {
            return contenidoRepository.searchAllVideosForGestor(query, pageable);
        }
        if (TIPO_AUDIO.equalsIgnoreCase(tipo)) {
            return contenidoRepository.searchAllAudiosForGestor(query, pageable);
        }
        return contenidoRepository.searchAllContenidosForGestor(query, pageable);
    }

    private String construirReferenciaReproduccion(Contenido contenido) {
        if (contenido instanceof Video v) {
            return v.getUrl();
        }
        if (contenido instanceof Audio a) {
            return "http://localhost:8080/multimedia/audio/" + a.getId();
        }
        return "";
    }
}
