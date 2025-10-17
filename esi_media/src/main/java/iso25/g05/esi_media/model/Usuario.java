package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Document(collection = "users")
public class Usuario {
    @Id
    private String id;

    protected String nombre;
    protected String apellidos;
    protected String email;
    protected Object foto;
    protected boolean bloqueado;
    private List<Codigo_recuperacion> codigosRecuperacion = new ArrayList<>();
    private List<Token> sesionsToken = new ArrayList<>();
    private Contrasenia contrasenia;
    protected Date fechaRegistro;
    private String secretkey;
    private boolean twoFactorAuthenticationEnabled;
    private boolean threeFactorAuthenticationEnabled;


    public Usuario(String apellidos, boolean bloqueado, Contrasenia contrasenia, String email, Object foto, String nombre, Date fechaRegistro) {
        this.apellidos = apellidos;
        this.bloqueado = bloqueado;
        this.contrasenia = contrasenia;
        this.email = email;
        this.foto = foto;
        this.nombre = nombre;
        this.fechaRegistro = fechaRegistro;
    }

        public Usuario() {
        this.fechaRegistro = new Date();
        this.codigosRecuperacion = new ArrayList<>();
        this.sesionsToken = new ArrayList<>();
    }

    // Constructor sin fecha de registro (se asigna automáticamente)
    public Usuario(String apellidos, boolean bloqueado, Contrasenia contrasenia, String email, Object foto, String nombre) {
        this(apellidos, bloqueado, contrasenia, email, foto, nombre, new Date());
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Object getFoto() {
        return foto;
    }

    public void setFoto(Object foto) {
        this.foto = foto;
    }

    public Contrasenia getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(Contrasenia contrasenia) {
        this.contrasenia = contrasenia;
    }

    public List<Codigo_recuperacion> getCodigosRecuperacion() {
        return codigosRecuperacion;
    }

    public void setCodigosRecuperacion(List<Codigo_recuperacion> codigosRecuperacion) {
        this.codigosRecuperacion = codigosRecuperacion;
    }

    public List<Token> getSesionsToken() {
        return sesionsToken;
    }

    public void setSesionsToken(List<Token> sesionsToken) {
        this.sesionsToken = sesionsToken;
    }

    public String getSecretkey() {
        return secretkey;
    }

    public void setSecretkey(String secretkey) {
        this.secretkey = secretkey;
    }

    public boolean isTwoFactorAuthenticationEnabled() {
        return twoFactorAuthenticationEnabled;
    }

    public void setTwoFactorAuthenticationEnabled(boolean twoFactorAuthenticationEnabled) {
        this.twoFactorAuthenticationEnabled = twoFactorAuthenticationEnabled;
    }

    public boolean isThreeFactorAuthenticationEnabled() {
        return threeFactorAuthenticationEnabled;
    }

    public void setThreeFactorAuthenticationEnabled(boolean threeFactorAuthenticationEnabled) {
        this.threeFactorAuthenticationEnabled = threeFactorAuthenticationEnabled;
    }
}
