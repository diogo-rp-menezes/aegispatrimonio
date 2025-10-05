package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.ManutencaoRequestDTO;
import br.com.aegispatrimonio.dto.response.ManutencaoResponseDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FornecedorRepository;
import br.com.aegispatrimonio.repository.ManutencaoRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManutencaoServiceTest {

    @InjectMocks
    private ManutencaoService manutencaoService;

    @Mock
    private ManutencaoRepository manutencaoRepository;
    @Mock
    private AtivoRepository ativoRepository;
    @Mock
    private FornecedorRepository fornecedorRepository;
    @Mock
    private PessoaRepository pessoaRepository;

    private Manutencao manutencao;
    private Ativo ativo;
    private Pessoa solicitante;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setNome("Projetor Epson");
        ativo.setStatus(StatusAtivo.ATIVO);

        solicitante = new Pessoa();
        solicitante.setId(1L);
        solicitante.setNome("Ana");

        fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        fornecedor.setNome("Epson do Brasil");

        manutencao = new Manutencao();
        manutencao.setId(1L);
        manutencao.setAtivo(ativo);
        manutencao.setSolicitante(solicitante);
        manutencao.setStatus(StatusManutencao.SOLICITADA);
    }

    @Test
    @DisplayName("Deve criar uma nova manutenção com sucesso quando os dados são válidos")
    void criar_quandoDadosValidos_deveRetornarManutencaoResponseDTO() {
        ManutencaoRequestDTO request = new ManutencaoRequestDTO();
        request.setAtivoId(1L);
        request.setSolicitanteId(1L);
        request.setFornecedorId(1L);
        request.setTipo(TipoManutencao.PREVENTIVA);

        when(ativoRepository.existsById(1L)).thenReturn(true);
        when(pessoaRepository.existsById(1L)).thenReturn(true);
        when(fornecedorRepository.existsById(1L)).thenReturn(true);

        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));
        when(pessoaRepository.findById(1L)).thenReturn(Optional.of(solicitante));
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));

        when(manutencaoRepository.save(any(Manutencao.class))).thenAnswer(invocation -> {
            Manutencao manutencaoSalva = invocation.getArgument(0);
            manutencaoSalva.setId(2L);
            return manutencaoSalva;
        });

        ManutencaoResponseDTO response = manutencaoService.criar(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        verify(manutencaoRepository, times(1)).save(any(Manutencao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar manutenção para um Ativo inexistente")
    void criar_quandoAtivoNaoExiste_deveLancarExcecao() {
        ManutencaoRequestDTO request = new ManutencaoRequestDTO();
        request.setAtivoId(99L);

        when(ativoRepository.existsById(99L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            manutencaoService.criar(request);
        });

        assertTrue(exception.getMessage().contains("Ativo não encontrado"));
        verify(manutencaoRepository, never()).save(any(Manutencao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar manutenção com Solicitante inexistente")
    void criar_quandoSolicitanteNaoExiste_deveLancarExcecao() {
        ManutencaoRequestDTO request = new ManutencaoRequestDTO();
        request.setAtivoId(1L);
        request.setSolicitanteId(99L);

        when(ativoRepository.existsById(1L)).thenReturn(true);
        when(pessoaRepository.existsById(99L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            manutencaoService.criar(request);
        });

        assertTrue(exception.getMessage().contains("Solicitante não encontrado"));
        verify(manutencaoRepository, never()).save(any(Manutencao.class));
    }

    @Test
    @DisplayName("Deve aprovar uma manutenção com sucesso quando o status é SOLICITADA")
    void aprovar_quandoStatusSolicitada_deveMudarStatusParaAprovada() {
        Long manutencaoId = 1L;
        manutencao.setStatus(StatusManutencao.SOLICITADA);

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));
        when(manutencaoRepository.save(any(Manutencao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ManutencaoResponseDTO response = manutencaoService.aprovar(manutencaoId);

        assertNotNull(response);
        assertEquals(StatusManutencao.APROVADA, response.getStatus());

        ArgumentCaptor<Manutencao> manutencaoCaptor = ArgumentCaptor.forClass(Manutencao.class);
        verify(manutencaoRepository).save(manutencaoCaptor.capture());
        assertEquals(StatusManutencao.APROVADA, manutencaoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar aprovar manutenção com status incorreto")
    void aprovar_quandoStatusIncorreto_deveLancarExcecao() {
        Long manutencaoId = 1L;
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            manutencaoService.aprovar(manutencaoId);
        });

        assertTrue(exception.getMessage().contains("Manutenção não pode ser aprovada"));
        verify(manutencaoRepository, never()).save(any(Manutencao.class));
    }

    @Test
    @DisplayName("Deve iniciar uma manutenção com sucesso quando o status é APROVADA")
    void iniciar_quandoStatusAprovada_deveMudarStatusParaEmAndamento() {
        Long manutencaoId = 1L;
        Long tecnicoId = 2L;
        manutencao.setStatus(StatusManutencao.APROVADA);

        Pessoa tecnico = new Pessoa();
        tecnico.setId(tecnicoId);

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));
        when(pessoaRepository.findById(tecnicoId)).thenReturn(Optional.of(tecnico));
        when(ativoRepository.save(any(Ativo.class))).thenReturn(ativo);
        when(manutencaoRepository.save(any(Manutencao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ManutencaoResponseDTO response = manutencaoService.iniciar(manutencaoId, tecnicoId);

        assertNotNull(response);
        assertEquals(StatusManutencao.EM_ANDAMENTO, response.getStatus());

        ArgumentCaptor<Ativo> ativoCaptor = ArgumentCaptor.forClass(Ativo.class);
        verify(ativoRepository).save(ativoCaptor.capture());
        assertEquals(StatusAtivo.EM_MANUTENCAO, ativoCaptor.getValue().getStatus());

        ArgumentCaptor<Manutencao> manutencaoCaptor = ArgumentCaptor.forClass(Manutencao.class);
        verify(manutencaoRepository).save(manutencaoCaptor.capture());
        assertEquals(tecnico, manutencaoCaptor.getValue().getTecnicoResponsavel());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar iniciar manutenção com status incorreto")
    void iniciar_quandoStatusIncorreto_deveLancarExcecao() {
        Long manutencaoId = 1L;
        Long tecnicoId = 2L;
        manutencao.setStatus(StatusManutencao.SOLICITADA);

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            manutencaoService.iniciar(manutencaoId, tecnicoId);
        });

        assertTrue(exception.getMessage().contains("Manutenção não pode ser iniciada"));
        verify(manutencaoRepository, never()).save(any(Manutencao.class));
        verify(ativoRepository, never()).save(any(Ativo.class));
    }

    @Test
    @DisplayName("Deve concluir uma manutenção com sucesso quando o status é EM_ANDAMENTO")
    void concluir_quandoStatusEmAndamento_deveMudarStatusParaConcluida() {
        Long manutencaoId = 1L;
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        String descricaoServico = "Lâmpada trocada";
        BigDecimal custoReal = new BigDecimal("150.00");
        Integer tempoExecucao = 60;

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));
        when(ativoRepository.save(any(Ativo.class))).thenReturn(ativo);
        when(manutencaoRepository.save(any(Manutencao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ManutencaoResponseDTO response = manutencaoService.concluir(manutencaoId, descricaoServico, custoReal, tempoExecucao);

        assertNotNull(response);
        assertEquals(StatusManutencao.CONCLUIDA, response.getStatus());

        ArgumentCaptor<Ativo> ativoCaptor = ArgumentCaptor.forClass(Ativo.class);
        verify(ativoRepository).save(ativoCaptor.capture());
        assertEquals(StatusAtivo.ATIVO, ativoCaptor.getValue().getStatus());

        ArgumentCaptor<Manutencao> manutencaoCaptor = ArgumentCaptor.forClass(Manutencao.class);
        verify(manutencaoRepository).save(manutencaoCaptor.capture());
        assertEquals(StatusManutencao.CONCLUIDA, manutencaoCaptor.getValue().getStatus());
        assertEquals(descricaoServico, manutencaoCaptor.getValue().getDescricaoServico());
        assertEquals(custoReal, manutencaoCaptor.getValue().getCustoReal());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar concluir manutenção com status incorreto")
    void concluir_quandoStatusIncorreto_deveLancarExcecao() {
        Long manutencaoId = 1L;
        manutencao.setStatus(StatusManutencao.APROVADA);

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            manutencaoService.concluir(manutencaoId, "serviço", BigDecimal.ONE, 10);
        });

        assertTrue(exception.getMessage().contains("Manutenção não pode ser concluída"));
        verify(manutencaoRepository, never()).save(any(Manutencao.class));
        verify(ativoRepository, never()).save(any(Ativo.class));
    }

    @Test
    @DisplayName("Deve cancelar uma manutenção com sucesso quando o status é SOLICITADA")
    void cancelar_quandoStatusSolicitada_deveMudarStatusParaCancelada() {
        Long manutencaoId = 1L;
        manutencao.setStatus(StatusManutencao.SOLICITADA);
        String motivo = "Teste de cancelamento";

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));
        when(manutencaoRepository.save(any(Manutencao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ManutencaoResponseDTO response = manutencaoService.cancelar(manutencaoId, motivo);

        assertNotNull(response);
        assertEquals(StatusManutencao.CANCELADA, response.getStatus());

        verify(ativoRepository, never()).save(any(Ativo.class));

        ArgumentCaptor<Manutencao> manutencaoCaptor = ArgumentCaptor.forClass(Manutencao.class);
        verify(manutencaoRepository).save(manutencaoCaptor.capture());
        assertEquals(StatusManutencao.CANCELADA, manutencaoCaptor.getValue().getStatus());
        assertEquals(motivo, manutencaoCaptor.getValue().getObservacoes());
    }

    @Test
    @DisplayName("Deve cancelar uma manutenção EM_ANDAMENTO e restaurar o status do ativo")
    void cancelar_quandoStatusEmAndamento_deveMudarStatusParaCanceladaERestaurarAtivo() {
        // --- Arrange (Organizar) ---
        Long manutencaoId = 1L;
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        ativo.setStatus(StatusAtivo.EM_MANUTENCAO); // Estado inicial do ativo
        String motivo = "Cancelamento durante execução";

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));
        when(ativoRepository.save(any(Ativo.class))).thenReturn(ativo);
        when(manutencaoRepository.save(any(Manutencao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Act (Agir) ---
        ManutencaoResponseDTO response = manutencaoService.cancelar(manutencaoId, motivo);

        // --- Assert (Verificar) ---
        assertNotNull(response);
        assertEquals(StatusManutencao.CANCELADA, response.getStatus());

        // Verificar se o ativo foi salvo com o status restaurado para ATIVO
        ArgumentCaptor<Ativo> ativoCaptor = ArgumentCaptor.forClass(Ativo.class);
        verify(ativoRepository).save(ativoCaptor.capture());
        assertEquals(StatusAtivo.ATIVO, ativoCaptor.getValue().getStatus());

        // Verificar se a manutenção foi salva com o status CANCELADA
        ArgumentCaptor<Manutencao> manutencaoCaptor = ArgumentCaptor.forClass(Manutencao.class);
        verify(manutencaoRepository).save(manutencaoCaptor.capture());
        assertEquals(StatusManutencao.CANCELADA, manutencaoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Deve 'deletar' uma manutenção (soft delete) com sucesso quando ela existe")
    void deletar_quandoManutencaoExiste_deveInvocarSoftDeleteDoRepositorio() {
        Long manutencaoId = 1L;
        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));

        assertDoesNotThrow(() -> manutencaoService.deletar(manutencaoId));

        verify(manutencaoRepository, times(1)).delete(manutencao);
    }
}
