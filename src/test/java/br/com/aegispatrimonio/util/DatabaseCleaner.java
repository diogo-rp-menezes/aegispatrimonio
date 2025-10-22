package br.com.aegispatrimonio.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DatabaseCleaner {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void clearTables() {
        entityManager.flush();
        
        // Desabilita constraints temporariamente
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        
        // Obtém todas as tabelas e limpa
        List<String> tableNames = getTableNames();
        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }
        
        // Reabilita constraints
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    private List<String> getTableNames() {
        // Especifica o tipo de resultado para a query nativa para evitar warnings de "unchecked operations".
        // Isso garante type safety e adere às boas práticas do JPA.
        return entityManager
                .createNativeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE()", String.class)
                .getResultList();
    }
}