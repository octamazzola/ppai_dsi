package ar.edu.utn.frc.dsi.bolsines.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "empleado")
public class Empleado {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "email", nullable = false)
    private String email;

    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @ManyToOne
    @JoinColumn(name = "comision_medica_codigo", nullable = false)
    private ComisionMedica asignadoA;

    @OneToOne
    @JoinColumn(name = "nombre_usuario")
    private Usuario usuario;

    public Empleado() {
    }

    public Empleado(Long id, String nombre, String apellido, String email, Rol rol, ComisionMedica asignadoA, Usuario usuario) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.rol = rol;
        this.asignadoA = asignadoA;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getEmail() {
        return email;
    }

    public Rol getRol() {
        return rol;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    // Patron Experto (segun diagrama_secuencia y Modelo de Dominio)
    public boolean esTuUsuario(Usuario usuarioBuscado) {
        return this.usuario != null && usuarioBuscado != null && this.usuario.equals(usuarioBuscado);
    }

    public ComisionMedica getCM() {
        return this.asignadoA;
    }

    public String mostrarCM() {
        return this.asignadoA != null ? this.asignadoA.getNombre() : null;
    }

    public boolean esTuCM(ComisionMedica comisionMedica) {
        return this.asignadoA != null && comisionMedica != null && this.asignadoA.equals(comisionMedica);
    }

    public boolean sosGCM() {
        return this.rol != null && this.rol.esGCM();
    }
}
