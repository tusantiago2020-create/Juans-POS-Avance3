package pe.juans.pos.controller;
import pe.juans.pos.repository.*;import pe.juans.pos.model.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.util.*;import java.math.*;import java.io.*;
@RestController @RequestMapping("/api/reportes") @PreAuthorize("hasRole('ADMIN')")
public class ReporteController {
 private final VentaRepository ventas;private final DetalleVentaRepository detalles;private final ProductoRepository productos;private final ClienteRepository clientes;
 public ReporteController(VentaRepository v,DetalleVentaRepository d,ProductoRepository p,ClienteRepository c){ventas=v;detalles=d;productos=p;clientes=c;}
 public record Resumen(BigDecimal ventasTotales,BigDecimal margenEstimado,int numeroVentas,BigDecimal deudaTotal,int productosCriticos) {}
 @GetMapping("/resumen") public Resumen resumen(){
  BigDecimal total=BigDecimal.ZERO,margen=BigDecimal.ZERO,deuda=BigDecimal.ZERO;int cantidad=0,criticos=0;
  for(Venta v:ventas.findAll()){if(!"COMPLETADA".equals(v.estado))continue;cantidad++;total=total.add(v.total);
   for(DetalleVenta d:detalles.findByIdVenta(v.id)) margen=margen.add(d.precioUnitario.subtract(d.costoUnitario).multiply(BigDecimal.valueOf(d.cantidad)));}
  for(Cliente c:clientes.findAll())deuda=deuda.add(c.saldoDeuda);
  for(Producto p:productos.findByActivoTrueOrderByNombreAsc())if(p.stockActual<=p.stockMinimo)criticos++;
  return new Resumen(total,margen,cantidad,deuda,criticos);
 }
 @GetMapping("/excel") public ResponseEntity<byte[]> excel() throws IOException {
  try(Workbook wb=new XSSFWorkbook();ByteArrayOutputStream out=new ByteArrayOutputStream()){
   Sheet sheet=wb.createSheet("Ventas");Row h=sheet.createRow(0);String[] cols={"N° Venta","Fecha","Pago","Total S/","Estado"};
   for(int i=0;i<cols.length;i++)h.createCell(i).setCellValue(cols[i]);int n=1;
   for(Venta v:ventas.findAllByOrderByFechaHoraDesc()){Row row=sheet.createRow(n++);
    row.createCell(0).setCellValue(v.id);row.createCell(1).setCellValue(v.fechaHora.toString());row.createCell(2).setCellValue(v.tipoPago);
    row.createCell(3).setCellValue(v.total.doubleValue());row.createCell(4).setCellValue(v.estado);}
   for(int i=0;i<cols.length;i++)sheet.autoSizeColumn(i);
   wb.write(out);return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=juans_ventas.xlsx")
    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    .body(out.toByteArray());
  }
 }
}
