'use strict';
let csrf={headerName:'X-XSRF-TOKEN',token:''};
let isAdmin=false;
let data={productos:[],clientes:[],ventas:[],alertas:[],resumen:null};
let cart=[];
const $=s=>document.querySelector(s);
const money=n=>'S/ '+Number(n||0).toFixed(2);
const esc=s=>String(s??'').replace(/[&<>"']/g,ch=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
function toast(m,bad=false){const e=$('#toast');e.textContent=m;e.classList.toggle('bad',bad);e.style.display='block';setTimeout(()=>e.style.display='none',4200);}
async function api(url,method='GET',body){
 const opts={method,credentials:'same-origin',headers:{}};
 if(method!=='GET')opts.headers[csrf.headerName]=csrf.token;
 if(body!==undefined){opts.headers['Content-Type']='application/json';opts.body=JSON.stringify(body);}
 const r=await fetch(url,opts);
 if(r.redirected&&r.url.includes('login.html')){location.href='/login.html';throw Error('Sesión vencida');}
 if(r.status===401){location.href='/login.html';throw Error('Inicia sesión');}
 let obj=await r.json().catch(()=>({}));
 if(!r.ok)throw Error(obj.error||`Error HTTP ${r.status}`);
 return obj;
}
async function load(){
 const v=await Promise.all([api('/api/productos'),api('/api/clientes'),api('/api/ventas'),api('/api/alertas')]);
 [data.productos,data.clientes,data.ventas,data.alertas]=v;
 if(isAdmin)data.resumen=await api('/api/reportes/resumen');
 render();
}
function table(headers,rows){
 if(!rows.length)return '<div class="empty">No hay registros para mostrar</div>';
 return '<table><thead><tr>'+headers.map(x=>`<th>${esc(x)}</th>`).join('')+'</tr></thead><tbody>'+rows.map(row=>'<tr>'+row.map(col=>`<td>${col}</td>`).join('')+'</tr>').join('')+'</tbody></table>';
}
function statusProducto(p){
 if(p.fechaVencimiento&&p.fechaVencimiento<new Date().toLocaleDateString('en-CA'))return '<span class="tag bad">Vencido</span>';
 if(p.stockActual<=p.stockMinimo)return '<span class="tag bad">Stock bajo</span>';
 if(p.fechaVencimiento&&Date.parse(p.fechaVencimiento+'T12:00:00')-Date.now()<7*86400000)return '<span class="tag warn">Por vencer</span>';
 return '<span class="tag">Disponible</span>';
}
function render(){
 const vValidas=data.ventas.filter(v=>v.estado==='COMPLETADA');
 const tVentas=vValidas.reduce((a,v)=>a+Number(v.total),0);
 const deuda=data.clientes.reduce((a,c)=>a+Number(c.saldoDeuda),0);
 const criticos=data.productos.filter(p=>p.stockActual<=p.stockMinimo);
 const stats=[['VENTAS ACUMULADAS',money(tVentas),'Ventas registradas'],['TRANSACCIONES',vValidas.length,'Ventas completadas'],['STOCK CRÍTICO',criticos.length,'Productos por reponer'],['DEUDAS PENDIENTES',money(deuda),'Fiado por cobrar']];
 $('#stats').innerHTML=stats.map(x=>`<div class="stat"><small>${x[0]}</small><strong>${x[1]}</strong><em>${x[2]}</em></div>`).join('');
 $('#dashStock').innerHTML=table(['Producto','Stock','Estado'],criticos.slice(0,6).map(p=>[esc(p.nombre),p.stockActual,statusProducto(p)]));
 $('#dashVentas').innerHTML=table(['#','Pago','Total'],data.ventas.slice(0,6).map(v=>['#'+v.id,esc(v.tipoPago),money(v.total)]));
 $('#tablaInventario').innerHTML=renderInventarioRows($('#buscarInventario').value);
 const al=data.alertas;
 $('#tablaAlertas').innerHTML=table(['Producto','Tipo','Mensaje','Estado','Acción'],al.map(a=>{
  const p=data.productos.find(t=>t.id===a.idProducto);
  return [esc(p?.nombre||('Producto #'+a.idProducto)),esc(a.tipo),esc(a.mensaje),a.estado==='PENDIENTE'?'<span class="tag warn">Pendiente</span>':'<span class="tag">Atendida</span>',a.estado==='PENDIENTE'?`<button class="mini-button" data-attend="${a.id}">Atender</button>`:'—'];
 }));
 $('#tablaClientes').innerHTML=table(['Cliente','DNI','Límite','Deuda','Fiado','Acciones'],data.clientes.map(c=>[
  esc(c.nombres),esc(c.dni),money(c.limiteCredito),money(c.saldoDeuda),c.habilitadoFiado?'<span class="tag">Habilitado</span>':'<span class="tag warn">No habilitado</span>',isAdmin?`<button class="mini-button" data-editcliente="${c.id}">Editar</button>`:'—'
 ]));
 $('#tablaVentas').innerHTML=table(['#','Fecha','Pago','Total','Estado','Acciones'],data.ventas.slice(0,40).map(v=>[
 '#'+v.id,esc(v.fechaHora?.replace('T',' ').slice(0,16)),esc(v.tipoPago),money(v.total),v.estado==='COMPLETADA'?'<span class="tag">Completada</span>':'<span class="tag bad">Anulada</span>',
 isAdmin&&v.estado==='COMPLETADA'?`<button class="mini-button" data-cancel="${v.id}">Anular</button>`:'—'
 ]));
 selectProducts();selectClients();renderCart();
 if(isAdmin){
 const re=data.resumen;
 $('#reportStats').innerHTML=[['TOTAL EN VENTAS',money(re?.ventasTotales)],['MARGEN ESTIMADO',money(re?.margenEstimado)],['OPERACIONES',re?.numeroVentas??0],['SALDOS POR COBRAR',money(re?.deudaTotal)]].map(x=>`<div class="stat"><small>${x[0]}</small><strong>${x[1]}</strong></div>`).join('');
 $('#reporteStock').innerHTML=table(['Producto','Actual','Mínimo'],[...data.productos].sort((a,b)=>a.stockActual-b.stockActual).slice(0,8).map(p=>[esc(p.nombre),p.stockActual,p.stockMinimo]));
 $('#reporteDeuda').innerHTML=table(['Cliente','Pendiente'],data.clientes.filter(c=>Number(c.saldoDeuda)>0).map(c=>[esc(c.nombres),money(c.saldoDeuda)]));
 }
}
function renderInventarioRows(filter=''){
 const t=filter.toLocaleLowerCase().trim();
 const ps=data.productos.filter(p=>(p.nombre+' '+p.codigo+' '+p.categoria).toLocaleLowerCase().includes(t));
 return table(['Código','Producto','Categoría','Precio','Stock','Estado','Acción'],ps.map(p=>[
 esc(p.codigo),`<strong>${esc(p.nombre)}</strong>`,esc(p.categoria),money(p.precioVenta),p.stockActual,statusProducto(p),
 isAdmin?`<button class="mini-button" data-editproducto="${p.id}">Editar</button><button class="mini-button" data-ingreso="${p.id}">Reponer</button><button class="mini-button" data-baja="${p.id}">Dar de baja</button>`:'—'
 ]));
}
function selectProducts(){
 const texto=$('#buscarVenta').value.toLocaleLowerCase().trim();const selected=$('#productoVenta').value;
 const ps=data.productos.filter(p=>(p.nombre+' '+p.codigo).toLocaleLowerCase().includes(texto)&&p.stockActual>0);
 $('#productoVenta').innerHTML=ps.map(p=>`<option value="${p.id}">${esc(p.codigo+' · '+p.nombre)} — ${money(p.precioVenta)} (${p.stockActual} und.)</option>`).join('');
 if(ps.some(p=>String(p.id)===selected))$('#productoVenta').value=selected;
}
function selectClients(){
 const x=$('#clienteFiado').value,y=$('#clienteAbono').value;
 $('#clienteFiado').innerHTML='<option value="">Elegir cliente</option>'+data.clientes.map(c=>`<option value="${c.id}">${esc(c.nombres)} · Deuda ${money(c.saldoDeuda)}</option>`).join('');
 $('#clienteAbono').innerHTML='<option value="">Elegir cliente</option>'+data.clientes.map(c=>`<option value="${c.id}">${esc(c.nombres)} · ${money(c.saldoDeuda)}</option>`).join('');
 if(data.clientes.some(c=>String(c.id)===x))$('#clienteFiado').value=x;
 if(data.clientes.some(c=>String(c.id)===y))$('#clienteAbono').value=y;
}
function totalCart(){return cart.reduce((acc,i)=>acc+i.cantidad*Number(data.productos.find(p=>p.id===i.productoId)?.precioVenta||0),0);}
function renderCart(){
 $('#carrito').innerHTML=table(['Producto','Cant.','Subtotal',''],cart.map(i=>{
 const p=data.productos.find(x=>x.id===i.productoId);
 return [esc(p?.nombre),i.cantidad,money(i.cantidad*Number(p?.precioVenta||0)),`<button class="mini-button" data-remove="${i.productoId}">Quitar</button>`];
 }));
 $('#ventaTotal').textContent=money(totalCart());calcularVuelto();
}
function calcularVuelto(){$('#vuelto').textContent=money(Math.max(0,Number($('#recibido').value||0)-totalCart()));}
function go(page){
 if(!$('#page-'+page))return;
 $('.page.current')?.classList.remove('current');$('#page-'+page).classList.add('current');
 document.querySelectorAll('.nav-btn').forEach(b=>b.classList.toggle('active',b.dataset.page===page));
 $('#tituloSeccion').textContent=({'dashboard':'Panel principal','ventas':'Punto de venta','inventario':'Inventario','producto':'Registrar producto','alertas':'Centro de alertas','clientes':'Clientes y fiado','reportes':'Reportes'})[page]||page;
 $('.sidebar').classList.remove('open');if(page==='ventas')$('#buscarVenta').focus();
}
window.go=go;
async function guard(fn){try{await fn()}catch(e){toast(e.message||'Error inesperado',true);console.error(e);}}
function clearProducto(){$('#formProducto').elements.stockActual.disabled=false;$('#formProducto').reset();$('#idProducto').value='';$('#headingProducto').textContent='Registrar producto';}
function clearCliente(){$('#formCliente').reset();$('#clienteEditId').value='';}
function fv(form){return Object.fromEntries(new FormData(form).entries());}
async function init(){
 const auth=await api('/api/yo');isAdmin=auth.rol.includes('ROLE_ADMIN');
 $('#userName').textContent=auth.usuario;$('#userRole').textContent=isAdmin?'ADMIN':'VENDEDOR';
 document.querySelectorAll('.admin-only').forEach(e=>{if(!isAdmin)e.style.display='none'});
 csrf=await api('/api/csrf'); await load();
}
document.querySelectorAll('.nav-btn').forEach(b=>b.addEventListener('click',()=>go(b.dataset.page)));
$('#menuToggle').onclick=()=>$('.sidebar').classList.toggle('open');
$('#exitButton').onclick=()=>guard(async()=>{await api('/logout','POST');location.href='/login.html?logout'});
$('#buscarInventario').addEventListener('input',e=>$('#tablaInventario').innerHTML=renderInventarioRows(e.target.value));
$('#buscarVenta').addEventListener('input',selectProducts);
$('#recibido').addEventListener('input',calcularVuelto);
$('#tipoPago').addEventListener('change',()=>{$('#fiadoBox').style.display=$('#tipoPago').value==='FIADO'?'block':'none';$('#efectivoBox').style.display=$('#tipoPago').value==='EFECTIVO'?'block':'none';});
$('#agregarItem').onclick=()=>{
 const productoId=Number($('#productoVenta').value),cantidad=Number($('#cantidadVenta').value);
 const p=data.productos.find(v=>v.id===productoId);if(!p)return toast('Selecciona un producto',true);
 if(!Number.isInteger(cantidad)||cantidad<1)return toast('Cantidad no válida',true);
 const found=cart.find(i=>i.productoId===productoId);
 const nuevo=cantidad+(found?.cantidad||0);if(nuevo>p.stockActual)return toast('No hay suficiente stock disponible',true);
 if(found)found.cantidad=nuevo;else cart.push({productoId,cantidad});renderCart();
};
$('#registrarVenta').onclick=()=>guard(async()=>{
 if(!cart.length)throw Error('Agrega productos al carrito');
 const pago=$('#tipoPago').value;
 const body={tipoPago:pago,items:cart,clienteId:pago==='FIADO'?Number($('#clienteFiado').value)||null:null,montoRecibido:pago==='EFECTIVO'?Number($('#recibido').value):0};
 if(!confirm('¿Confirmar venta por '+money(totalCart())+'?'))return;
 const v=await api('/api/ventas','POST',body);toast('Venta #'+v.id+' registrada. Total: '+money(v.total));cart=[];await load();
});
$('#formProducto').onsubmit=e=>{e.preventDefault();guard(async()=>{
 const f=fv(e.target);const id=$('#idProducto').value;
 const body={...f,precioCompra:Number(f.precioCompra),precioVenta:Number(f.precioVenta),stockActual:Number(f.stockActual),stockMinimo:Number(f.stockMinimo),fechaVencimiento:f.fechaVencimiento||null};
 await api(id?'/api/productos/'+id:'/api/productos',id?'PUT':'POST',body);toast('Producto guardado correctamente');clearProducto();await load();go('inventario');
 });};
$('#nuevoProducto').onclick=clearProducto;
$('#formCliente').onsubmit=e=>{e.preventDefault();guard(async()=>{
 const f=fv(e.target),id=$('#clienteEditId').value;
 const body={dni:f.dni,nombres:f.nombres,telefono:f.telefono,limiteCredito:Number(f.limiteCredito),habilitadoFiado:$('#formCliente [name=habilitadoFiado]').checked};
 await api(id?'/api/clientes/'+id:'/api/clientes',id?'PUT':'POST',body);toast('Cliente guardado');clearCliente();await load();
 });};
$('#limpiarCliente').onclick=clearCliente;
$('#formAbono').onsubmit=e=>{e.preventDefault();guard(async()=>{
 const f=fv(e.target);if(!f.cliente)throw Error('Selecciona un cliente');
 await api('/api/clientes/'+f.cliente+'/abonos','POST',{monto:Number(f.monto),medioPago:f.medioPago});toast('Abono registrado correctamente');e.target.reset();await load();
 });};
document.addEventListener('click',e=>{
 const b=e.target.closest('button[data-remove],button[data-attend],button[data-editproducto],button[data-ingreso],button[data-baja],button[data-editcliente],button[data-cancel]');if(!b)return;
 guard(async()=>{
 const d=b.dataset;
 if(d.remove){cart=cart.filter(i=>i.productoId!==Number(d.remove));renderCart();}
 if(d.attend){await api('/api/alertas/'+d.attend+'/atender','PATCH');toast('Alerta atendida');await load();}
 if(d.editproducto){const p=data.productos.find(v=>v.id===Number(d.editproducto));if(!p)return;
  clearProducto();$('#idProducto').value=p.id;const f=$('#formProducto');['codigo','nombre','categoria','proveedor','precioCompra','precioVenta','stockActual','stockMinimo','fechaVencimiento'].forEach(k=>f.elements[k].value=p[k]??'');f.elements.stockActual.disabled=true;$('#headingProducto').textContent='Editar producto';go('producto');}
 if(d.ingreso){const n=prompt('Unidades de mercadería que ingresan:','10');if(n===null)return;const cant=Number(n);if(!Number.isInteger(cant)||cant<=0)throw Error('Cantidad incorrecta');await api('/api/productos/'+d.ingreso+'/ingresos','POST',{cantidad:cant,motivo:'Reposición de inventario'});toast('Stock actualizado');await load();}
 if(d.baja){if(!confirm('¿Dar de baja este producto?'))return;await api('/api/productos/'+d.baja+'/baja','PATCH');toast('Producto desactivado');await load();}
 if(d.editcliente){const c=data.clientes.find(v=>v.id===Number(d.editcliente));if(!c)return;clearCliente();$('#clienteEditId').value=c.id;const f=$('#formCliente');['dni','nombres','telefono','limiteCredito'].forEach(k=>f.elements[k].value=c[k]??'');f.elements.habilitadoFiado.checked=c.habilitadoFiado;go('clientes');}
 if(d.cancel){const motivo=prompt('Motivo de anulación:');if(!motivo)return;await api('/api/ventas/'+d.cancel+'/anular','PATCH',{motivo});toast('Venta anulada; stock restituido');await load();}
 });
});
$('#descargaExcel').onclick=()=>toast('Descargando reporte Excel');
init().catch(e=>toast('Error al iniciar: '+e.message,true));
