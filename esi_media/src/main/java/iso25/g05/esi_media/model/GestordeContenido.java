package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.List;

/**
 * GestordeContenido - hereda de Usuario
 * NO necesita @Document porque Usuario ya lo tiene
 * Spring Data MongoDB usará el discriminador _class para identificar el tipo
 */
public class GestordeContenido extends Usuario {

	private String alias;
	private String descripcion;
	private String campoespecializacion;
	private String tipocontenidovideooaudio;
	public List<Lista> listasgeneradas;
	private List<String> contenidos_subidos = new ArrayList<>();	// Lista de IDs de contenidos subidos por el gestor

	// Constructor vacío requerido por MongoDB
	public GestordeContenido() {
		super("", false, null, "", null, "");
		this.listasgeneradas = new ArrayList<>();
	}

	public GestordeContenido(String apellidos, boolean bloqueado, Contrasenia contrasenia, String email,
			Object foto, String nombre) {
		super(apellidos, bloqueado, contrasenia, email, foto, nombre);
		this.listasgeneradas = new ArrayList<>();
		this.alias = null;
		this.descripcion = null;
		this.campoespecializacion = null;
		this.tipocontenidovideooaudio = null;
	}

	public String getalias() {
		return alias;
	}

	public void setalias(String alias) {
		this.alias = alias;
	}

	public String getdescripcion() {
		return descripcion;
	}

	public void setdescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getcampoespecializacion() {
		return campoespecializacion;
	}

	public void setcampoespecializacion(String campoespecializacion) {
		this.campoespecializacion = campoespecializacion;
	}

	public String gettipocontenidovideooaudio() {
		return tipocontenidovideooaudio;
	}

	public void settipocontenidovideooaudio(String tipocontenidovideooaudio) {
		this.tipocontenidovideooaudio = tipocontenidovideooaudio;
	}

	public List<Lista> getListasgeneradas() {
		return listasgeneradas;
	}

	public void setListasgeneradas(List<Lista> listasgeneradas) {
		this.listasgeneradas = listasgeneradas;
	}

	public List<String> getContenidos_subidos() {
		return contenidos_subidos;
	}

	public void setContenidos_subidos(List<String> contenidos_subidos) {
		this.contenidos_subidos = contenidos_subidos;
	}

	//TODO: Habria que meter un add y un remove para las listas generadas y otro para los contenidos subidos

	public void subir(Contenido aC) {
		throw new UnsupportedOperationException();
	}

}