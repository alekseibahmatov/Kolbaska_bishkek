package ee.kolbaska.kolbaska.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class RequestContextFilter implements Filter {

    private static final ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<>();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        requestHolder.set(request);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            requestHolder.remove();
        }
    }

    public static HttpServletRequest getCurrentRequest() {
        return requestHolder.get();
    }
}
