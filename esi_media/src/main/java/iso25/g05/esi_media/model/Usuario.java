package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

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
    
    /**
     * Email único para login y identificación
     * TEMPORAL: @Indexed comentado para evitar conflictos durante desarrollo
     * @Indexed(unique = true) - Crea un índice único en MongoDB para búsquedas rápidas y
     * para garantizar que no pueda haber dos usuarios con el mismo email
     */
    // @Indexed(unique = true) // Comentado temporalmente durante desarrollo
    protected String email;
    protected Object foto;
    protected boolean bloqueado;
    
    /**
     * RELACIONES ENTRE ENTIDADES - RESOLUCIÓN DE PROBLEMAS CIRCULARES:
     * 
     * @DBRef(lazy = true): Anotación de MongoDB que:
     *   1. Establece una relación entre documentos (similar a foreign key en SQL)
     *   2. El parámetro "lazy = true" hace que los datos relacionados NO se carguen 
     *      automáticamente al consultar un usuario, sino solo cuando se accede a ellos
     *   3. Evita cargar grandes cantidades de datos innecesarios
     *      Por ejemplo: al cargar un usuario no carga automáticamente todos sus tokens
     * 
     * @JsonIgnore: Anotación de Jackson (biblioteca de JSON) que:
     *   1. Evita que este campo se incluya al convertir el objeto a JSON
     *   2. Previene bucles infinitos de serialización en relaciones bidireccionales
     *      Ejemplo: Usuario → Contraseña → Usuario → Contraseña → ...
     *   3. Sin esta anotación, las APIs REST que devuelven usuarios generarían
     *      respuestas enormes o errores de memoria
     */
    @org.springframework.data.mongodb.core.mapping.DBRef(lazy = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Codigo_recuperacion> codigos_recuperacion = new ArrayList<>();
    
    @Transient
    @org.springframework.data.mongodb.core.mapping.DBRef(lazy = true) 
    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<Token> sesionstoken = new ArrayList<>();
    
    @org.springframework.data.mongodb.core.mapping.DBRef(lazy = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Contrasenia contrasenia;
    
    protected Date fecharegistro;
    @Transient
    private String secretkey;
    private boolean twoFactorAutenticationEnabled;
    private boolean threeFactorAutenticationEnabled;

    /**
     * Constructor vacío requerido por Spring Data MongoDB
     */
    public Usuario() {
        // Inicializar listas para evitar NullPointerException
        this.codigos_recuperacion = new ArrayList<>();
        this.sesionstoken = new ArrayList<>();
    }

    public Usuario(String apellidos, boolean bloqueado, String email, Object foto, String nombre, Date fechaRegistro) {
        this.apellidos = apellidos;
        this.bloqueado = bloqueado;
        this.email = email;
        this.foto = foto;
        this.nombre = nombre;
        this.fecharegistro = fechaRegistro;
        // Inicializar listas
        this.codigos_recuperacion = new ArrayList<>();
        this.sesionstoken = new ArrayList<>();
        // contrasenia se inicializa como null (será asignada por separado)
    }

     // Constructor sin fecha de registro (se asigna automáticamente)
    public Usuario(String apellidos, boolean bloqueado, String email, Object foto, String nombre) {
        this(apellidos, bloqueado, email, foto, nombre, new Date());
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
    
    // === GETTERS/SETTERS ADICIONALES PARA PERSISTENCIA ===
    
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
    
    /**
     * GETTERS/SETTERS PARA RELACIONES - Con protecciones contra referencias circulares
     * 
     * @JsonIgnore en los getters: Es fundamental para prevenir la serialización circular
     * cuando se convierten objetos a JSON (especialmente en respuestas de API REST).
     * 
     * Si no usáramos @JsonIgnore, al intentar convertir un Usuario a JSON para enviarlo
     * como respuesta de API, se produciría un bucle infinito:
     * 1. Usuario intenta serializar su contraseña
     * 2. Contraseña intenta serializar su usuario (si tuviera referencia inversa)
     * 3. Usuario intenta serializar su contraseña... y así indefinidamente
     * 
     * Este patrón nos permite:
     * - Mantener las relaciones necesarias en Java
     * - Evitar errores de recursión infinita en JSON
     * - Controlar exactamente qué datos se incluyen en las respuestas API
     */
    
    @com.fasterxml.jackson.annotation.JsonIgnore // Evita serialización circular en JSON
    public Contrasenia getContrasenia() {
        return contrasenia;
    }
    
    public void setContrasenia(Contrasenia contrasenia) {
        this.contrasenia = contrasenia;
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore // Evita serialización circular en JSON
    public List<Codigo_recuperacion> getCodigosRecuperacion() {
        return codigos_recuperacion;
    }
    
    public void setCodigosRecuperacion(List<Codigo_recuperacion> codigos) {
        this.codigos_recuperacion = codigos != null ? codigos : new ArrayList<>();
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore // Evita serialización circular en JSON
    public List<Token> getSesionsToken() {
        return sesionstoken;
    }
    
    public void setSesionsToken(List<Token> tokens) {
        this.sesionstoken = tokens != null ? tokens : new ArrayList<>();
    }
    
    public String getSecretkey() {
        return secretkey;
    }
    
    public void setSecretkey(String secretkey) {
        this.secretkey = secretkey;
    }
    
    public boolean is2FactorAutenticationEnabled() {
        return twoFactorAutenticationEnabled;
    }
    
    public void set2FactorAutenticationEnabled(boolean enabled) {
        this.twoFactorAutenticationEnabled = enabled;
    }
    
    public boolean is3FactorAutenticationEnabled() {
        return threeFactorAutenticationEnabled;
    }
    
    public void set3FactorAutenticationEnabled(boolean enabled) {
        this.threeFactorAutenticationEnabled = enabled;
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

}
