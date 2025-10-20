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

	protected Date fechaestadoautomatico;
	protected Date fechadisponiblehasta;
	protected int edadvisualizacion;
	protected Object caratula;
	protected int nvisualizaciones;
	protected String gestorId;	// ID del gestor que subió el contenido


	
	public Contenido() { }

	public Contenido(String id, String titulo, String descripcion, List<String> tags, double duracion, boolean vip,
			boolean estado, Date fechaestadoautomatico, Date fechadisponiblehasta, int edadvisualizacion,
			Object caratula, int nvisualizaciones, String gestorId) {
		this.id = id;
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.tags = tags;
		this.duracion = duracion;
		this.vip = vip;
		this.estado = estado;
		this.fechaestadoautomatico = fechaestadoautomatico;
		this.fechadisponiblehasta = fechadisponiblehasta;
		this.edadvisualizacion = edadvisualizacion;
		this.caratula = caratula;
		this.nvisualizaciones = nvisualizaciones;
		this.gestorId = gestorId;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String gettitulo() {
		return titulo;
	}

	public void settitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getdescripcion() {
		return descripcion;
	}

	public void setdescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public List<String> gettags() {
		return tags;
	}

	public void settags(List<String> tags) {
		this.tags = tags;
	}

	public double getduracion() {
		return duracion;
	}

	public void setduracion(double duracion) {
		this.duracion = duracion;
	}

	public boolean isvip() {
		return vip;
	}

	public void setvip(boolean vip) {
		this.vip = vip;
	}

	public boolean isestado() {
		return estado;
	}

	public void setestado(boolean estado) {
		this.estado = estado;
	}

	public Date getfechaestadoautomatico() {
		return fechaestadoautomatico;
	}

	public void setfechaestadoautomatico(Date fechaestadoautomatico) {
		this.fechaestadoautomatico = fechaestadoautomatico;
	}

	public Date getfechadisponiblehasta() {
		return fechadisponiblehasta;
	}

	public void setfechadisponiblehasta(Date fechadisponiblehasta) {
		this.fechadisponiblehasta = fechadisponiblehasta;
	}

	public int getedadvisualizacion() {
		return edadvisualizacion;
	}

	public void setedadvisualizacion(int edadvisualizacion) {
		this.edadvisualizacion = edadvisualizacion;
	}

	public Object getcaratula() {
		return caratula;
	}

	public void setcaratula(Object caratula) {
		this.caratula = caratula;
	}

	public int getnvisualizaciones() {
		return nvisualizaciones;
	}

	public void setnvisualizaciones(int nvisualizaciones) {
		this.nvisualizaciones = nvisualizaciones;
	}

	public String getgestorId() {
		return gestorId;
	}

	public void setgestorId(String gestorId) {
		this.gestorId = gestorId;
	}

}
