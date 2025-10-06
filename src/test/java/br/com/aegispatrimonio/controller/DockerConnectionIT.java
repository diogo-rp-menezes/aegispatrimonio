package br.com.aegispatrimonio.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@DisplayName("Teste de Diagnóstico da Conexão Docker")
class DockerConnectionIT {

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0");

    @Test
    @DisplayName("Deve iniciar o contêiner do MySQL e verificar se ele está rodando")
    void deve_conectar_e_iniciar_o_container() {
        // Este teste simplesmente verifica se o Testcontainers conseguiu iniciar o contêiner.
        // Se este teste passar, a conexão Java <-> Docker está funcionando.
        assertTrue(mysqlContainer.isRunning());
        System.out.println("SUCESSO: O contêiner Docker foi iniciado em: " + mysqlContainer.getJdbcUrl());
    }
}
