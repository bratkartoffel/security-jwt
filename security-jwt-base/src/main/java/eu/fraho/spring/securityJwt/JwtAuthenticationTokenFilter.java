package eu.fraho.spring.securityJwt;

import eu.fraho.spring.securityJwt.service.JwtTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailsService = null;

    @Autowired
    private JwtTokenService jwtTokenUtil = null;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        Optional<String> authToken = jwtTokenUtil.getToken(request);
        SecurityContextHolder.getContext().setAuthentication(null);

        authToken.ifPresent(token -> {
            if (jwtTokenUtil.validateToken(token)) {
                log.debug("Provided token is valid");
                jwtTokenUtil.parseUser(token).ifPresent(jwtUser -> {
                    log.debug("Successfully used token to authenticate {}", jwtUser.getUsername());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(jwtUser, authToken, jwtUser.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            } else {
                log.debug("Provided token is invalid");
            }
        });

        chain.doFilter(request, response);
    }
}