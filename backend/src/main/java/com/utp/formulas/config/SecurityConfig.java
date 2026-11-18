package com.utp.formulas.config;
import com.utp.formulas.repository.UsuarioRepository;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    SecurityFilterChain filtros(HttpSecurity http, UsuarioRepository usuarios) throws Exception {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> usuarios.findByUsername(jwt.getSubject())
            .map(u -> List.<org.springframework.security.core.GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_" + u.getRol()))).orElse(List.of()));
        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> {});
        http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(a -> a
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll()
            .requestMatchers("/error").permitAll()
            .anyRequest().authenticated());
        http.oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter))
            .authenticationEntryPoint((req, res, ex) -> {
                res.setStatus(401);
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"mensaje\":\"Inicia sesión: token ausente, inválido o vencido\"}");
            }));
        http.exceptionHandling(e -> e.accessDeniedHandler((req, res, ex) -> {
            res.setStatus(403);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"mensaje\":\"Tu rol no puede realizar esta acción\"}");
        }));
        return http.build();
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origin}") String origen) {
        CorsConfiguration config = new CorsConfiguration();
        
        config.setAllowedOrigins(java.util.Arrays.stream(origen.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
