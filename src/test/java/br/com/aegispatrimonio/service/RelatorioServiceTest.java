package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.AtivoDetalheHardware;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.TipoAtivo;
import br.com.aegispatrimonio.repository.AtivoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private AtivoRepository ativoRepository;

    @Mock
    private QRCodeService qrCodeService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RelatorioService relatorioService;

    private Ativo ativo;
    private Funcionario funcionario;

    @BeforeEach
    void setUp() {
        funcionario = new Funcionario();
        funcionario.setNome("João da Silva");

        TipoAtivo tipoAtivo = new TipoAtivo();
        tipoAtivo.setNome("Notebook");

        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setNome("Notebook Dell");
        ativo.setNumeroPatrimonio("PAT-12345");
        ativo.setValorAquisicao(new BigDecimal("5000.00"));
        ativo.setTipoAtivo(tipoAtivo);
        ativo.setFuncionarioResponsavel(funcionario);

        AtivoDetalheHardware hardware = new AtivoDetalheHardware();
        hardware.setComputerName("NB-JOAO");
        ativo.setDetalheHardware(hardware);
    }

    @Test
    void testGerarTermoResponsabilidade_Sucesso() {
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        byte[] pdf = relatorioService.gerarTermoResponsabilidade(1L);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);

        // Simple check for PDF header
        String content = new String(pdf);
        assertTrue(content.startsWith("%PDF"), "Output should be a PDF");
    }

    @Test
    void testGerarTermoResponsabilidade_SemFuncionario() {
        ativo.setFuncionarioResponsavel(null);
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        assertThrows(IllegalStateException.class, () -> relatorioService.gerarTermoResponsabilidade(1L));
    }
}
