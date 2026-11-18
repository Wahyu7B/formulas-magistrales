package com.utp.formulas.model;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@Entity
@Table(name = "pacientes")
@Getter
@Setter
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 8)
    private String dni;
    @Column(nullable = false, length = 100)
    private String nombres;
    @Column(nullable = false, length = 100)
    private String apellidos;
    @Column(nullable = false, length = 9)
    private String telefono;
    @Column(nullable = false, length = 250)
    private String direccion;
    @Column(nullable = false)
    private boolean activo = true;
}
