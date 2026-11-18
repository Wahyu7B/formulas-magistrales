package com.utp.formulas.config;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

@Configuration
public class JwtConfig {
    @Bean
    SecretKeySpec clave(@Value("${app.jwt.secret}") String secreto) {
        if (secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET requiere al menos 32 bytes");
        }
        return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
    @Bean
    JwtEncoder codificador(SecretKeySpec clave) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
    }
    @Bean
    JwtDecoder decodificador(SecretKeySpec clave) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("formulas-avance2"));
        return decoder;
    }
}
