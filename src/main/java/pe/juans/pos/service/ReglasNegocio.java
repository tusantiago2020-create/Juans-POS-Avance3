package pe.juans.pos.service;
import java.math.BigDecimal;
import java.util.Objects;
/** Reglas puras: no dependen de Spring y son verificables con JDK. */
public final class ReglasNegocio {
 private ReglasNegocio() {}
 public static void validarStock(int stock, int cantidad) {
  if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
  if (stock < cantidad) throw new IllegalArgumentException("Stock insuficiente (disponible: " + stock + ")");
 }
 public static void validarCredito(boolean habilitado, BigDecimal saldo, BigDecimal limite, BigDecimal compra) {
  Objects.requireNonNull(saldo);Objects.requireNonNull(limite);Objects.requireNonNull(compra);
  if (!habilitado) throw new IllegalArgumentException("El cliente no está habilitado para fiado");
  if (compra.signum() <= 0 || saldo.add(compra).compareTo(limite) > 0)
   throw new IllegalArgumentException("La venta supera el límite de crédito del cliente");
 }
 public static BigDecimal validarAbono(BigDecimal deuda, BigDecimal abono) {
  Objects.requireNonNull(deuda); Objects.requireNonNull(abono);
  if (abono.signum()<=0 || abono.compareTo(deuda)>0)
   throw new IllegalArgumentException("El abono debe ser positivo y no mayor que la deuda");
  return deuda.subtract(abono);
 }
 public static BigDecimal subtotal(BigDecimal precio,int cantidad) {
  if(precio==null||precio.signum()<=0||cantidad<=0) throw new IllegalArgumentException("Precio y cantidad inválidos");
  return precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2,java.math.RoundingMode.HALF_UP);
 }
}
