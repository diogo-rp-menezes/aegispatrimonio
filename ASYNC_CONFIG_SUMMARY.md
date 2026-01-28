# ⚡ Configuração de Thread Pool Assíncrono

**💡 O quê:**
Foi implementada a classe `AsyncConfig` (`br.com.aegispatrimonio.config.AsyncConfig`) definindo um bean `taskExecutor` do tipo `ThreadPoolTaskExecutor`.

**🎯 Porquê:**
O sistema utiliza `@Async` no `SecurityAuditService` para registrar logs de auditoria sem bloquear a thread principal. Sem uma configuração explícita, o Spring utiliza `SimpleAsyncTaskExecutor`, que cria uma **nova thread para cada requisição**. Em cenários de alta carga ou ataques (ex: brute force, que gera muitos logs de auditoria), isso causaria exaustão de threads (OutOfMemoryError ou thread starvation).

**📊 Melhoria Mensurada:**
- **Robustez:** Limite de concorrência estabelecido (Máximo 10 threads para tarefas de fundo).
- **Proteção:** Fila limitada (500 itens) previne estouro de memória; tarefas excedentes são descartadas (com log de erro) em vez de derrubar a aplicação.
- **Observabilidade:** Threads nomeadas com prefixo `AegisAsync-` facilitam debug e profiling.
