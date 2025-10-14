package iso25.g05.esi_media.model;
import java.util.Date;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Document(collection = "contenidos")
public class Contenido {

	@Id
	protected String id;
	protected String _titulo;
	protected String _descripcion;
	protected List<String> _tags;	// Separados por comas
	protected double _duracion;
	protected boolean _vip;
	protected boolean _estado;	// ACLARACION: TRUE: Visible, FALSE: No visible
	protected Date _fecha_estado_automatico;
	protected Date _fecha_disponible_hasta;
	protected int _edad_visualizacion;
	protected Object _caratula;
	protected int _n_visualizaciones;


	
	public Contenido(String id, String _titulo, String _descripcion, List<String> _tags, double _duracion, boolean _vip,
			boolean _estado, Date _fecha_estado_automatico, Date _fecha_disponible_hasta, int _edad_visualizacion,
			Object _caratula, int _n_visualizaciones) {
		this.id = id;
		this._titulo = _titulo;
		this._descripcion = _descripcion;
		this._tags = _tags;
		this._duracion = _duracion;
		this._vip = _vip;
		this._estado = _estado;
		this._fecha_estado_automatico = _fecha_estado_automatico;
		this._fecha_disponible_hasta = _fecha_disponible_hasta;
		this._edad_visualizacion = _edad_visualizacion;
		this._caratula = _caratula;
		this._n_visualizaciones = _n_visualizaciones;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String get_titulo() {
		return _titulo;
	}

	public void set_titulo(String _titulo) {
		this._titulo = _titulo;
	}

	public String get_descripcion() {
		return _descripcion;
	}

	public void set_descripcion(String _descripcion) {
		this._descripcion = _descripcion;
	}

	public List<String> get_tags() {
		return _tags;
	}

	public void set_tags(List<String> _tags) {
		this._tags = _tags;
	}

	public double get_duracion() {
		return _duracion;
	}

	public void set_duracion(double _duracion) {
		this._duracion = _duracion;
	}

	public boolean is_vip() {
		return _vip;
	}

	public void set_vip(boolean _vip) {
		this._vip = _vip;
	}

	public boolean is_estado() {
		return _estado;
	}

	public void set_estado(boolean _estado) {
		this._estado = _estado;
	}

	public Date get_fecha_estado_automatico() {
		return _fecha_estado_automatico;
	}

	public void set_fecha_estado_automatico(Date _fecha_estado_automatico) {
		this._fecha_estado_automatico = _fecha_estado_automatico;
	}

	public Date get_fecha_disponible_hasta() {
		return _fecha_disponible_hasta;
	}

	public void set_fecha_disponible_hasta(Date _fecha_disponible_hasta) {
		this._fecha_disponible_hasta = _fecha_disponible_hasta;
	}

	public int get_edad_visualizacion() {
		return _edad_visualizacion;
	}

	public void set_edad_visualizacion(int _edad_visualizacion) {
		this._edad_visualizacion = _edad_visualizacion;
	}

	public Object get_caratula() {
		return _caratula;
	}

	public void set_caratula(Object _caratula) {
		this._caratula = _caratula;
	}

	public int get_n_visualizaciones() {
		return _n_visualizaciones;
	}

	public void set_n_visualizaciones(int _n_visualizaciones) {
		this._n_visualizaciones = _n_visualizaciones;
	}



	

}