import pe.juans.pos.service.ReglasNegocio;
import java.math.BigDecimal;
public class PruebaReglas {
 static void rechaza(Runnable f, String descripcion){
  boolean salio=false;try{f.run();}catch(IllegalArgumentException ex){salio=true;}
  if(!salio)throw new AssertionError("Debió rechazar: "+descripcion);
  System.out.println("OK rechazó: "+descripcion);
 }
 public static void main(String[]args){
  ReglasNegocio.validarStock(8,2);System.out.println("OK venta con stock");
  rechaza(()->ReglasNegocio.validarStock(2,3),"venta sin stock");
  rechaza(()->ReglasNegocio.validarStock(4,0),"cantidad cero");
  rechaza(()->ReglasNegocio.validarCredito(false,BigDecimal.ZERO,new BigDecimal("100"),new BigDecimal("30")),"fiado deshabilitado");
  rechaza(()->ReglasNegocio.validarCredito(true,new BigDecimal("80"),new BigDecimal("100"),new BigDecimal("30")),"límite de fiado excedido");
  ReglasNegocio.validarCredito(true,new BigDecimal("70"),new BigDecimal("100"),new BigDecimal("30"));System.out.println("OK fiado dentro del límite");
  if(ReglasNegocio.validarAbono(new BigDecimal("50"),new BigDecimal("20")).compareTo(new BigDecimal("30"))!=0)throw new AssertionError("Abono incorrecto");
  System.out.println("OK saldo tras abono");
  rechaza(()->ReglasNegocio.validarAbono(new BigDecimal("40"),new BigDecimal("80")),"abono superior a la deuda");
  if(ReglasNegocio.subtotal(new BigDecimal("4.50"),3).compareTo(new BigDecimal("13.50"))!=0)throw new AssertionError("Subtotal incorrecto");
  System.out.println("OK subtotal exacto con BigDecimal");
  System.out.println("RESULTADO: 9 ESCENARIOS APROBADOS");
 }
}
