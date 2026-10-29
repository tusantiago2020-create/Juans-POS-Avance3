package pe.juans.pos.service;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
public class ReglasNegocioTest {
 @Test void permiteVentaConStockSuficiente(){assertDoesNotThrow(()->ReglasNegocio.validarStock(10,3));}
 @Test void rechazaVentaSinStock(){assertThrows(IllegalArgumentException.class,()->ReglasNegocio.validarStock(2,3));}
 @Test void rechazaCantidadNegativa(){assertThrows(IllegalArgumentException.class,()->ReglasNegocio.validarStock(10,-1));}
 @Test void rechazaClienteFiadoDeshabilitado(){assertThrows(IllegalArgumentException.class,()->ReglasNegocio.validarCredito(false,new BigDecimal("10"),new BigDecimal("100"),new BigDecimal("20")));}
 @Test void rechazaCreditoQueSuperaLimite(){assertThrows(IllegalArgumentException.class,()->ReglasNegocio.validarCredito(true,new BigDecimal("90"),new BigDecimal("100"),new BigDecimal("20")));}
 @Test void permiteCreditoDentroDeLimite(){assertDoesNotThrow(()->ReglasNegocio.validarCredito(true,new BigDecimal("80"),new BigDecimal("100"),new BigDecimal("20")));}
 @Test void descuentaAbonoCorrectamente(){assertEquals(new BigDecimal("30.00"),ReglasNegocio.validarAbono(new BigDecimal("50.00"),new BigDecimal("20.00")));}
 @Test void rechazaAbonoMayorQueSaldo(){assertThrows(IllegalArgumentException.class,()->ReglasNegocio.validarAbono(new BigDecimal("50"),new BigDecimal("60")));}
 @Test void calculaSubtotalConDosDecimales(){assertEquals(new BigDecimal("13.50"),ReglasNegocio.subtotal(new BigDecimal("4.50"),3));}
}
