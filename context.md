# Contexto do Projeto: Aegis Patrimônio

## Visão Geral

O Aegis Patrimônio é um sistema de gestão de ativos construído com Spring Boot. O objetivo é fornecer uma plataforma centralizada para rastrear e gerenciar ativos, fornecedores e suas localizações.

## Arquitetura e Tecnologias

### Backend

*   **Framework:** Spring Boot 3.3.0
*   **Linguagem:** Java 21
*   **Persistência de Dados:** Spring Data JPA com Hibernate
*   **Banco de Dados:** MySQL com migrações gerenciadas pelo Flyway
*   **Validação:** Spring Validation (Bean Validation)
*   **Build:** Maven
*   **Outras dependências:** Lombok para redução de código boilerplate.

### Frontend (Planejado)

*   **Framework:** Vue.js 3
*   **UI:** Bootstrap 5
*   **Gráficos:** Chart.js

## Estrutura do Projeto

O projeto segue uma estrutura padrão de projetos Spring Boot:

*   `src/main/java`: Código-fonte da aplicação.
*   `src/main/resources`: Arquivos de configuração, incluindo `application.properties` e as migrações do Flyway em `db/migration`.
*   `src/test/java`: Testes unitários e de integração.
*   `pom.xml`: Arquivo de configuração do Maven, definindo as dependências e o processo de build.
*   `frontend/`: Diretório reservado para o futuro desenvolvimento do frontend com Vue.js.

## Funcionalidades

### Implementadas

*   Operações CRUD (Create, Read, Update, Delete) para as entidades principais: Ativos, Fornecedores, Localizações e Tipos de Ativo.
*   Uso de DTOs (Data Transfer Objects) para a comunicação entre as camadas da aplicação.
*   Validações de dados de entrada.

### Em Desenvolvimento

*   Exposição de endpoints RESTful para todas as entidades.
*   Implementação de autenticação e autorização de usuários.
*   Desenvolvimento da interface de usuário com Vue.js.
*   Geração de relatórios em PDF.
*   Integração com leitores de QR Code para facilitar a identificação de ativos.

## Objetivos Futuros

*   Automatizar o processo de inventário.
*   Fornecer relatórios gerenciais em tempo real.
*   Suportar a gestão de ativos em múltiplas filiais e departamentos.
