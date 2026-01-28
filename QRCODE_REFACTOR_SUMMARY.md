# ⚡ Refatoração: Consolidação de Serviço de QR Code

## 💡 O quê
Unificação da lógica de geração de QR Codes em um único serviço (`QRCodeService`), eliminando a duplicidade (`QrCodeService`) e padronizando o uso em todo o sistema.

## 🎯 Porquê
Identificou-se a existência de dois serviços com propósitos idênticos (`QRCodeService` e `QrCodeService`), causando confusão e manutenção duplicada. Esta ação remove dívida técnica e segue o princípio DRY (Don't Repeat Yourself).

## 📊 Melhoria Mensurada
- **Redução de Código:** Eliminação de 1 classe de serviço e 1 classe de teste redundantes.
- **Padronização:** Todos os pontos de consumo (Controller e Relatórios) agora utilizam a implementação canônica baseada em `MultiFormatWriter` (ZXing).
- **Cobertura de Testes:** Mantida a cobertura com testes unitários validados para o serviço remanescente.
