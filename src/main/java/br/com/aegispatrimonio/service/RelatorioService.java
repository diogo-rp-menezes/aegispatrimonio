package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.repository.AtivoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class RelatorioService {

    private final AtivoRepository ativoRepository;
    private final QrCodeService qrCodeService;
    private final ObjectMapper objectMapper;

    public RelatorioService(AtivoRepository ativoRepository, QrCodeService qrCodeService, ObjectMapper objectMapper) {
        this.ativoRepository = ativoRepository;
        this.qrCodeService = qrCodeService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public byte[] gerarQrCode(Long ativoId) {
        Ativo ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + ativoId));

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("id", ativo.getId());
            data.put("patrimonio", ativo.getNumeroPatrimonio() != null ? ativo.getNumeroPatrimonio() : "");
            data.put("nome", ativo.getNome());

            String content = objectMapper.writeValueAsString(data);
            return qrCodeService.generateQrCode(content, 300, 300);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar conteúdo do QR Code", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] gerarTermoResponsabilidade(Long ativoId) {
        Ativo ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + ativoId));

        Funcionario responsavel = ativo.getFuncionarioResponsavel();
        if (responsavel == null) {
            throw new IllegalStateException("Este ativo não possui um funcionário responsável atribuído.");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            // Título
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titulo = new Paragraph("Termo de Responsabilidade", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            document.add(titulo);

            // Texto Legal
            Font fontCorpo = FontFactory.getFont(FontFactory.HELVETICA, 12);
            String texto = String.format(
                    "Pelo presente termo, eu, %s, declaro ter recebido da empresa o equipamento abaixo descrito, " +
                            "em perfeito estado de conservação e funcionamento, comprometendo-me a zelar pelo mesmo " +
                            "e utilizá-lo exclusivamente para fins profissionais.",
                    responsavel.getNome()
            );
            Paragraph corpo = new Paragraph(texto, fontCorpo);
            corpo.setAlignment(Element.ALIGN_JUSTIFIED);
            corpo.setSpacingAfter(20);
            document.add(corpo);

            // Detalhes do Ativo
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            document.add(new Paragraph("Detalhes do Equipamento:", fontLabel));
            document.add(new Paragraph("Nome: " + ativo.getNome()));
            document.add(new Paragraph("Número de Patrimônio: " + ativo.getNumeroPatrimonio()));

            if (ativo.getDetalheHardware() != null) {
                 if (ativo.getDetalheHardware().getComputerName() != null) {
                     document.add(new Paragraph("Hostname: " + ativo.getDetalheHardware().getComputerName()));
                 }
            }

            document.add(new Paragraph("Tipo: " + ativo.getTipoAtivo().getNome()));
            document.add(new Paragraph("Valor de Aquisição: R$ " + ativo.getValorAquisicao()));
            document.add(new Paragraph("Data de Entrega: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

            // Espaço para Assinatura
            document.add(new Paragraph("\n\n\n\n"));
            Paragraph linhaAssinatura = new Paragraph("___________________________________________________");
            linhaAssinatura.setAlignment(Element.ALIGN_CENTER);
            document.add(linhaAssinatura);

            Paragraph nomeAssinatura = new Paragraph(responsavel.getNome());
            nomeAssinatura.setAlignment(Element.ALIGN_CENTER);
            document.add(nomeAssinatura);

            Paragraph localData = new Paragraph("\nLocal e Data: ____________________, ____/____/_______");
            localData.setAlignment(Element.ALIGN_CENTER);
            document.add(localData);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do termo de responsabilidade", e);
        }
    }
}
