package com.rabbithole.usuarios.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuración de la conexión a Oracle.
 * Esta clase se encarga de configurar el datasource para conectarse a Oracle
 * usando el wallet.
 */
@Configuration
public class OracleConnectionConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * Configura y crea el datasource para Oracle con el wallet.
     * 
     * @return Datasource configurado
     */
    @Bean
    @Primary
    public DataSource getDataSource() {
        // Asegurar que la ruta al wallet es absoluta
        String walletPath = "./Wallet_C3CQ5Y7AELCSQGMU";
        File walletDir = new File(walletPath);

        // Si la ruta relativa no funciona, intentamos con una ruta absoluta
        if (!walletDir.exists()) {
            Path currentPath = Paths.get("").toAbsolutePath();
            walletPath = currentPath.toString() + File.separator + "Wallet_C3CQ5Y7AELCSQGMU";
            walletDir = new File(walletPath);
        }

        // Verificar si el directorio del wallet existe
        if (!walletDir.exists()) {
            throw new RuntimeException("Wallet directory not found: " + walletPath);
        }

        // Configurar la propiedad TNS_ADMIN para Oracle
        System.setProperty("oracle.net.tns_admin", walletPath);

        // Construir y devolver el datasource
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }
}
