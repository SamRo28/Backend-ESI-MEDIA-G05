package iso25.g05.esi_media.mapper;

import iso25.g05.esi_media.dto.ContenidoDetalleDTO;
import iso25.g05.esi_media.dto.ContenidoResumenDTO;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.Contenido;
import iso25.g05.esi_media.model.Video;

/**
 * Mapeador simple de entidades Contenido a DTOs.
 * Mantiene baja la complejidad y centraliza la conversión.
 */
public final class ContenidoMapper {

    private ContenidoMapper() {}

    /**
     * Construye un ContenidoResumenDTO a partir de la entidad.
     */
    public static ContenidoResumenDTO aResumen(Contenido c) {
        String tipo = tipoDe(c);
        return new ContenidoResumenDTO(
            c.getId(),
            c.gettitulo(),
            tipo,
            c.getcaratula(),
            c.isvip()
        );
    }

    /**
    * Construye un ContenidoDetalleDTO a partir de la entidad y una referencia de reproducción.
    * La referencia debe ser calculada por la capa de servicio:
    *  - VIDEO: url externa
    *  - AUDIO: endpoint del backend (p. ej., "/multimedia/audio/{id}")
     */
    public static ContenidoDetalleDTO aDetalle(Contenido c, String referenciaReproduccion) {
        String tipo = tipoDe(c);
        return new ContenidoDetalleDTO(
            c.getId(),
            c.gettitulo(),
            c.getdescripcion(),
            tipo,
            c.getcaratula(),
            c.isvip(),
            c.getfechadisponiblehasta(),
            c.getedadvisualizacion(),
            c.getnvisualizaciones(),
            c.gettags(),
            referenciaReproduccion
        );
    }

    /**
     * Determina el tipo del contenido para el DTO.
     */
    private static String tipoDe(Contenido c) {
        if (c instanceof Audio) return "AUDIO";
        if (c instanceof Video) return "VIDEO";
        return "DESCONOCIDO";
    }
}
