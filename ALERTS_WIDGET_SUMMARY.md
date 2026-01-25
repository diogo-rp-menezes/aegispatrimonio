# ⚡ Consolidação e Correção da API de Alertas

## 💡 O quê
Unificação dos controllers `AlertController` e `AlertaController` em um único `AlertaController` (padrão `/api/v1/alertas`), implementação de RBAC na listagem de alertas e correção do frontend para usar os novos endpoints e campos DTO.

## 🎯 Porquê
Havia duplicação de lógica e inconsistência nos endpoints (`/alerts` vs `/alertas`), além de uma falha de segurança onde a listagem de alertas não filtrava por filial (RBAC). O frontend falhava ao exibir o nome do ativo nos alertas devido a uma incompatibilidade entre a estrutura esperada e o DTO retornado.

## 📊 Melhoria Mensurada
- **Segurança:** Correção crítica de vazamento de dados entre filiais na listagem de alertas. O `AlertNotificationService` agora centraliza a lógica de autorização.
- **Manutenibilidade:** Eliminação de código duplicado (`AlertController.java` removido).
- **Correção Visual:** O Dashboard agora exibe corretamente o nome do ativo nos alertas, utilizando o campo `ativoNome` do DTO.
- **Performance:** Uso de `@EntityGraph` no repositório para evitar problemas de N+1 queries na listagem de alertas.
