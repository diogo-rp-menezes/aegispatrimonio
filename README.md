# Aegis Patrimônio

<div align="center">

Sistema completo de gestão patrimonial com controle de ativos, fornecedores e localizações

</div>

## Status do CI e Cobertura

- Status do CI (GitHub Actions):

  [![CI](https://github.com/OWNER/REPO/actions/workflows/ci.yml/badge.svg)](https://github.com/OWNER/REPO/actions/workflows/ci.yml)

  *(Substitua OWNER/REPO pelo caminho do repositório no GitHub para ativar o badge)*

- Cobertura (gate mínimo):

  [![Coverage ≥ 80%](https://img.shields.io/badge/coverage-%E2%89%A580%25-brightgreen)](#)

## Sobre o Projeto

O Aegis Patrimônio é um sistema robusto desenvolvido em Spring Boot para gestão completa do patrimônio institucional, oferecendo controle detalhado de ativos, fornecedores, localizações e tipos de equipamentos.

## Objetivos

✅ Centralizar o controle patrimonial em uma única plataforma

✅ Automatizar processos manuais de inventário

✅ Fornecer relatórios gerenciais em tempo real

✅ Facilitar a localização e rastreamento de ativos

✅ Suportar múltiplas coligadas e departamentos

## Tecnologias Utilizadas

### Backend
- **Java 21** - Linguagem de programação
- **Spring Boot 3.3.0** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Spring Security + JWT** - Autenticação e Autorização
- **Hibernate Envers** - Auditoria de dados
- **Lombok** - Redução de boilerplate code
- **Maven** - Gerenciamento de dependências

### Banco de Dados
- **MySQL 8.0** - Banco de dados relacional
- **Flyway** - Migrações e versionamento do banco

### Frontend
- **Vue.js 3** - Framework frontend
- **Bootstrap 5** - UI framework
- **Pinia** - Gerenciamento de estado
- **Vite** - Build tool

## Funcionalidades

### ✅ Implementadas
- **Gestão de Ativos:** CRUD completo com detalhes de hardware e depreciação.
- **Gestão de Fornecedores:** Cadastro e manutenção de fornecedores.
- **Controle de Localizações:** Hierarquia de filiais e locais.
- **Tipos de Ativo:** Categorização e ícones.
- **Autenticação e Autorização:** JWT, RBAC (Role-Based Access Control) e Multi-tenancy.
- **Auditoria:** Rastreamento de alterações com Hibernate Envers.
- **Validações:** Bean Validation e regras de negócio consistentes.
- **API RESTful:** Endpoints padronizados (V1) e documentados.

### 🚀 Roadmap (Em Breve)
- **Frontend Completo:** Integração total do Vue.js com o backend.
- **Relatórios PDF:** Geração de termos de responsabilidade e relatórios gerenciais.
- **QR Code:** Integração para leitura e etiquetação de ativos.

## Base Path da API e Exemplos

- **Base path atual:** `/api/v1`
- **Documentação OpenAPI:** `http://localhost:8080/swagger-ui.html`

### Exemplos cURL (substitua `TOKEN` por um JWT válido)

**Listar ativos (paginado):**
```bash
curl -X GET "http://localhost:8080/api/v1/ativos?page=0&size=20" \
  -H "Authorization: Bearer TOKEN"
```

**Listar ativos com filtros:**
```bash
# Por filial e status
curl -X GET "http://localhost:8080/api/v1/ativos?filialId=1&status=ATIVO" \
  -H "Authorization: Bearer TOKEN"
```

**Buscar ativo por ID:**
```bash
curl -X GET "http://localhost:8080/api/v1/ativos/1" \
  -H "Authorization: Bearer TOKEN"
```

### Formato de Erros
A API retorna erros padronizados conforme **RFC 7807** (`application/problem+json`).

## Execução Local

### Opção 1: Docker (Recomendado)
1. Build da imagem:
   ```bash
   docker build -t aegispatrimonio:latest .
   ```
2. Subir com Docker Compose:
   ```bash
   docker compose up -d
   ```
   Acesse a API em `http://localhost:8080`.

### Opção 2: Maven (Local)
1. Configure o banco de dados MySQL localmente ou via Docker.
2. Defina as variáveis de ambiente necessárias (`SPRING_DATASOURCE_URL`, `JWT_SECRET`, etc.) ou ajuste `application.properties`.
3. Execute:
   ```bash
   ./mvnw spring-boot:run
   ```

### Kubernetes
1. Aplique os manifestos:
   ```bash
   kubectl apply -f k8s/aegis-app.yaml
   ```
2. (Local) Faça port-forward:
   ```bash
   kubectl port-forward svc/aegis-app -n aegis 8080:80
   ```

## Testes

### Executar Testes
```bash
./mvnw clean verify
```

### TestContainers (Reuso)
Habilitamos o reuso de containers para acelerar testes locais. O arquivo `.testcontainers.properties` já está configurado na raiz.

## Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para detalhes.

## Desenvolvedor

**Diogo Menezes**
- Email: diogorpm@gmail.com
- GitHub: [@diogo-rp-menezes](https://github.com/diogo-rp-menezes)

<div align="center">

⭐️ Se este projeto te ajudou, deixe uma estrela no repositório!

*"Proteção digital para seu patrimônio físico"*

</div>
