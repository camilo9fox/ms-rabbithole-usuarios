package com.rabbithole.usuarios.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Importaciones eliminadas - se agregarán cuando se restaure la seguridad OAuth2
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final String clientId;
    private final String issuerUri;
    private final Environment env;
    
    public SecurityConfig(
            @Value("${azure.activedirectory.client-id}") String clientId,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
            Environment env) {
        this.clientId = clientId;
        this.issuerUri = issuerUri;
        this.env = env;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Temporalmente permitir todos los endpoints para depuración
                        .anyRequest().permitAll());
                // Configuración OAuth2 deshabilitada temporalmente para depuración
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Usar JwkSetUri en lugar de issuerLocation para más flexibilidad con algoritmos
        String jwkSetUri = env.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri");
        String clientSecret = env.getProperty("azure.activedirectory.client-secret");
        
        if (jwkSetUri == null) {
            jwkSetUri = "https://login.microsoftonline.com/5497f70a-565c-40b7-92c7-b62049f8638f/discovery/v2.0/keys";
            logger.warn("JWK Set URI no encontrado en propiedades, usando valor por defecto: {}", jwkSetUri);
        }
        
        logger.info("Configurando JwtDecoder con client secret: {}", clientSecret != null ? "[CONFIGURADO]" : "[NO CONFIGURADO]");
        
        // Configurar el decodificador para aceptar múltiples algoritmos
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithms(algorithms -> {
                    // Aceptar algoritmos comunes de firma
                    algorithms.add(SignatureAlgorithm.RS256);
                    algorithms.add(SignatureAlgorithm.RS384);
                    algorithms.add(SignatureAlgorithm.RS512);
                    algorithms.add(SignatureAlgorithm.ES256);
                    algorithms.add(SignatureAlgorithm.ES384);
                    algorithms.add(SignatureAlgorithm.ES512);
                })
                .build();
        
        // Configurar validadores
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(clientId);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
        
        jwtDecoder.setJwtValidator(withAudience);
        
        // Agregar logging para depuración
        logger.info("Configurado JwtDecoder con JWK Set URI: {}", jwkSetUri);
        
        return jwtDecoder;
    }

    // El método jwtAuthenticationConverter se implementará cuando se restaure la seguridad OAuth2

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
