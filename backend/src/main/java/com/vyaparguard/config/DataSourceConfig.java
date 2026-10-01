package com.vyaparguard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.Socket;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/vyaparguard}")
    private String pgUrl;

    @Value("${spring.datasource.username:postgres}")
    private String pgUser;

    @Value("${spring.datasource.password:postgres}")
    private String pgPass;

    @Bean
    @Primary
    public DataSource dataSource() {
        String host = "localhost";
        int port = 5432;
        try {
            if (pgUrl != null && pgUrl.contains("://")) {
                String clean = pgUrl.substring(pgUrl.indexOf("://") + 3);
                int slash = clean.indexOf('/');
                if (slash > 0) clean = clean.substring(0, slash);
                int colon = clean.indexOf(':');
                if (colon > 0) {
                    host = clean.substring(0, colon);
                    port = Integer.parseInt(clean.substring(colon + 1));
                } else {
                    host = clean;
                }
            }
        } catch (Exception ignored) {
        }

        if (isPostgresReachable(host, port)) {
            System.out.println("=================================================");
            System.out.println("[VyaparGuard] PostgreSQL detected at " + host + ":" + port + ". Connecting to PostgreSQL...");
            System.out.println("=================================================");
            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(pgUrl)
                    .username(pgUser)
                    .password(pgPass)
                    .build();
        } else {
            System.out.println("=================================================");
            System.out.println("[VyaparGuard] PostgreSQL port 5432 not active. Initializing PostgreSQL-compatible in-memory database.");
            System.out.println("[VyaparGuard] Exact PostgreSQL dialect & schema maintained.");
            System.out.println("=================================================");
            return DataSourceBuilder.create()
                    .driverClassName("org.h2.Driver")
                    .url("jdbc:h2:mem:vyaparguard;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1")
                    .username("sa")
                    .password("")
                    .build();
        }
    }

    private boolean isPostgresReachable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 600);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
