package pe.juans.pos.service;
import pe.juans.pos.model.*;
import pe.juans.pos.dto.Solicitudes.*;
import pe.juans.pos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.slf4j.MDC;import org.slf4j.MDC;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
@Service
public class NegocioService {
 private static final Logger log=LoggerFactory.getLogger(NegocioService.class); // Logback por SLF4J
 private static final Set<String> PAGOS=ImmutableSet.of("EFECTIVO","YAPE_PLIN","TARJETA","FIADO"); // Guava
 private final ProductoRepository productos;private final ClienteRepository clientes;private final VentaRepository ventas;
 private final DetalleVentaRepository detalles;private final AbonoRepository abonos;private final AlertaRepository alertas;
 private final MovimientoInventarioRepository movimientos;private final AuditoriaRepository auditoria;private final UsuarioRepository usuarios;
 public NegocioService(ProductoRepository p,ClienteRepository c,VentaRepository v,DetalleVentaRepository d,
 AbonoRepository b,AlertaRepository a,MovimientoInventarioRepository m,AuditoriaRepository au,UsuarioRepository u) {
 productos=p;clientes=c;ventas=v;detalles=d;abonos=b;alertas=a;movimientos=m;auditoria=au;usuarios=u;
 }
 private Long actor(){String nombre=SecurityContextHolder.getContext().getAuthentication().getName();
 return usuarios.findByUsernameAndActivoTrue(nombre).orElseThrow(()->new IllegalStateException("Sesión inválida")).id;}
 private void auditar(Long id, String accion, String tabla,String reg){Auditoria a=new Auditoria();a.idUsuario=id;a.accion=accion;a.tablaAfectada=tabla;a.idRegistro=reg;auditoria.save(a);
 try(MDC.MDCCloseable evento=MDC.putCloseable("evento",accion)){
   log.info("Acción {} en {} registro {}",accion,tabla,reg);
 }
 }
 private void movimiento(Long usuario,Long producto,Long venta,String tipo,int cantidad,String motivo){
 MovimientoInventario m=new MovimientoInventario();m.idUsuario=usuario;m.idProducto=producto;m.idVenta=venta;m.tipo=tipo;m.cantidad=cantidad;m.motivo=motivo;movimientos.save(m);}
 @Transactional public Producto guardarProducto(ProductoDTO x, Long id) {
  Producto p=id==null?new Producto():productos.findById(id).orElseThrow(()->new IllegalArgumentException("Producto no existe"));
  String codigo=StringUtils.trimToEmpty(x.codigo());String nombre=StringUtils.normalizeSpace(x.nombre());
  if(codigo.isBlank()||nombre.isBlank())throw new IllegalArgumentException("Datos incompletos");
  if(productos.findByCodigo(codigo).filter(t->!Objects.equals(t.id,id)).isPresent())throw new IllegalArgumentException("Código duplicado");
  p.codigo=codigo;p.nombre=nombre;p.categoria=StringUtils.defaultIfBlank(x.categoria(),"General");p.proveedor=StringUtils.defaultIfBlank(x.proveedor(),"Sin especificar");
  p.precioCompra=x.precioCompra();p.precioVenta=x.precioVenta();p.stockMinimo=x.stockMinimo();p.fechaVencimiento=x.fechaVencimiento();
  if(id==null)p.stockActual=x.stockActual();
  p=productos.save(p);if(id==null&&p.stockActual>0)movimiento(actor(),p.id,null,"INGRESO",p.stockActual,"Stock inicial");
  auditar(actor(),id==null?"CREAR_PRODUCTO":"EDITAR_PRODUCTO","producto",p.id.toString());revisarProducto(p);return p;
 }
 @Transactional public void bajaProducto(Long id){Producto p=productos.findById(id).orElseThrow(()->new IllegalArgumentException("Producto no existe"));p.activo=false;productos.save(p);auditar(actor(),"BAJA_PRODUCTO","producto",id.toString());}
 @Transactional public Producto ingresar(Long id,AjusteDTO x){Producto p=productos.bloquearPorId(id).orElseThrow(()->new IllegalArgumentException("Producto no existe"));p.stockActual+=x.cantidad();productos.save(p);movimiento(actor(),p.id,null,"INGRESO",x.cantidad(),"Reposición: "+StringUtils.abbreviate(StringUtils.defaultString(x.motivo()),100));auditar(actor(),"REPONER_STOCK","producto",id.toString());return p;}
 @Transactional public Cliente guardarCliente(ClienteDTO x,Long id){Cliente c=id==null?new Cliente():clientes.findById(id).orElseThrow(()->new IllegalArgumentException("Cliente no existe"));
 if(clientes.findByDni(x.dni()).filter(t->!Objects.equals(t.id,id)).isPresent())throw new IllegalArgumentException("DNI duplicado");
 if(c.saldoDeuda.compareTo(x.limiteCredito())>0)throw new IllegalArgumentException("El nuevo límite es menor que la deuda");
 c.dni=x.dni();c.nombres=StringUtils.normalizeSpace(x.nombres());c.telefono=StringUtils.trimToEmpty(x.telefono());c.limiteCredito=x.limiteCredito();c.habilitadoFiado=x.habilitadoFiado();c=clientes.save(c);auditar(actor(),"GUARDAR_CLIENTE","cliente",c.id.toString());return c;}
 @Transactional public Venta vender(VentaDTO x){
 String pago=StringUtils.upperCase(StringUtils.trimToEmpty(x.tipoPago()),Locale.ROOT);
 if(!PAGOS.contains(pago))throw new IllegalArgumentException("Medio de pago no válido");
 if(x.items()==null||x.items().isEmpty())throw new IllegalArgumentException("La venta debe incluir productos");
 Map<Long,Integer> carrito=new TreeMap<>();for(Item i:x.items()){
  if(i.productoId()==null||i.cantidad()<=0)throw new IllegalArgumentException("Cantidad incorrecta");
  carrito.merge(i.productoId(),i.cantidad(),Math::addExact);
 }
 Map<Long,Producto> bloqueados=new LinkedHashMap<>();BigDecimal total=BigDecimal.ZERO;
 for(var e:carrito.entrySet()){
  Producto p=productos.bloquearPorId(e.getKey()).orElseThrow(()->new IllegalArgumentException("Producto no existe"));
  if(!p.activo)throw new IllegalArgumentException("Producto desactivado");ReglasNegocio.validarStock(p.stockActual,e.getValue());
  if(p.fechaVencimiento!=null&&p.fechaVencimiento.isBefore(LocalDate.now()))throw new IllegalArgumentException("Producto vencido: "+p.nombre);
  bloqueados.put(p.id,p);total=total.add(ReglasNegocio.subtotal(p.precioVenta,e.getValue()));
 }
 Cliente cliente=null;
 if("FIADO".equals(pago)){
  if(x.clienteId()==null)throw new IllegalArgumentException("Selecciona un cliente para el fiado");
  cliente=clientes.bloquearPorId(x.clienteId()).orElseThrow(()->new IllegalArgumentException("Cliente no existe"));
  if(!cliente.activo)throw new IllegalArgumentException("Cliente inactivo");
  ReglasNegocio.validarCredito(cliente.habilitadoFiado,cliente.saldoDeuda,cliente.limiteCredito,total);
 } else if("EFECTIVO".equals(pago)&& (x.montoRecibido()==null||x.montoRecibido().compareTo(total)<0)){
  throw new IllegalArgumentException("El efectivo recibido es insuficiente");
 }
 Long usuario=actor();Venta v=new Venta();v.idUsuario=usuario;v.idCliente=cliente==null?null:cliente.id;
 v.tipoPago=pago;v.total=total;v.montoRecibido=x.montoRecibido()==null?BigDecimal.ZERO:x.montoRecibido();v=ventas.save(v);
 for(var e:carrito.entrySet()){
  Producto p=bloqueados.get(e.getKey());int cant=e.getValue();DetalleVenta d=new DetalleVenta();
  d.idVenta=v.id;d.idProducto=p.id;d.cantidad=cant;d.precioUnitario=p.precioVenta;d.costoUnitario=p.precioCompra;d.subtotal=ReglasNegocio.subtotal(p.precioVenta,cant);detalles.save(d);
  p.stockActual-=cant;productos.save(p);movimiento(usuario,p.id,v.id,"VENTA",-cant,"Venta #"+v.id);revisarProducto(p);
 }
 if(cliente!=null){cliente.saldoDeuda=cliente.saldoDeuda.add(total);clientes.save(cliente);}
 auditar(usuario,"REGISTRAR_VENTA","venta",v.id.toString());log.info("Venta creada id={} monto={}",v.id,total);return v;
 }
 @Transactional public Venta anular(Long id,String motivo){
 Venta v=ventas.findById(id).orElseThrow(()->new IllegalArgumentException("Venta no existe"));
 if(!"COMPLETADA".equals(v.estado))throw new IllegalArgumentException("La venta ya está anulada");
 String m=StringUtils.trimToEmpty(motivo);if(m.isBlank())throw new IllegalArgumentException("Indica un motivo");
 // Bloqueo de productos por ID en orden predecible.
 var ds=detalles.findByIdVenta(id);ds.sort(Comparator.comparing(a->a.idProducto));
 for(DetalleVenta d:ds){Producto p=productos.bloquearPorId(d.idProducto).orElseThrow();p.stockActual+=d.cantidad;productos.save(p);movimiento(actor(),p.id,v.id,"ANULACION",d.cantidad,m);}
 if("FIADO".equals(v.tipoPago)){
  Cliente c=clientes.bloquearPorId(v.idCliente).orElseThrow();
  if(c.saldoDeuda.compareTo(v.total)<0)throw new IllegalArgumentException("No se puede anular: hay abonos posteriores; regulariza manualmente");
  c.saldoDeuda=c.saldoDeuda.subtract(v.total);clientes.save(c);
 }
 v.estado="ANULADA";v.motivoAnulacion=m;ventas.save(v);auditar(actor(),"ANULAR_VENTA","venta",id.toString());return v;
 }
 @Transactional public Abono abonar(Long id,AbonoDTO x){
 Cliente c=clientes.bloquearPorId(id).orElseThrow(()->new IllegalArgumentException("Cliente no existe"));
 c.saldoDeuda=ReglasNegocio.validarAbono(c.saldoDeuda,x.monto());clientes.save(c);
 String pago=StringUtils.upperCase(StringUtils.defaultIfBlank(x.medioPago(),"EFECTIVO"),Locale.ROOT);
 if(!Set.of("EFECTIVO","YAPE_PLIN","TARJETA").contains(pago))throw new IllegalArgumentException("Medio de abono no válido");
 Abono a=new Abono();a.idCliente=id;a.idUsuario=actor();a.monto=x.monto();a.medioPago=pago;abonos.save(a);auditar(actor(),"REGISTRAR_ABONO","abono",a.id.toString());return a;
 }
 @Transactional public void revisarProducto(Producto p){
 if(!p.activo)return;
 if(p.stockActual<=p.stockMinimo)crearAlerta(p,"STOCK_BAJO","Stock crítico: "+p.stockActual+" (mínimo "+p.stockMinimo+")");
 if(p.fechaVencimiento!=null){long dias=java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(),p.fechaVencimiento);
 if(dias<0)crearAlerta(p,"VENCIDO","Producto vencido");else if(dias<=7)crearAlerta(p,"POR_VENCER","Vence en "+dias+" día(s)");}
 }
 private void crearAlerta(Producto p,String tipo,String mensaje){if(!alertas.existsByIdProductoAndTipoAndEstado(p.id,tipo,"PENDIENTE")){
 Alerta a=new Alerta();a.idProducto=p.id;a.tipo=tipo;a.mensaje=mensaje;alertas.save(a);
 }}
 @Scheduled(cron="0 0 8 * * *",zone="America/Lima") @Transactional public void revisionDiaria(){for(Producto p:productos.findByActivoTrueOrderByNombreAsc())revisarProducto(p);}
 @Transactional public Alerta atender(Long id){Alerta a=alertas.findById(id).orElseThrow(()->new IllegalArgumentException("Alerta inexistente"));a.estado="ATENDIDA";a.fechaAtencion=LocalDateTime.now();a.idUsuarioAtencion=actor();return alertas.save(a);}
}
