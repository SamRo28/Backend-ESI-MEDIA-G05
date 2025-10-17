package iso25.g05.esi_media.model;
import java.util.Date;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Document(collection = "contenidos")
public class Contenido {

	@Id
	protected String id;
	protected String titulo;
	protected String descripcion;
	protected List<String> tags;
	protected double duracion;
	protected boolean vip;
	protected boolean estado;	// ACLARACION: TRUE: Visible, FALSE: No visible
	protected Date fecha_estado_automatico;
	protected Date fecha_disponible_hasta;
	protected int edad_visualizacion;
	protected Object caratula;
	protected int n_visualizaciones;
	protected String gestorId;	// ID del gestor que subió el contenido


	
	public Contenido(String id, String titulo, String descripcion, List<String> tags, double duracion, boolean vip,
			boolean estado, Date fecha_estado_automatico, Date fecha_disponible_hasta, int edad_visualizacion,
			Object caratula, int n_visualizaciones, String gestorId) {
		this.id = id;
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.tags = tags;
		this.duracion = duracion;
		this.vip = vip;
		this.estado = estado;
		this.fecha_estado_automatico = fecha_estado_automatico;
		this.fecha_disponible_hasta = fecha_disponible_hasta;
		this.edad_visualizacion = edad_visualizacion;
		this.caratula = caratula;
		this.n_visualizaciones = n_visualizaciones;
		this.gestorId = gestorId;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public double getDuracion() {
		return duracion;
	}

	public void setDuracion(double duracion) {
		this.duracion = duracion;
	}

	public boolean isVip() {
		return vip;
	}

	public void setVip(boolean vip) {
		this.vip = vip;
	}

	public boolean isEstado() {
		return estado;
	}

	public void setEstado(boolean estado) {
		this.estado = estado;
	}

	public Date getFecha_estado_automatico() {
		return fecha_estado_automatico;
	}

	public void setFecha_estado_automatico(Date fecha_estado_automatico) {
		this.fecha_estado_automatico = fecha_estado_automatico;
	}

	public Date getFecha_disponible_hasta() {
		return fecha_disponible_hasta;
	}

	public void setFecha_disponible_hasta(Date fecha_disponible_hasta) {
		this.fecha_disponible_hasta = fecha_disponible_hasta;
	}

	public int getEdad_visualizacion() {
		return edad_visualizacion;
	}

	public void setEdad_visualizacion(int edad_visualizacion) {
		this.edad_visualizacion = edad_visualizacion;
	}

	public Object getCaratula() {
		return caratula;
	}

	public void setCaratula(Object caratula) {
		this.caratula = caratula;
	}

	public int getN_visualizaciones() {
		return n_visualizaciones;
	}

	public void setN_visualizaciones(int n_visualizaciones) {
		this.n_visualizaciones = n_visualizaciones;
	}

	public String getGestorId() {
		return gestorId;
	}

	public void setGestorId(String gestorId) {
		this.gestorId = gestorId;
	}

}