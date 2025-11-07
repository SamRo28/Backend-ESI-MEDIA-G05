package iso25.g05.esi_media.service;

import iso25.g05.esi_media.dto.ContenidoDetalleDTO;
import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.exception.AccesoNoAutorizadoException;
import iso25.g05.esi_media.exception.PeticionInvalidaException;
import iso25.g05.esi_media.exception.RecursoNoEncontradoException;
import iso25.g05.esi_media.mapper.ContenidoMapper;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.Contenido;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Video;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.ContenidoRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * Servicio de lectura y reproducción de contenidos multimedia para visualizadores.
 * 
 * Objetivo: ofrecer un punto único de negocio para listar contenidos accesibles
 * (según visibilidad, edad y VIP) y obtener el detalle listo para reproducir.
 * 
 * Por qué existe: encapsula reglas (edad/VIP/visibilidad) y reduce duplicación
 * en controladores, manteniendo el código simple y testeable.
 */
@Service
public class MultimediaService {

    @Autowired
    private ContenidoRepository contenidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Lista contenidos visibles y accesibles para el visualizador autenticado.
     * 
     * Qué hace: valida al usuario (token), calcula su edad y devuelve un listado
     * paginado filtrado por visibilidad, edad mínima y, si no es VIP, excluye VIP.
     * 
     * Por qué: así evitamos enviar elementos que el usuario no puede reproducir
     * y mantenemos la respuesta ligera y paginada.
     * 
     * @param pageable parámetros de paginación (page, size, sort)
     * @param authHeaderOrToken cabecera Authorization o token en bruto
     * @return página de contenidos en formato resumen
     * @throws PeticionInvalidaException si falta token
     * @throws AccesoNoAutorizadoException si el token no es válido o no es visualizador
     */
    public Page<ContenidoResumenDTO> listarContenidos(Pageable pageable, String authHeaderOrToken, String tipo) {
        Visualizador visualizador = validarYObtenerVisualizador(authHeaderOrToken);
        int edad = calcularEdad(visualizador.getFechaNac());

        Page<Contenido> pagina;
        boolean filtrar = (tipo != null && !tipo.isBlank());
        String className = null;
        if (filtrar) {
            if ("VIDEO".equalsIgnoreCase(tipo)) className = Video.class.getName();
            else if ("AUDIO".equalsIgnoreCase(tipo)) className = Audio.class.getName();
        }

        if (filtrar && className != null) {
            // Intento 1: filtrado por _class exacto
            pagina = visualizador.isVip()
                    ? contenidoRepository.findByEstadoTrueAndEdadvisualizacionLessThanEqualAndClass(edad, className, pageable)
                    : contenidoRepository.findByEstadoTrueAndVipFalseAndEdadvisualizacionLessThanEqualAndClass(edad, className, pageable);
            // Si la página viene mezclada (heurística simple), usar fallback por campos característicos
            boolean mezclado = pagina.getContent().stream().anyMatch(c -> {
                boolean esVideoEsperado = "VIDEO".equalsIgnoreCase(tipo) && c instanceof Audio;
                boolean esAudioEsperado = "AUDIO".equalsIgnoreCase(tipo) && c instanceof Video;
                return esVideoEsperado || esAudioEsperado;
            });
            if (mezclado) {
                if ("VIDEO".equalsIgnoreCase(tipo)) {
                    pagina = visualizador.isVip()
                            ? contenidoRepository.findVideos(edad, pageable)
                            : contenidoRepository.findVideosNoVip(edad, pageable);
                } else if ("AUDIO".equalsIgnoreCase(tipo)) {
                    pagina = visualizador.isVip()
                            ? contenidoRepository.findAudios(edad, pageable)
                            : contenidoRepository.findAudiosNoVip(edad, pageable);
                }
            }
        } else {
            pagina = visualizador.isVip()
                    ? contenidoRepository.findByEstadoTrueAndEdadvisualizacionLessThanEqual(edad, pageable)
                    : contenidoRepository.findByEstadoTrueAndVipFalseAndEdadvisualizacionLessThanEqual(edad, pageable);
        }

        return pagina.map(ContenidoMapper::aResumen);
    }

    /**
     * Sobrecarga para compatibilidad con código y tests existentes que no pasan parámetro tipo.
     * Delegamos en la versión extendida con tipo = null (sin filtrado por clase en repositorio).
     */
    public Page<ContenidoResumenDTO> listarContenidos(Pageable pageable, String authHeaderOrToken) {
        return listarContenidos(pageable, authHeaderOrToken, null);
    }

    /**
     * Obtiene el detalle de un contenido, validando visibilidad, edad y VIP.
     * 
     * Qué hace: comprueba que el contenido existe y está visible, verifica que el
     * visualizador cumple edad y/o VIP, y construye la referencia de reproducción
     * (URL externa para vídeo o endpoint interno para audio).
     * 
     * Por qué: centraliza las reglas de acceso para evitar duplicaciones.
     * 
     * @param id identificador del contenido
     * @param authHeaderOrToken cabecera Authorization o token en bruto
     * @return DTO de detalle con la referencia de reproducción
     * @throws PeticionInvalidaException si el id es nulo o vacío
     * @throws RecursoNoEncontradoException si el contenido no existe o no está visible
     * @throws AccesoNoAutorizadoException si no cumple edad o no es VIP
     */
    public ContenidoDetalleDTO obtenerContenidoPorId(String id, String authHeaderOrToken) {
        if (id == null || id.isBlank()) {
            throw new PeticionInvalidaException("El id de contenido es obligatorio");
        }

        Visualizador visualizador = validarYObtenerVisualizador(authHeaderOrToken);

        Optional<Contenido> opt = contenidoRepository.findByIdAndEstadoTrue(id);
        Contenido contenido = opt.orElseThrow(() -> new RecursoNoEncontradoException("Contenido no encontrado"));

        validarAcceso(contenido, visualizador);

        String referencia = construirReferenciaReproduccion(contenido);
        return ContenidoMapper.aDetalle(contenido, referencia);
    }

    /**
     * Valida que el visualizador puede acceder al contenido: edad y VIP.
     * 
     * Qué hace: compara la edad del usuario con la edad mínima del contenido
     * y exige VIP cuando el contenido está marcado como VIP.
     * 
     * Por qué: aseguramos que solo se accede a contenidos permitidos.
     * 
     * @param contenido entidad de contenido a validar
     * @param visualizador usuario autenticado que intenta acceder
     * @throws AccesoNoAutorizadoException si no cumple requisitos
     */
    public void validarAcceso(Contenido contenido, Visualizador visualizador) {
        // Edad mínima
        int edadMin = contenido.getedadvisualizacion();
        int edadUsuario = calcularEdad(visualizador.getFechaNac());
        if (edadMin > 0 && edadUsuario < edadMin) {
            throw new AccesoNoAutorizadoException("Contenido restringido por edad");
        }

        // VIP
        if (contenido.isvip() && !visualizador.isVip()) {
            throw new AccesoNoAutorizadoException("Contenido disponible solo para usuarios VIP");
        }
    }

    /**
     * Construye la referencia de reproducción dependiendo del tipo.
     * 
     * Qué hace: retorna la URL externa para vídeo (para embeber en el frontend)
     * o el endpoint interno para audio (stream desde nuestro backend).
     * 
     * Por qué: el frontend necesita una referencia única y directa para reproducir.
     * 
     * Nota sobre vídeos: aunque el archivo no esté en BD, se reproducen en la
     * propia aplicación embebiendo la URL (iframe/player) del proveedor externo.
     * 
     * @param contenido entidad de contenido (Audio o Video)
     * @return referencia reproducible para el cliente
     */
    public String construirReferenciaReproduccion(Contenido contenido) {
        if (contenido instanceof Video v) {
            return v.geturl();
        }
        if (contenido instanceof Audio a) {
            return "/multimedia/audio/" + a.getId();
        }
        return ""; // por defecto vacío si no es tipo reconocido
    }

    /**
     * Valida acceso del visualizador y devuelve el Audio listo para streaming.
     *
     * @param id id del contenido Audio
     * @param authHeaderOrToken Authorization ("Bearer x" o token) o token en bruto
     * @return entidad Audio con el fichero binario
     * @throws PeticionInvalidaException si id es inválido o el contenido no es audio
     * @throws RecursoNoEncontradoException si no existe o no está visible
     * @throws AccesoNoAutorizadoException si no cumple edad/VIP o token inválido
     */
    public Audio validarYObtenerAudioParaStreaming(String id, String authHeaderOrToken) {
        if (id == null || id.isBlank()) {
            throw new PeticionInvalidaException("El id de contenido es obligatorio");
        }

        Visualizador visualizador = validarYObtenerVisualizador(authHeaderOrToken);

        Optional<Contenido> opt = contenidoRepository.findByIdAndEstadoTrue(id);
        Contenido contenido = opt.orElseThrow(() -> new RecursoNoEncontradoException("Contenido no encontrado"));

        if (!(contenido instanceof Audio audio)) {
            throw new PeticionInvalidaException("El contenido solicitado no es de tipo audio");
        }

        validarAcceso(contenido, visualizador);
        return audio;
    }

    /**
     * Valida el token (con o sin prefijo Bearer) y devuelve el Visualizador.
     * 
     * Qué hace: extrae el valor del token, busca al usuario por sesión y exige
     * que sea un Visualizador para continuar.
     * 
     * Por qué: solo los visualizadores están habilitados para consumir multimedia.
     * 
     * @param authHeaderOrToken cabecera Authorization o token en bruto
     * @return visualizador autenticado
     * @throws PeticionInvalidaException si no se manda token
     * @throws AccesoNoAutorizadoException si el token es inválido o no es visualizador
     */
    private Visualizador validarYObtenerVisualizador(String authHeaderOrToken) {
        String token = extraerToken(authHeaderOrToken);
        if (token == null || token.isBlank()) {
            throw new PeticionInvalidaException("Token de autorización requerido");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findBySesionToken(token);
        if (usuarioOpt.isEmpty()) {
            throw new AccesoNoAutorizadoException("Token no válido");
        }

        Usuario usuario = usuarioOpt.get();
        if (!(usuario instanceof Visualizador visualizador)) {
            throw new AccesoNoAutorizadoException("Solo los visualizadores pueden acceder a contenidos multimedia");
        }

        return visualizador;
    }

    /**
     * Extrae el token del header o retorna el valor tal cual si ya es un token.
     * 
     * Qué hace: soporta formatos "Bearer xyz" o el token limpio "xyz".
     * 
     * Por qué: robustez frente a distintos clientes que envían el token.
     * 
     * @param headerOrToken cabecera Authorization o token en bruto
     * @return token extraído listo para buscar en BD
     */
    private String extraerToken(String headerOrToken) {
        if (headerOrToken == null) return null;
        String v = headerOrToken.trim();
        if (v.toLowerCase().startsWith("bearer ")) {
            return v.substring(7).trim();
        }
        return v;
    }

    /**
     * Calcula la edad en años a partir de una fecha de nacimiento.
     * 
     * Qué hace: convierte Date a LocalDate y calcula años transcurridos.
     * 
     * Por qué: se usa para aplicar restricciones de edad en los contenidos.
     * Política: si la fecha es nula, devuelve 200 (no bloquear por defecto).
     * 
     * @param fechaNac fecha de nacimiento del usuario
     * @return edad calculada en años
     */
    private int calcularEdad(Date fechaNac) {
        if (fechaNac == null) return 200;
        LocalDate birth = fechaNac.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(birth, LocalDate.now()).getYears();
    }
}
