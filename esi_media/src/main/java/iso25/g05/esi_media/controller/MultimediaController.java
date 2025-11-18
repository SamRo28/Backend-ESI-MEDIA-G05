package iso25.g05.esi_media.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iso25.g05.esi_media.dto.ContenidoDetalleDTO;
import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.service.MultimediaService;

/**
 * Controlador REST para reproducción/listado de contenido multimedia por visualizadores.
 *
 * Qué resuelve:
 * - Exponer un catálogo paginado de contenidos visibles para el usuario autenticado.
 * - Entregar el detalle listo para reproducir (URL de vídeo o endpoint de audio).
 * - Servir audio binario con soporte de rangos HTTP (avance rápido/continuación) y validación de acceso.
 *
 * Endpoints:
 *  - GET /multimedia                → lista paginada de ContenidoResumenDTO
 *  - GET /multimedia/{id}          → detalle (ContenidoDetalleDTO)
 *  - GET /multimedia/audio/{id}    → streaming de audio (.mp3) con Range (206)
 */
@RestController
@RequestMapping("/multimedia")
@CrossOrigin(origins = "*")
public class MultimediaController {

    private static final String MSG = "mensaje";

    @Autowired
    private MultimediaService multimediaService;

    /**
     * GET /multimedia
     * Lista contenidos accesibles para el visualizador autenticado (paginado).
     *
     * Contrato:
     * - Seguridad: requiere header Authorization con token ("Bearer <token>" o el token directamente).
     * - Paginación: soporta parámetros estándar de Spring (page, size, sort).
     * - Visibilidad: respeta estado, edad mínima y filtrado por VIP según el usuario (no-VIP no ve VIP).
     *
     * @param pageable parámetros de paginación inyectados por Spring
     * @param authHeader cabecera Authorization con el token de sesión
     * @return Page de ContenidoResumenDTO
     */
    @GetMapping
    public ResponseEntity<?> listarContenidos(
            Pageable pageable,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "query", required = false) String query) {

        if (authHeader == null || authHeader.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(MSG, "No autenticado"));
        }
        try {
            Page<ContenidoResumenDTO> pagina = multimediaService.listarContenidos(pageable, authHeader, tipo, query);
            return ResponseEntity.ok(pagina);
        } catch (Exception e) {
            // Fallback genérico para evitar 500 sin mensaje claro
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        MSG, "Error interno del servidor",
                        "detalle", e.getMessage() != null ? e.getMessage() : ""));
        }
    }

    /**
     * GET /multimedia/{id}
     * Devuelve detalle del contenido validando edad/VIP/visibilidad.
     *
     * Contrato:
     * - Seguridad: requiere Authorization (Bearer/limpio) y ser Visualizador válido.
     * - Reglas: aplica restricciones de edad y VIP; 403 si no cumple.
     * - Respuesta: para vídeo, una URL externa; para audio, la ruta del endpoint de streaming.
     *
     * @param id identificador del contenido
     * @param authHeader cabecera Authorization con token
     * @return ContenidoDetalleDTO con referencia de reproducción
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContenidoDetalleDTO> obtenerDetalle(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {

        ContenidoDetalleDTO detalle = multimediaService.obtenerContenidoPorId(id, authHeader);
        return ResponseEntity.ok(detalle);
    }

    /**
     * POST /multimedia/{id}/reproducir
     * Incrementa en 1 el contador de visualizaciones del contenido indicado.
     * Requiere autenticación y validar acceso (edad/VIP para visualizadores).
     * Devuelve el nuevo total de visualizaciones.
     */
    @PostMapping("/{id}/reproducir")
    public ResponseEntity<?> registrarReproduccion(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(MSG, "No autenticado"));
        }

        try {
            int total = multimediaService.registrarReproduccion(id, authHeader);
            return ResponseEntity.ok(Map.of("nvisualizaciones", total));
        } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(MSG, "Error al registrar reproducción", "detalle", e.getMessage() != null ? e.getMessage() : ""));
        }
    }

    /**
     * GET /multimedia/audio/{id}
     * Descarga/streaming del audio completo (sin soporte de rangos HTTP por ahora) y con validación de acceso.
     *
     * Por qué existe (explicación sencilla):
     * - Cuando el usuario quiera escuchar un audio descargará el archivo completo.
     * - Antes de enviar nada, comprobamos que el usuario está autorizado (token válido, edad mínima y VIP si aplica).
     *
     * Cómo funciona (paso a paso):
     * - Validamos que el id corresponde a un contenido visible y de tipo audio, y que el usuario puede acceder.
     * - Leemos el binario completo del audio desde BD.
     * - Respondemos 200 OK con todo el archivo y las cabeceras {@code Content-Type} y {@code Content-Length}.
     * - Ignoramos la cabecera {@code Range} si el cliente la enviase: siempre devolvemos el audio completo.
     *
     * Seguridad y reglas:
     * - Requiere cabecera {@code Authorization} (acepta {@code Bearer <token>} o el token en claro).
     * - Solo usuarios del tipo Visualizador pueden reproducir contenidos.
     * - Se comprueba edad mínima del contenido y condición VIP (si el contenido es VIP, el usuario también debe serlo).
     *
     * Errores típicos:
     * - 400: el id es vacío o el recurso encontrado no es de tipo audio.
     * - 403: token inválido/no autorizado o no cumple edad/VIP.
     * - 404: audio no encontrado o sin binario almacenado.
     *
     * @param id id del contenido de audio
     * @param authHeader cabecera Authorization con token (Bearer o en claro)
     * @return bytes del audio completo con cabeceras apropiadas
     */
    @GetMapping("/audio/{id}")
    public ResponseEntity<byte[]> streamAudio(
        @PathVariable String id,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestParam(value = "auth", required = false) String authQueryParam) {

        /*
         * Paso 1: Validaciones previas de acceso y tipo de contenido
         * - Usamos el servicio para: comprobar que el id existe y corresponde a un contenido visible,
         *   validar el token (soporta "Bearer <token>" o el token en claro) y asegurar que el usuario es un Visualizador.
         * - También valida reglas de negocio: edad mínima del contenido y condición VIP del usuario cuando aplique.
         * - Si algo falla, el servicio lanzará excepciones (400/403/404) y este método responderá en consecuencia.
         */
    // Permitir token vía cabecera o query (?auth=TOKEN) para habilitar reproducción directa desde etiquetas <audio>
    String token = (authHeader != null && !authHeader.isBlank()) ? authHeader : authQueryParam;
    Audio audio = multimediaService.validarYObtenerAudioParaStreaming(id, token);

        /*
         * Paso 2: Cargar el binario del audio
         * - El archivo .mp3 está guardado en MongoDB como un campo binario (Binary).
         * - Obtenemos el arreglo de bytes real. Si por cualquier motivo no existe o está vacío,
         *   respondemos con 404 (no hay contenido para reproducir).
         */
        byte[] data = audio.getfichero() != null ? audio.getfichero().getData() : null;
        if (data == null || data.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        /*
         * Paso 3: Preparar metadatos y cabeceras HTTP
         * - Calculamos el tamaño total del archivo en bytes.
         * - Establecemos el tipo MIME (por defecto "audio/mpeg" si no viene informado con el audio).
         * - No anunciamos soporte de rangos: se envía siempre el archivo completo.
         */
        long fileLength = data.length;
        String mime = audio.getmimeType() != null ? audio.getmimeType() : "audio/mpeg";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mime));

        /*
         * Paso 4: Enviar SIEMPRE el archivo completo (200 OK)
         * - Incluimos Content-Length para que el cliente conozca el tamaño total.
         */
        headers.setContentLength(fileLength);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
