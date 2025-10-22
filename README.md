
# Aegis Patrimônio



<div align="center">



Sistema completo de gestão patrimonial com controle de ativos, fornecedores e localizações



</div>



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

Java 17 - Linguagem de programação



Spring Boot 3.2.4 - Framework principal



Spring Data JPA - Persistência de dados



Spring Validation - Validações de entrada



Lombok - Redução de boilerplate code



Maven - Gerenciamento de dependências



### Banco de Dados

MySQL 8.0 - Banco de dados relacional



Flyway - Migrações e versionamento do banco



### Frontend (Planejado)

Vue.js 3 - Framework frontend



Bootstrap 5 - UI framework



Chart.js - Gráficos e dashboards



## Funcionalidades



### ✅ Implementadas

CRUD completo de Ativos



Gestão de Fornecedores



Controle de Localizações



Categorização por Tipo de Ativo



Validações com Bean Validation



DTOs para request/response



Migrações com Flyway



### Em Desenvolvimento

Endpoints RESTful



Controladores para cada entidade



Autenticação e autorização



Frontend em Vue.js



Relatórios PDF



Integração com leitor de QR Code



## Licença



Este projeto está sob a licença MIT. Veja o arquivo LICENSE para detalhes.



## Desenvolvedor



Diogo Menezes



Email: diogorpm@gmail.com



GitHub: @diogo-rp-menezes



<div align="center">

⭐️ Se este projeto te ajudou, deixe uma estrela no repositório!



"Proteção digital para seu patrimônio físico"

</div>



# Aegis Patrimônio



<div align="center">



Sistema completo de gestão patrimonial com controle de ativos, fornecedores e localizações



</div>



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

Java 21 - Linguagem de programação



Spring Boot 3.3.0 - Framework principal



Spring Data JPA - Persistência de dados



Spring Validation - Validações de entrada



Lombok - Redução de boilerplate code



Maven - Gerenciamento de dependências



### Banco de Dados

MySQL 8.0 - Banco de dados relacional



Flyway - Migrações e versionamento do banco



### Frontend (Planejado)

Vue.js 3 - Framework frontend



Bootstrap 5 - UI framework



Chart.js - Gráficos e dashboards



## Funcionalidades



### ✅ Implementadas

CRUD completo de Ativos



Gestão de Fornecedores



Controle de Localizações



Categorização por Tipo de Ativo



Validações com Bean Validation



DTOs para request/response



Migrações com Flyway



### Em Desenvolvimento

Endpoints RESTful



Controladores para cada entidade



Autenticação e autorização



Frontend em Vue.js



Relatórios PDF



Integração com leitor de QR Code



## Base Path da API e exemplos

- Base path atual: /api/v1
- Documentação OpenAPI: http://localhost:8080/swagger-ui.html

Exemplos cURL (substitua TOKEN por um JWT válido):

- Listar ativos (paginado)
  curl -X GET "http://localhost:8080/api/v1/ativos?page=0&size=20" -H "Authorization: Bearer TOKEN"

- Buscar ativo por ID
  curl -X GET "http://localhost:8080/api/v1/ativos/1" -H "Authorization: Bearer TOKEN"

- Criar ativo (ADMIN)
  curl -X POST "http://localhost:8080/api/v1/ativos" -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{...}"

- Atualizar ativo (ADMIN)
  curl -X PUT "http://localhost:8080/api/v1/ativos/1" -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{...}"

- Deletar ativo (ADMIN)
  curl -X DELETE "http://localhost:8080/api/v1/ativos/1" -H "Authorization: Bearer TOKEN"

- Health-check do ativo
  curl -X PATCH "http://localhost:8080/api/v1/ativos/1/health-check" -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d "{...}"

## Execução local

1) Banco de dados
- Configure um MySQL local ou use docker-compose up -d para subir os serviços.

2) Variáveis de ambiente
- SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/aegis-patrimonio?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
- SPRING_DATASOURCE_USERNAME=seu_usuario
- SPRING_DATASOURCE_PASSWORD=sua_senha
- JWT_SECRET=um_segredo_forte
- APP_CORS_ALLOWED_ORIGINS=http://localhost:8080,http://localhost:3000

3) Rodar aplicação
- ./mvnw spring-boot:run

4) Rodar testes e cobertura
- ./mvnw -DskipTests=false verify
- Abra target/site/jacoco/index.html para ver o relatório de cobertura

## Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para detalhes.

## Desenvolvedor

Diogo Menezes

Email: diogorpm@gmail.com

GitHub: @diogo-rp-menezes

<div align="center">

⭐️ Se este projeto te ajudou, deixe uma estrela no repositório!

"Proteção digital para seu patrimônio físico"

</div>
