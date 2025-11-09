package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Clase base para usuarios del sistema (Visualizador, Administrador, Gestor).
 * 
 * ESTRATEGIA DE PERSISTENCIA: Herencia en MongoDB
 * - Todos los tipos de usuario se guardan en la colección "users"
 * - MongoDB usa discriminadores automáticos para diferenciar tipos
 * - Permite consultas polimórficas y reutilización de código
 * 
 * ANOTACIONES DE PERSISTENCIA:
 * - @Document: Define la colección de MongoDB donde se almacenan los objetos
 * - @Id: Marca un campo como identificador único en MongoDB
 * - @Indexed: Define índices para búsquedas eficientes (ej: email único)
 * - @DBRef: Establece una referencia a otro documento en MongoDB
 * - @JsonIgnore: Evita la serialización circular en JSON cuando hay relaciones bidireccionales
 */
@Document(collection = "users") // Define que esta clase se guardará en la colección "users" de MongoDB
public class Usuario {
    
    /**
     * Identificador único generado por MongoDB
     * @Id - Marca este campo como el identificador único en la base de datos (similar a una clave primaria)
     */
    @Id // Indica a MongoDB que este campo es el identificador único del documento
    private String id;

    protected String nombre;
    protected String apellidos;

    @Indexed(unique = true, sparse = true)
    protected String email;
    
    protected Object foto;
    protected boolean bloqueado;


    @JsonIgnore
    public List<Codigorecuperacion> codigosrecuperacion = new ArrayList<>();
    
    @JsonIgnore
    public List<Token> sesionstoken = new ArrayList<>();

    @org.springframework.data.mongodb.core.mapping.DBRef
    @JsonIgnore
    public Contrasenia contrasenia;

    protected Date fecharegistro;

    @JsonIgnore
    private String secretkey;
    
    private boolean twoFactorAutenticationEnabled;
    private boolean threeFactorAutenticationEnabled;

    // Constructor vacío requerido por MongoDB
    public Usuario() {
        this.codigosrecuperacion = new ArrayList<>();
        this.sesionstoken = new ArrayList<>();
        this.fecharegistro = new Date();
    }

    public Usuario(String apellidos, boolean bloqueado, String email, Object foto, String nombre, Date fechaRegistro) {
        this.apellidos = apellidos;
        this.bloqueado = bloqueado;
        this.email = email;
        this.foto = foto;
        this.nombre = nombre;
        this.fecharegistro = fechaRegistro;
        // Inicializar listas
        this.codigosrecuperacion = new ArrayList<>();
        this.sesionstoken = new ArrayList<>();
        // contrasenia se inicializa como null (será asignada por separado)
    }

     // Constructor sin fecha de registro (se asigna automáticamente)
    public Usuario(String apellidos, boolean bloqueado, String email, Object foto, String nombre) {
        this(apellidos, bloqueado, email, foto, nombre, new Date());
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

    public List<Codigorecuperacion> getCodigosrecuperacion() {
        return codigosrecuperacion;
    }

    public void setCodigosrecuperacion(List<Codigorecuperacion> codigosrecuperacion) {
        this.codigosrecuperacion = codigosrecuperacion;
    }

    public List<Token> getSesionstoken() {
        return sesionstoken;
    }

    public void setSesionstoken(List<Token> sesionstoken) {
        this.sesionstoken = sesionstoken;
    }

    public String getSecretkey() {
        return secretkey;
    }

    public void setSecretkey(String secretkey) {
        this.secretkey = secretkey;
    }



}
