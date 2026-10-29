package pe.juans.pos.controller;
import pe.juans.pos.repository.*;
import pe.juans.pos.service.NegocioService;
import pe.juans.pos.dto.Solicitudes.*;
import pe.juans.pos.model.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import java.util.*;
@RestController @RequestMapping("/api")
public class ApiController {
 private final NegocioService negocio;private final ProductoRepository productos;private final ClienteRepository clientes;
 private final VentaRepository ventas;private final DetalleVentaRepository detalles;private final AlertaRepository alertas;private final AbonoRepository abonos;
 private final MovimientoInventarioRepository movimientos;
 public ApiController(NegocioService s,ProductoRepository p,ClienteRepository c,VentaRepository v,
  DetalleVentaRepository d,AlertaRepository a,AbonoRepository ab,MovimientoInventarioRepository m){
  negocio=s;productos=p;clientes=c;ventas=v;detalles=d;alertas=a;abonos=ab;movimientos=m;
 }
 @GetMapping("/csrf") public Map<String,String> csrf(CsrfToken t){return Map.of("headerName",t.getHeaderName(),"token",t.getToken());}
 @GetMapping("/yo") public Map<String,String> yo(Authentication a){return Map.of("usuario",a.getName(),"rol",a.getAuthorities().toString());}
 @GetMapping("/productos") public List<Producto> productos(){return productos.findByActivoTrueOrderByNombreAsc();}
 @PostMapping("/productos") @PreAuthorize("hasRole('ADMIN')") public Producto crear(@Valid @RequestBody ProductoDTO x){return negocio.guardarProducto(x,null);}
 @PutMapping("/productos/{id}") @PreAuthorize("hasRole('ADMIN')") public Producto editar(@PathVariable Long id,@Valid @RequestBody ProductoDTO x){return negocio.guardarProducto(x,id);}
 @PatchMapping("/productos/{id}/baja") @PreAuthorize("hasRole('ADMIN')") public Map<String,String> baja(@PathVariable Long id){negocio.bajaProducto(id);return Map.of("ok","Producto dado de baja");}
 @PostMapping("/productos/{id}/ingresos") @PreAuthorize("hasRole('ADMIN')") public Producto ingresar(@PathVariable Long id,@Valid @RequestBody AjusteDTO x){return negocio.ingresar(id,x);}
 @GetMapping("/clientes") public List<Cliente> clientes(){return clientes.findByActivoTrueOrderByNombresAsc();}
 @PostMapping("/clientes") @PreAuthorize("hasRole('ADMIN')") public Cliente crearCliente(@Valid @RequestBody ClienteDTO x){return negocio.guardarCliente(x,null);}
 @PutMapping("/clientes/{id}") @PreAuthorize("hasRole('ADMIN')") public Cliente editarCliente(@PathVariable Long id,@Valid @RequestBody ClienteDTO x){return negocio.guardarCliente(x,id);}
 @PostMapping("/clientes/{id}/abonos") public Abono abonar(@PathVariable Long id,@Valid @RequestBody AbonoDTO x){return negocio.abonar(id,x);}
 @GetMapping("/clientes/{id}/abonos") public List<Abono> abonos(@PathVariable Long id){return abonos.findByIdClienteOrderByFechaHoraDesc(id);}
 @GetMapping("/ventas") public List<Venta> ventas(){return ventas.findAllByOrderByFechaHoraDesc();}
 @GetMapping("/ventas/{id}/detalle") public List<DetalleVenta> detalles(@PathVariable Long id){return detalles.findByIdVenta(id);}
 @PostMapping("/ventas") public Venta vender(@Valid @RequestBody VentaDTO x){return negocio.vender(x);}
 @PatchMapping("/ventas/{id}/anular") @PreAuthorize("hasRole('ADMIN')") public Venta anular(@PathVariable Long id,@Valid @RequestBody AnularDTO x){return negocio.anular(id,x.motivo());}
 @GetMapping("/alertas") public List<Alerta> alertas(){return alertas.findAllByOrderByFechaGeneracionDesc();}
 @PatchMapping("/alertas/{id}/atender") public Alerta atender(@PathVariable Long id){return negocio.atender(id);}
 @GetMapping("/movimientos") public List<MovimientoInventario> movimientos(){return movimientos.findAllByOrderByFechaHoraDesc();}
}
