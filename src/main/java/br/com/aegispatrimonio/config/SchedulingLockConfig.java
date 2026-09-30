package br.com.aegispatrimonio.config;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;

import javax.sql.DataSource;

/**
 * H3 (audit 2026-09-30): configura o lock distribuído (ShedLock) para jobs
 * agendados, garantindo execução em apenas uma réplica quando o k8s escala
 * a aplicação (replicas: 2). A tabela shedlock é criada pela migration
 * V19__create_shedlock_table.sql.
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
public class SchedulingLockConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime()
                        .build()
        );
    }
}
