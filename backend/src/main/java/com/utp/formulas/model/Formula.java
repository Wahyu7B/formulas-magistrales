package com.utp.formulas.model;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@Entity
@Table(name = "formulas")
@Getter
@Setter
public class Formula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nombre;
    @Column(nullable = false, length = 100)
    private String presentacion;
    @Column(nullable = false, length = 2000)
    private String indicaciones;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
    @OneToMany(mappedBy = "formula", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingrediente> ingredientes = new ArrayList<>();
    @Column(nullable = false)
    private boolean activo = true;
}
