package pe.juans.pos.config;
import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.*;
@RestControllerAdvice
public class ApiErrors {
 @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class,ArithmeticException.class})
 public ResponseEntity<Map<String,String>> negocio(RuntimeException e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<Map<String,String>> validacion(MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("error","Revisa los campos del formulario"));}
 @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
 public ResponseEntity<Map<String,String>> prohibido(Exception e){return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error","Operación reservada al administrador"));}
}
