package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.List;

public class GestordeContenido extends Usuario {

	private String alias;
	private String descripcion;
	private String campoespecializacion;
	private String tipocontenidovideooaudio;
	public List<Lista> listasgeneradas ;

	// Constructor vacío requerido por MongoDB
	public GestordeContenido() {
		super("", false, null, "", null, "");
		this.listasgeneradas = new ArrayList<>();
		// MongoDB usará este constructor y luego los setters para poblar los campos
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



	public void subir(Contenido aC) {
		throw new UnsupportedOperationException();
	}


}