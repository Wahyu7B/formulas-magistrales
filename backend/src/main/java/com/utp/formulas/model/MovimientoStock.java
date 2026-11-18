package com.utp.formulas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "movimientos_stock")
@Getter
@Setter
public class MovimientoStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidad;

    @Column(nullable = false, length = 250)
    private String motivo;

    @Column(name = "stock_resultante", nullable = false, precision = 12, scale = 4)
    private BigDecimal stockResultante;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();
}
