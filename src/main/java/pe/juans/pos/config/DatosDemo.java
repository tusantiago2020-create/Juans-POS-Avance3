package pe.juans.pos.config;
import pe.juans.pos.model.*;import pe.juans.pos.repository.*;
import org.springframework.boot.CommandLineRunner;import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;import java.time.LocalDate;
@Configuration
public class DatosDemo {
 @Bean CommandLineRunner cargarDemo(UsuarioRepository ur,ProductoRepository pr,ClienteRepository cr,PasswordEncoder encoder,
  @Value("${juans.demo.seed:false}") boolean demo){return args->{if(!demo)return;
   if(ur.count()==0){
    Usuario a=new Usuario();a.username=" ";a.nombres=" ";a.rol=" ";a.passwordHash=encoder.encode(" ");ur.save(a);
    Usuario b=new Usuario();b.username=" ";b.nombres="  ";b.rol=" ";b.passwordHash=encoder.encode(" ");ur.save(b);
   }
   if(pr.count()==0){producto(pr,"P001","Arroz 1 kg","Abarrotes", "3.60","5.00",25,8,null);
    producto(pr,"P002","Leche evaporada","Lácteos","3.20","4.50",8,10,LocalDate.now().plusDays(4));
    producto(pr,"P003","Aceite 1 L","Abarrotes","7.00","9.50",15,5,null);
    producto(pr,"P004","Gaseosa 1.5 L","Bebidas","4.00","6.00",22,6,null);
    producto(pr,"P005","Pan de molde","Panadería","4.10","6.50",5,6,LocalDate.now().plusDays(5));
   }
   if(cr.count()==0){Cliente c=new Cliente();c.dni="12345678";c.nombres="Cliente de prueba";c.telefono="";c.limiteCredito=new BigDecimal("150.00");c.habilitadoFiado=true;cr.save(c);}
  };}
 private static void producto(ProductoRepository r,String codigo,String nombre,String cat,String compra,String venta,int stock,int minimo,LocalDate vence){
  Producto p=new Producto();p.codigo=codigo;p.nombre=nombre;p.categoria=cat;p.precioCompra=new BigDecimal(compra);p.precioVenta=new BigDecimal(venta);
  p.stockActual=stock;p.stockMinimo=minimo;p.fechaVencimiento=vence;r.save(p);}
}
