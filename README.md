
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

- Listar ativos com filtros (qualquer combinação)
  # Por filial
  curl -X GET "http://localhost:8080/api/v1/ativos?filialId=1" -H "Authorization: Bearer TOKEN"
  # Por tipo de ativo
  curl -X GET "http://localhost:8080/api/v1/ativos?tipoAtivoId=2" -H "Authorization: Bearer TOKEN"
  # Por status (ATIVO, BAIXADO, etc.)
  curl -X GET "http://localhost:8080/api/v1/ativos?status=ATIVO" -H "Authorization: Bearer TOKEN"
  # Combinado com paginação e ordenação
  curl -X GET "http://localhost:8080/api/v1/ativos?filialId=1&status=ATIVO&page=0&size=10&sort=nome,asc" -H "Authorization: Bearer TOKEN"

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

### Formato de erros (application/problem+json)
- A API retorna erros padronizados conforme RFC 7807.
- Inclui um correlationId (header opcional X-Correlation-Id ou gerado pelo servidor) para rastreabilidade.
- Exemplo de resposta 400 de validação:
  {
    "type": "about:blank",
    "title": "Bad Request",
    "status": 400,
    "detail": "Validation failed",
    "errors": [ { "field": "nome", "message": "não deve estar em branco" } ],
    "correlationId": "d4f8e2c8-0b0a-4f0a-bb7a-2a1c6f1e9a2b"
  }

## Execução local

### Docker (local)
- Build da imagem: `docker build -t aegispatrimonio:latest .`
- Subir com Docker Compose (MySQL + app): `docker compose up -d`
- Variáveis importantes (já configuradas no compose):
  - SPRING_PROFILES_ACTIVE=prod
  - JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=25 -XX:MaxRAMPercentage=75 -XX:MaxMetaspaceSize=256m -XX:+UseStringDeduplication -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp -Dfile.encoding=UTF-8"
  - A aplicação é container-aware (Java 21). Se você alocar 4GiB ao container, a JVM usará até ~75% disso como heap (ajustável via MaxRAMPercentage).

### Kubernetes
1) Ajuste a imagem no arquivo k8s/aegis-app.yaml (campo `image:`). Por padrão está `aegispatrimonio:latest` para uso com `kind`/`minikube` carregando a imagem local.
2) Aplique os manifests:
```
kubectl apply -f k8s/aegis-app.yaml
```
3) Recursos e probes:
- Deployment com 2 réplicas, readiness em /actuator/health/readiness e liveness em /actuator/health/liveness.
- Limites de recursos: memória 4Gi (`limits.memory: 4Gi`) e CPU 1 vCPU.
- JVM ajustada via JAVA_TOOL_OPTIONS para respeitar memória do container.
4) Exposição do serviço:
- Service ClusterIP na porta 80 → 8080. Em ambiente local, use `kubectl port-forward svc/aegis-app -n aegis 8080:80` e acesse http://localhost:8080.

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


## Status do CI e Cobertura

- Status do CI (GitHub Actions):
  
  [![CI](https://github.com/OWNER/REPO/actions/workflows/ci.yml/badge.svg)](https://github.com/OWNER/REPO/actions/workflows/ci.yml)

  Substitua OWNER/REPO pelo caminho do repositório no GitHub (por exemplo, diogo-rp-menezes/aegispatrimonio) para ativar o badge.

- Cobertura (gate mínimo):
  
  [![Coverage ≥ 80%](https://img.shields.io/badge/coverage-%E2%89%A580%25-brightgreen)](#)

  O pipeline publica o relatório HTML do JaCoCo como artifact (target/site/jacoco). Abra o artifact jacoco-report-html no job do CI para visualizar os detalhes de cobertura.


# Aegis Patrimônio

## TestContainers — Configuração de Reuso de Containers

Para acelerar a suíte de testes de integração, habilitamos o reuso de containers do Testcontainers no ambiente local de desenvolvimento.

### Passo 1 — Arquivo de configuração
Já incluímos no repositório o arquivo `.testcontainers.properties` na raiz do projeto com:

```
testcontainers.reuse.enable=true
```

Observações:
- Esta configuração é voltada para ambiente local. Em CI/CD, avalie políticas de isolamento antes de ativar reuso.
- O BaseIT já utiliza `.withReuse(true)` no container MySQL.

### Passo 2 — Pré‑requisitos locais
- Docker em execução e com suporte a volume de reuso (Ryuk habilitado por padrão).
- Java 21 e Maven instalados.

### Passo 3 — Executar a suíte de testes
```
mvn clean verify
```

Se o reuso estiver habilitado, execuções subsequentes serão mais rápidas, pois o container MySQL será reaproveitado entre runs.

### CI/CD — Diretrizes
- Caso o pipeline utilize runners dedicados (máquinas persistentes), é possível manter o reuso. Certifique-se de:
  - Permitir que o processo do runner retenha o diretório do workspace entre jobs.
  - Liberar portas/volumes ao rotacionar runners.
- Em runners efêmeros (ambiente limpo por job), o reuso trará pouco benefício. Recomenda-se manter o padrão (sem reuso) ou configurar cache de camadas Docker.

### Monitoramento de Performance pós‑correção
- Acompanhe os tempos de `mvn test` nas execuções locais antes/depois da configuração.
- No pipeline, compare o tempo das etapas de testes entre os últimos 5 builds.
- Caso observe instabilidade, desative temporariamente o reuso no CI definindo `testcontainers.reuse.enable=false` via arquivo ou variável de ambiente.

---

## Como rodar localmente
1. Configure variáveis no `application.properties` caso necessário.
2. Suba o backend:
   ```
   mvn spring-boot:run
   ```
3. Testes:
   ```
   mvn clean verify
   ```
