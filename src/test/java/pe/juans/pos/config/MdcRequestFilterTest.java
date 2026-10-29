package pe.juans.pos.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class MdcRequestFilterTest {
    private final MdcRequestFilter filter = new MdcRequestFilter();

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    void poneUsuarioOperacionYRequestIdDurantePeticionYLimpiaAlFinal() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "[oculta]",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        MockHttpServletRequest req = peticion("POST", "/api/ventas");
        MockHttpServletResponse res = new MockHttpServletResponse();
        AtomicReference<String> id = new AtomicReference<>();
        FilterChain chain = (request, response) -> {
            assertEquals("admin", MDC.get("usuario"));
            assertEquals("REGISTRAR_VENTA", MDC.get("operacion"));
            id.set(MDC.get("requestId"));
        };
        filter.doFilter(req, res, chain);
        assertNotNull(id.get());
        assertFalse(id.get().isBlank());
        assertNull(MDC.get("usuario"));
        assertNull(MDC.get("operacion"));
        assertNull(MDC.get("requestId"));
    }

    @Test
    void restauraContextoPrevioInclusoCuandoOcurreExcepcion() {
        MDC.put("origen", "prueba");
        MockHttpServletRequest req = peticion("POST", "/api/clientes/10/abonos");
        MockHttpServletResponse res = new MockHttpServletResponse();
        assertThrows(IllegalStateException.class, () -> filter.doFilter(req, res, (request, response) -> {
            assertEquals("anonimo", MDC.get("usuario"));
            assertEquals("REGISTRAR_ABONO", MDC.get("operacion"));
            throw new IllegalStateException("Error simulado");
        }));
        assertEquals("prueba", MDC.get("origen"));
        assertNull(MDC.get("usuario"));
        assertNull(MDC.get("requestId"));
        assertNull(MDC.get("operacion"));
    }

    @Test
    void noInterfiereConArchivosEstaticos() throws Exception {
        MockHttpServletRequest req = peticion("GET", "/assets/app.js");
        filter.doFilter(req, new MockHttpServletResponse(), (request, response) ->
                assertNull(MDC.get("requestId")));
        assertNull(MDC.get("requestId"));
    }

    private static MockHttpServletRequest peticion(String metodo, String ruta) {
        MockHttpServletRequest req = new MockHttpServletRequest(metodo, ruta);
        req.setServletPath(ruta);
        return req;
    }
}
