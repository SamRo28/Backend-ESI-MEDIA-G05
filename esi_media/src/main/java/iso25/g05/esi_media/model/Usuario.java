package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class Usuario {
    @Id
    private String id;

    protected String nombre;
    protected String apellidos;
    protected String email;
    protected Object foto;
    protected boolean bloqueado;

    @Transient
    public List<Codigorecuperacion> codigosrecuperacion = new ArrayList<>();
    
    @Transient
    public List<Token> sesionstoken = new ArrayList<>();

    @org.springframework.data.mongodb.core.mapping.DBRef
    public Contrasenia contrasenia;

    protected Date fecharegistro;

    @Transient
    private String secretkey;
    
    private boolean twoFactorAutenticationEnabled;
    private boolean threeFactorAutenticationEnabled;

    // Constructor vacío requerido por MongoDB
    public Usuario() {
        this.codigosrecuperacion = new ArrayList<>();
        this.sesionstoken = new ArrayList<>();
        this.fecharegistro = new Date();
    }


    public Usuario(String apellidos, boolean bloqueado, Contrasenia contrasenia, String email, Object foto, String nombre, Date fecharegistro) {
        this.apellidos = apellidos;
        this.bloqueado = bloqueado;
        this.contrasenia = contrasenia;
        this.email = email;
        this.foto = foto;
        this.nombre = nombre;
        this.fecharegistro = fecharegistro;
    }

    // Constructor sin fecha de registro (se asigna automáticamente)
    public Usuario(String apellidos, boolean bloqueado, Contrasenia contrasenia, String email, Object foto, String nombre) {
        this(apellidos, bloqueado, contrasenia, email, foto, nombre, new Date());
    }

    public Contrasenia getContrasenia() {
        return contrasenia;
    }
    public void setContrasenia(Contrasenia c) {
        contrasenia = c;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String n) {
        nombre = n;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String a) {
        apellidos = a;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String e) {
        email = e;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean b) {
        bloqueado = b;
    }

    public Date getFechaRegistro() {
        return fecharegistro;
    }

    public void setFechaRegistro(Date fecha) {
        this.fecharegistro = fecha;
    }
public boolean isTwoFactorAutenticationEnabled() {
        return twoFactorAutenticationEnabled;
    }

    public void setTwoFactorAutenticationEnabled(boolean twoFactorAutenticationEnabled) {
        this.twoFactorAutenticationEnabled = twoFactorAutenticationEnabled;
    }

    public boolean isThreeFactorAutenticationEnabled() {
        return threeFactorAutenticationEnabled;
    }

    public void setThreeFactorAutenticationEnabled(boolean threeFactorAutenticationEnabled) {
        this.threeFactorAutenticationEnabled = threeFactorAutenticationEnabled;
    }

    // Getters faltantes para id y foto
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getFoto() {
        return foto;
    }

    public void setFoto(Object foto) {
        this.foto = foto;
    }

}