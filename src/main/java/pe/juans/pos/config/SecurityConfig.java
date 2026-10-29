package pe.juans.pos.config;

import pe.juans.pos.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean UserDetailsService userDetailsService(UsuarioRepository repo) {
        return username -> repo.findByUsernameAndActivoTrue(username)
            .map(u -> User.withUsername(u.username).password(u.passwordHash)
                .roles(u.rol).disabled(!u.activo).build())
            .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuario no encontrado"));
    }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
        http.csrf(c -> c.csrfTokenRepository(csrf).csrfTokenRequestHandler(handler))
            .authorizeHttpRequests(a -> a.requestMatchers("/login.html", "/assets/**", "/api/csrf", "/error").permitAll().anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login.html").loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true).failureUrl("/login.html?error").permitAll())
            .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login.html?logout"))
            .sessionManagement(s -> s.sessionFixation(f -> f.migrateSession()));
        // Ubicarlo después de autenticación anónima permite leer el usuario del contexto
        // y antes de autorización permite identificar accesos denegados.
        http.addFilterAfter(new MdcRequestFilter(), AnonymousAuthenticationFilter.class);
        return http.build();
    }
}
