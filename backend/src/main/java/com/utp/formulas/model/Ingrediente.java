package com.utp.formulas.model;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@Entity
@Table(name = "ingredientes")
@Getter
@Setter
public class Ingrediente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidad;
    @Column(nullable = false, length = 10)
    private String unidad;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formula_id", nullable = false)
    private Formula formula;
}
