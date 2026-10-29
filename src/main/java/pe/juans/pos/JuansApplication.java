package pe.juans.pos;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class JuansApplication {
    public static void main(String[] args) { SpringApplication.run(JuansApplication.class, args); }
}
