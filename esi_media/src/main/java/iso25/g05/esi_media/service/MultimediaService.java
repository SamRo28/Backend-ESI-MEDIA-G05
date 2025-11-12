package iso25.g05.esi_media.service;

import iso25.g05.esi_media.dto.ContenidoDetalleDTO;
import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.exception.AccesoNoAutorizadoException;
import iso25.g05.esi_media.exception.PeticionInvalidaException;
import iso25.g05.esi_media.exception.RecursoNoEncontradoException;
import iso25.g05.esi_media.mapper.ContenidoMapper;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.Contenido;
import iso25.g05.esi_media.model.GestordeContenido;
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
        Usuario usuario = validarYObtenerUsuarioAutorizado(authHeaderOrToken);
        
        // Si es Gestor de Contenido, puede ver todos los contenidos sin restricciones
        if (usuario instanceof GestordeContenido) {
            return listarTodosLosContenidos(pageable, tipo);
        }
        
        // Si es Visualizador, aplicar las restricciones normales
        Visualizador visualizador = (Visualizador) usuario;
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
        // Usamos los métodos legacy esperados por los tests, cuyos @Query ya incluyen el caso {$exists:false}
        pagina = visualizador.isVip()
            ? contenidoRepository.findByEstadoTrueAndEdadvisualizacionLessThanEqual(edad, pageable)
            : contenidoRepository.findByEstadoTrueAndVipFalseAndEdadvisualizacionLessThanEqual(edad, pageable);
        }

        return pagina.map(ContenidoMapper::aResumen);
    }

    /**
     * Lista contenidos visibles y accesibles para el visualizador autenticado con búsqueda por texto.
     * 
     * Qué hace: valida al usuario (token), calcula su edad y devuelve un listado
     * paginado filtrado por visibilidad, edad mínima, VIP y opcionalmente por búsqueda de texto.
     * 
     * @param pageable parámetros de paginación (page, size, sort)
     * @param authHeaderOrToken cabecera Authorization o token en bruto
     * @param tipo filtro por tipo de contenido (AUDIO/VIDEO)
     * @param query texto de búsqueda para filtrar por título o descripción
     * @return página de contenidos en formato resumen
     */
    public Page<ContenidoResumenDTO> listarContenidos(Pageable pageable, String authHeaderOrToken, String tipo, String query) {
        Usuario usuario = validarYObtenerUsuarioAutorizado(authHeaderOrToken);
        
        // Si es Gestor de Contenido, puede ver todos los contenidos sin restricciones
        if (usuario instanceof GestordeContenido) {
            // Si hay query de búsqueda
            if (query != null && !query.trim().isEmpty()) {
                return buscarTodosLosContenidos(pageable, tipo, query.trim());
            }
            return listarTodosLosContenidos(pageable, tipo);
        }
        
        // Si es Visualizador, aplicar las restricciones normales
        Visualizador visualizador = (Visualizador) usuario;
        int edad = calcularEdad(visualizador.getFechaNac());

        // Si hay query de búsqueda, usar métodos específicos de búsqueda
        if (query != null && !query.trim().isEmpty()) {
            return buscarContenidosConFiltros(pageable, visualizador, edad, tipo, query.trim());
        }

        // Si no hay query, usar la lógica existente
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
     * Método auxiliar para buscar contenidos con texto y aplicar todos los filtros.
     * 
     * @param pageable parámetros de paginación
     * @param visualizador usuario autenticado
     * @param edad edad calculada del usuario
     * @param tipo filtro por tipo (AUDIO/VIDEO o null)
     * @param query texto de búsqueda
     * @return página de contenidos filtrados
     */
    private Page<ContenidoResumenDTO> buscarContenidosConFiltros(Pageable pageable, Visualizador visualizador, int edad, String tipo, String query) {
        Page<Contenido> pagina;
        
        if (tipo != null && !tipo.isBlank()) {
            if ("VIDEO".equalsIgnoreCase(tipo)) {
                pagina = visualizador.isVip()
                        ? contenidoRepository.searchVideos(query, edad, pageable)
                        : contenidoRepository.searchVideosNoVip(query, edad, pageable);
            } else if ("AUDIO".equalsIgnoreCase(tipo)) {
                pagina = visualizador.isVip()
                        ? contenidoRepository.searchAudios(query, edad, pageable)
                        : contenidoRepository.searchAudiosNoVip(query, edad, pageable);
            } else {
                // Tipo no reconocido, buscar en todo
                pagina = visualizador.isVip()
                        ? contenidoRepository.searchContenidos(query, edad, pageable)
                        : contenidoRepository.searchContenidosNoVip(query, edad, pageable);
            }
        } else {
            // Buscar en todos los tipos
            pagina = visualizador.isVip()
                    ? contenidoRepository.searchContenidos(query, edad, pageable)
                    : contenidoRepository.searchContenidosNoVip(query, edad, pageable);
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

        Usuario usuario = validarYObtenerUsuarioAutorizado(authHeaderOrToken);

        Optional<Contenido> opt;
        // Si es Gestor de Contenido, puede acceder a contenidos con cualquier estado
        if (usuario instanceof GestordeContenido) {
            opt = contenidoRepository.findByIdForGestor(id);
        } else {
            // Para Visualizadores, solo contenidos con estado true
            opt = contenidoRepository.findByIdAndEstadoTrue(id);
        }
        
        Contenido contenido = opt.orElseThrow(() -> new RecursoNoEncontradoException("Contenido no encontrado"));

        // Si es Gestor de Contenido, puede acceder sin restricciones
        if (usuario instanceof GestordeContenido) {
            String referencia = construirReferenciaReproduccion(contenido);
            return ContenidoMapper.aDetalle(contenido, referencia);
        }
        
        // Si es Visualizador, aplicar validaciones de acceso
        Visualizador visualizador = (Visualizador) usuario;
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
            // Usar URL absoluta para evitar que el <audio> la trate como ruta relativa en 4200
            return "http://localhost:8080/multimedia/audio/" + a.getId();
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

        Usuario usuario = validarYObtenerUsuarioAutorizado(authHeaderOrToken);

        Optional<Contenido> opt;
        // Si es Gestor de Contenido, puede acceder a contenidos con cualquier estado
        if (usuario instanceof GestordeContenido) {
            opt = contenidoRepository.findByIdForGestor(id);
        } else {
            // Para Visualizadores, solo contenidos con estado true
            opt = contenidoRepository.findByIdAndEstadoTrue(id);
        }
        
        Contenido contenido = opt.orElseThrow(() -> new RecursoNoEncontradoException("Contenido no encontrado"));

        if (!(contenido instanceof Audio audio)) {
            throw new PeticionInvalidaException("El contenido solicitado no es de tipo audio");
        }

        // Si es Gestor de Contenido, puede acceder sin restricciones
        if (usuario instanceof GestordeContenido) {
            return audio;
        }
        
        // Si es Visualizador, aplicar validaciones de acceso
        Visualizador visualizador = (Visualizador) usuario;
        validarAcceso(contenido, visualizador);
        return audio;
    }

    /**
     * Valida el token y devuelve el Usuario autorizado (Visualizador o GestordeContenido).
     * 
     * @param authHeaderOrToken cabecera Authorization o token en bruto
     * @return usuario autenticado (Visualizador o GestordeContenido)
     * @throws PeticionInvalidaException si no se manda token
     * @throws AccesoNoAutorizadoException si el token es inválido o no es un tipo de usuario autorizado
     */
    private Usuario validarYObtenerUsuarioAutorizado(String authHeaderOrToken) {
        String token = extraerToken(authHeaderOrToken);
        if (token == null || token.isBlank()) {
            throw new PeticionInvalidaException("Token de autorización requerido");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findBySesionToken(token);
        if (usuarioOpt.isEmpty()) {
            throw new AccesoNoAutorizadoException("Token no válido");
        }

        Usuario usuario = usuarioOpt.get();
        if (!(usuario instanceof Visualizador) && !(usuario instanceof GestordeContenido)) {
            throw new AccesoNoAutorizadoException("Solo los visualizadores y gestores de contenido pueden acceder a contenidos multimedia");
        }

        return usuario;
    }

    /**
     * Lista todos los contenidos sin restricciones de edad, VIP o estado (para Gestores de Contenido).
     * 
     * @param pageable parámetros de paginación
     * @param tipo filtro por tipo de contenido (AUDIO/VIDEO o null)
     * @return página de contenidos en formato resumen
     */
    private Page<ContenidoResumenDTO> listarTodosLosContenidos(Pageable pageable, String tipo) {
        Page<Contenido> pagina;
        boolean filtrar = (tipo != null && !tipo.isBlank());
        String className = null;
        if (filtrar) {
            if ("VIDEO".equalsIgnoreCase(tipo)) className = Video.class.getName();
            else if ("AUDIO".equalsIgnoreCase(tipo)) className = Audio.class.getName();
        }

        if (filtrar && className != null) {
            // Buscar por tipo específico SIN restricciones de estado (para Gestores)
            pagina = contenidoRepository.findAllContenidosByClassForGestor(className, pageable);
            // Si la página viene mezclada, usar fallback
            boolean mezclado = pagina.getContent().stream().anyMatch(c -> {
                boolean esVideoEsperado = "VIDEO".equalsIgnoreCase(tipo) && c instanceof Audio;
                boolean esAudioEsperado = "AUDIO".equalsIgnoreCase(tipo) && c instanceof Video;
                return esVideoEsperado || esAudioEsperado;
            });
            if (mezclado) {
                if ("VIDEO".equalsIgnoreCase(tipo)) {
                    pagina = contenidoRepository.findAllVideosForGestor(pageable);
                } else if ("AUDIO".equalsIgnoreCase(tipo)) {
                    pagina = contenidoRepository.findAllAudiosForGestor(pageable);
                }
            }
        } else {
            // Buscar TODOS los contenidos SIN restricciones de estado (para Gestores)
            pagina = contenidoRepository.findAllContenidosForGestor(pageable);
        }

        return pagina.map(ContenidoMapper::aResumen);
    }

    /**
     * Busca contenidos con texto sin restricciones de estado (para Gestores de Contenido).
     * 
     * @param pageable parámetros de paginación
     * @param tipo filtro por tipo (AUDIO/VIDEO o null)
     * @param query texto de búsqueda
     * @return página de contenidos filtrados
     */
    private Page<ContenidoResumenDTO> buscarTodosLosContenidos(Pageable pageable, String tipo, String query) {
        Page<Contenido> pagina;
        
        if (tipo != null && !tipo.isBlank()) {
            if ("VIDEO".equalsIgnoreCase(tipo)) {
                pagina = contenidoRepository.searchAllVideosForGestor(query, pageable);
            } else if ("AUDIO".equalsIgnoreCase(tipo)) {
                pagina = contenidoRepository.searchAllAudiosForGestor(query, pageable);
            } else {
                // Tipo no reconocido, buscar en todo
                pagina = contenidoRepository.searchAllContenidosForGestor(query, pageable);
            }
        } else {
            // Buscar en todos los tipos SIN restricciones de estado
            pagina = contenidoRepository.searchAllContenidosForGestor(query, pageable);
        }

        return pagina.map(ContenidoMapper::aResumen);
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

    /**
     * Registra una reproducción del contenido indicado, validando acceso del usuario.
     *
     * Contrato:
     * - Requiere token (Authorization o token en claro) y cumplir restricciones de edad/VIP si es visualizador.
     * - Incrementa en +1 el contador de nvisualizaciones y persiste el cambio.
     * - Devuelve el nuevo total de visualizaciones.
     */
    public int registrarReproduccion(String id, String authHeaderOrToken) {
        if (id == null || id.isBlank()) {
            throw new PeticionInvalidaException("El id de contenido es obligatorio");
        }

        Usuario usuario = validarYObtenerUsuarioAutorizado(authHeaderOrToken);

        Optional<Contenido> opt;
        // Gestor: puede acceder siempre; Visualizador: solo visibles
        if (usuario instanceof GestordeContenido) {
            opt = contenidoRepository.findByIdForGestor(id);
        } else {
            opt = contenidoRepository.findByIdAndEstadoTrue(id);
        }

        Contenido contenido = opt.orElseThrow(() -> new RecursoNoEncontradoException("Contenido no encontrado"));

        if (usuario instanceof Visualizador v) {
            validarAcceso(contenido, v);
        }

        // Incrementar contador de visualizaciones de forma simple
        int current = Math.max(0, contenido.getnvisualizaciones());
        contenido.setnvisualizaciones(current + 1);
        contenidoRepository.save(contenido);
        return contenido.getnvisualizaciones();
    }
}
