package pe.juans.pos.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Añade contexto diagnóstico a cada petición de API sin guardar credenciales ni
 * parámetros. Se incorpora una sola vez a la cadena de Spring Security.
 */
public class MdcRequestFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(MdcRequestFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // Los hilos del servidor se reutilizan: jamás dejarlos con MDC de otro usuario.
        Map<String, String> previous = MDC.getCopyOfContextMap();
        try {
            MDC.put("requestId", UUID.randomUUID().toString());
            MDC.put("usuario", usuarioActual());
            MDC.put("operacion", tipoOperacion(request));
            chain.doFilter(request, response);
        } finally {
            Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            // El patrón de ruta no incluye IDs, datos de búsqueda ni parámetros.
            String ruta = pattern instanceof String ? (String) pattern : "/api/**";
            try {
                log.info("Petición API método={} ruta={} estadoHTTP={}", request.getMethod(), ruta, response.getStatus());
            } finally {
                if (previous == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previous);
                }
            }
        }
    }

    private static String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "anonimo";
        }
        // Limita caracteres de control y saltos de línea en el registro.
        String username = auth.getName().replaceAll("[^a-zA-Z0-9_.-]", "_");
        return username.length() > 50 ? username.substring(0, 50) : username;
    }

    private static String tipoOperacion(HttpServletRequest request) {
        String metodo = request.getMethod();
        String ruta = request.getServletPath();
        if ("POST".equals(metodo) && "/api/ventas".equals(ruta)) return "REGISTRAR_VENTA";
        if ("PATCH".equals(metodo) && ruta.matches("/api/ventas/[0-9]+/anular")) return "ANULAR_VENTA";
        if ("POST".equals(metodo) && ruta.matches("/api/clientes/[0-9]+/abonos")) return "REGISTRAR_ABONO";
        if ("POST".equals(metodo) && "/api/productos".equals(ruta)) return "CREAR_PRODUCTO";
        if ("PUT".equals(metodo) && ruta.matches("/api/productos/[0-9]+")) return "EDITAR_PRODUCTO";
        if ("POST".equals(metodo) && ruta.matches("/api/productos/[0-9]+/ingresos")) return "REPONER_STOCK";
        if ("PATCH".equals(metodo) && ruta.matches("/api/alertas/[0-9]+/atender")) return "ATENDER_ALERTA";
        if ("GET".equals(metodo)) return "CONSULTAR_API";
        return "API_" + metodo.replaceAll("[^A-Z]", "");
    }
}
