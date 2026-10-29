package pe.juans.pos.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public final class Solicitudes {
 private Solicitudes() {}
 public record ProductoDTO(@NotBlank String codigo, @NotBlank String nombre, @NotBlank String categoria,
  String proveedor, @NotNull @DecimalMin("0.00") BigDecimal precioCompra,
  @NotNull @DecimalMin("0.01") BigDecimal precioVenta, @Min(0) int stockActual,
  @Min(0) int stockMinimo, LocalDate fechaVencimiento) {}
 public record ClienteDTO(@NotBlank @Pattern(regexp="[0-9]{8}") String dni, @NotBlank String nombres,
  String telefono, @NotNull @DecimalMin("0.00") BigDecimal limiteCredito, boolean habilitadoFiado) {}
 public record Item(@NotNull Long productoId, @Min(1) int cantidad) {}
 public record VentaDTO(@NotBlank String tipoPago, Long clienteId,
   @NotEmpty List<@Valid Item> items, BigDecimal montoRecibido) {}
 public record AbonoDTO(@NotNull @DecimalMin("0.01") BigDecimal monto, String medioPago) {}
 public record AjusteDTO(@Min(1) int cantidad, String motivo) {}
 public record AnularDTO(@NotBlank String motivo) {}
}
