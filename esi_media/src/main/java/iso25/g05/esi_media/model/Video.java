package iso25.g05.esi_media.model;

public class Video extends Contenido {

    private String _url;
    private String _resolucion;

    public Video(String _titulo, String _descripcion, java.util.List<String> _tags, double _duracion, boolean _vip,
            boolean _estado, java.util.Date _fecha_estado_automatico, java.util.Date _fecha_disponible_hasta,
            int _edad_visualizacion, Object _caratula, int _n_visualizaciones, java.util.List<Lista> _unnamed_Lista_,
            java.util.List<Visualizador> _unnamed_Visualizador_, String url, String resolucion) {
        super(_titulo, _descripcion, _tags, _duracion, _vip, _estado, _fecha_estado_automatico, _fecha_disponible_hasta,
                _edad_visualizacion, _caratula, _n_visualizaciones, _unnamed_Lista_, _unnamed_Visualizador_);
        this._url = url;
        this._resolucion = resolucion;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(String u) {
        _url = u;
    }

    public String getResolucion() {
        return _resolucion;
    }

    public void setResolucion(String r) {
        _resolucion = r;
    }
}
