
# Aegis Patrimônio ������️



<div align="center">

  

![Aegis Patrimônio](https://via.placeholder.com/800x200/1a3e72/ffffff?text=Aegis+Patrimônio)

*Sistema completo de gestão patrimonial com controle de ativos, fornecedores e localizações*



[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)](https://spring.io/projects/spring-boot)

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://openjdk.org/projects/jdk/17/)

[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)](https://www.mysql.com/)

[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)



</div>



## ��� Sobre o Projeto



O **Aegis Patrimônio** é um sistema robusto desenvolvido em Spring Boot para gestão completa do patrimônio institucional, ofereciendo controle detalhado de ativos, fornecedores, localizações e tipos de equipamentos.



### ��� Objetivos

- ✅ Centralizar o controle patrimonial em uma única plataforma

- ✅ Automatizar processos manuais de inventário

- ✅ Fornecer relatórios gerenciais em tempo real

- ✅ Facilitar a localização e rastreamento de ativos

- ✅ Suportar múltiplas coligadas e departamentos



## ���️ Tecnologias Utilizadas



### Backend

- **Java 17** - Linguagem de programação

- **Spring Boot 3.2.4** - Framework principal

- **Spring Data JPA** - Persistência de dados

- **Spring Validation** - Validações de entrada

- **Lombok** - Redução de boilerplate code

- **Maven** - Gerenciamento de dependências



### Banco de Dados

- **MySQL 8.0** - Banco de dados relacional

- **Flyway** - Migrações e versionamento do banco



### Frontend (Planejado)

- **Vue.js 3** - Framework frontend

- **Bootstrap 5** - UI framework

- **Chart.js** - Gráficos e dashboards



## ���️ Estrutura do Projeto

aegispatrimonio/

├── src/main/java/br/com/aegispatrimonio/

│ ├── dto/

│ │ ├── request/ # DTOs de entrada

│ │ │ ├── AtivoRequestDTO.java

│ │ │ ├── FornecedorRequestDTO.java

│ │ │ ├── LocalizacaoRequestDTO.java

│ │ │ └── TipoAtivoRequestDTO.java

│ │ └── response/ # DTOs de saída

│ │ ├── AtivoResponseDTO.java

│ │ ├── FornecedorResponseDTO.java

│ │ ├── LocalizacaoResponseDTO.java

│ │ └── TipoAtivoResponseDTO.java

│ ├── model/ # Entidades JPA

│ ├── repository/ # Interfaces Spring Data JPA

│ ├── service/ # Lógica de negócio

│ │ └── AtivoService.java

│ ├── controller/ # Endpoints REST (em desenvolvimento)

│ └── exception/ # Tratamento de exceções

├── src/main/resources/

│ ├── application.properties # Configurações

│ └── db/migration/ # Scripts Flyway

├── pom.xml # Dependências Maven

└── README.md



text



## ��� Modelo de Dados Principais



### Entidades Implementadas

- **Ativo** - Patrimônio com dados completos

- **Fornecedor** - Empresas fornecedoras

- **Localizacao** - Locais físicos dos ativos  

- **TipoAtivo** - Categorização dos bens



### Relacionamentos

Ativo [N:1] TipoAtivo

Ativo [N:1] Localizacao

Ativo [N:1] Fornecedor

Localizacao [N:1] Localizacao (hierarquia)



text



## ⚙️ Configuração e Instalação



### Pré-requisitos

- Java 17 JDK

- Maven 3.6+

- MySQL 8.0+

- Git



### 1. Clone o repositório

```bash

git clone https://github.com/diogo-rp-menezes/aegispatrimonio.git

cd aegispatrimonio

2. Configure o banco MySQL

sql

CREATE DATABASE aegis_db;

CREATE USER 'aegis_user'@'localhost' IDENTIFIED BY 'password';

GRANT ALL PRIVILEGES ON aegis_db.* TO 'aegis_user'@'localhost';

FLUSH PRIVILEGES;

3. Configure a aplicação

Edite src/main/resources/application.properties:



properties

# Datasource

spring.datasource.url=jdbc:mysql://localhost:3306/aegis_db

spring.datasource.username=aegis_user

spring.datasource.password=password



# JPA

spring.jpa.hibernate.ddl-auto=validate

spring.jpa.show-sql=true



# Flyway

spring.flyway.enabled=true

spring.flyway.locations=classpath:db/migration

4. Execute a aplicação

bash

mvn spring-boot:run

5. Acesse a API

text

http://localhost:8080/api/

��� Funcionalidades

✅ Implementadas

CRUD completo de Ativos



Gestão de Fornecedores



Controle de Localizações



Categorização por Tipo de Ativo



Validações com Bean Validation



DTOs para request/response



Migrações com Flyway



��� Em Desenvolvimento

Endpoints RESTful



Controladores para cada entidade



Autenticação e autorização



Frontend em Vue.js



Relatórios PDF



Integração com leitor de QR Code



��� API Endpoints (Exemplos)

Ativos

http

GET    /api/ativos          # Listar todos ativos

POST   /api/ativos          # Criar novo ativo

GET    /api/ativos/{id}     # Buscar ativo por ID

PUT    /api/ativos/{id}     # Atualizar ativo

DELETE /api/ativos/{id}     # Excluir ativo

Fornecedores

http

GET    /api/fornecedores    # Listar fornecedores

POST   /api/fornecedores    # Criar fornecedor

��� Testes

bash

# Executar testes unitários

mvn test



# Executar com coverage

mvn jacoco:report



# Gerar documentação

mvn javadoc:javadoc

��� Contribuição

Fork o projeto



Crie uma branch: git checkout -b feature/nova-funcionalidade



Commit suas mudanças: git commit -m 'Adiciona nova funcionalidade'



Push para a branch: git push origin feature/nova-funcionalidade



Abra um Pull Request



Padrões de Commit

feat: Nova funcionalidade



fix: Correção de bug



docs: Documentação



style: Formatação de código



refactor: Refatoração de código



test: Testes



��� Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para detalhes.



���‍��� Desenvolvedor

Diogo Menezes



��� Email: diogorpm@gmail.com



��� GitHub: @diogo-rp-menezes



��� LinkedIn: Diogo Menezes



���‍♂️ Suporte

Para dúvidas ou sugestões:



Abra uma issue



Envie um email para diogorpm@gmail.com



<div align="center">

⭐️ Se este projeto te ajudou, deixe uma estrela no repositório!



"Proteção digital para seu patrimônio físico" ���️



</div> ``` EOF
