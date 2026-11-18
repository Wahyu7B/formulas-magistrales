package com.utp.formulas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "insumos")
@Getter
@Setter
public class Insumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal stock;

    @Column(name = "stock_minimo", nullable = false, precision = 12, scale = 4)
    private BigDecimal stockMinimo;

    @Column(nullable = false, length = 10)
    private String unidad;

    @Column(nullable = false)
    private boolean activo = true;
}
