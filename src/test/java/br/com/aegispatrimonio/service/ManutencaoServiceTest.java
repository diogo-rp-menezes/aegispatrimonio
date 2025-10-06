package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.ManutencaoCancelDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoConclusaoDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoInicioDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoRequestDTO;
import br.com.aegispatrimonio.dto.response.ManutencaoResponseDTO;
import br.com.aegispatrimonio.exception.ResourceConflictException;
import br.com.aegispatrimonio.exception.ResourceNotFoundException;
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

        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));
        when(pessoaRepository.findById(1L)).thenReturn(Optional.of(solicitante));
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));

        when(manutencaoRepository.save(any(Manutencao.class))).thenAnswer(invocation -> {
            Manutencao manutencaoSalva = invocation.getArgument(0);
            manutencaoSalva.setId(2L); // Simula a geração de ID pelo banco
            return manutencaoSalva;
        });

        ManutencaoResponseDTO response = manutencaoService.criar(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        verify(manutencaoRepository, times(1)).save(any(Manutencao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar manutenção para um Ativo inexistente")
    void criar_quandoAtivoNaoExiste_deveLancarResourceNotFoundException() {
        ManutencaoRequestDTO request = new ManutencaoRequestDTO();
        request.setAtivoId(99L);

        when(ativoRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            manutencaoService.criar(request);
        });

        assertTrue(exception.getMessage().contains("Ativo não encontrado"));
        verify(manutencaoRepository, never()).save(any(Manutencao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar manutenção com Solicitante inexistente")
    void criar_quandoSolicitanteNaoExiste_deveLancarResourceNotFoundException() {
        ManutencaoRequestDTO request = new ManutencaoRequestDTO();
        request.setAtivoId(1L);
        request.setSolicitanteId(99L);

        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));
        when(pessoaRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
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
    void aprovar_quandoStatusIncorreto_deveLancarResourceConflictException() {
        Long manutencaoId = 1L;
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));

        ResourceConflictException exception = assertThrows(ResourceConflictException.class, () -> {
            manutencaoService.aprovar(manutencaoId);
        });

        assertTrue(exception.getMessage().contains("A manutenção não pode ser aprovada"));
        verify(manutencaoRepository, never()).save(any(Manutencao.class));
    }

    @Test
    @DisplayName("Deve iniciar uma manutenção com sucesso quando o status é APROVADA")
    void iniciar_quandoStatusAprovada_deveMudarStatusParaEmAndamento() {
        Long manutencaoId = 1L;
        Long tecnicoId = 2L;
        manutencao.setStatus(StatusManutencao.APROVADA);

        ManutencaoInicioDTO inicioDTO = new ManutencaoInicioDTO();
        inicioDTO.setTecnicoId(tecnicoId);

        Pessoa tecnico = new Pessoa();
        tecnico.setId(tecnicoId);

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));
        when(pessoaRepository.findById(tecnicoId)).thenReturn(Optional.of(tecnico));
        when(ativoRepository.save(any(Ativo.class))).thenReturn(ativo);
        when(manutencaoRepository.save(any(Manutencao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ManutencaoResponseDTO response = manutencaoService.iniciar(manutencaoId, inicioDTO);

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
    void iniciar_quandoStatusIncorreto_deveLancarResourceConflictException() {
        Long manutencaoId = 1L;
        manutencao.setStatus(StatusManutencao.SOLICITADA);
        ManutencaoInicioDTO inicioDTO = new ManutencaoInicioDTO();
        inicioDTO.setTecnicoId(2L);

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));

        ResourceConflictException exception = assertThrows(ResourceConflictException.class, () -> {
            manutencaoService.iniciar(manutencaoId, inicioDTO);
        });

        assertTrue(exception.getMessage().contains("A manutenção não pode ser iniciada"));
        verify(manutencaoRepository, never()).save(any(Manutencao.class));
        verify(ativoRepository, never()).save(any(Ativo.class));
    }

    @Test
    @DisplayName("Deve concluir uma manutenção com sucesso quando o status é EM_ANDAMENTO")
    void concluir_quandoStatusEmAndamento_deveMudarStatusParaConcluida() {
        Long manutencaoId = 1L;
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        
        ManutencaoConclusaoDTO conclusaoDTO = new ManutencaoConclusaoDTO();
        conclusaoDTO.setDescricaoServico("Lâmpada trocada");
        conclusaoDTO.setCustoReal(new BigDecimal("150.00"));
        conclusaoDTO.setTempoExecucao(60);

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));
        when(ativoRepository.save(any(Ativo.class))).thenReturn(ativo);
        when(manutencaoRepository.save(any(Manutencao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ManutencaoResponseDTO response = manutencaoService.concluir(manutencaoId, conclusaoDTO);

        assertNotNull(response);
        assertEquals(StatusManutencao.CONCLUIDA, response.getStatus());

        ArgumentCaptor<Ativo> ativoCaptor = ArgumentCaptor.forClass(Ativo.class);
        verify(ativoRepository).save(ativoCaptor.capture());
        assertEquals(StatusAtivo.ATIVO, ativoCaptor.getValue().getStatus());

        ArgumentCaptor<Manutencao> manutencaoCaptor = ArgumentCaptor.forClass(Manutencao.class);
        verify(manutencaoRepository).save(manutencaoCaptor.capture());
        assertEquals(StatusManutencao.CONCLUIDA, manutencaoCaptor.getValue().getStatus());
        assertEquals(conclusaoDTO.getDescricaoServico(), manutencaoCaptor.getValue().getDescricaoServico());
        assertEquals(conclusaoDTO.getCustoReal(), manutencaoCaptor.getValue().getCustoReal());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar concluir manutenção com status incorreto")
    void concluir_quandoStatusIncorreto_deveLancarResourceConflictException() {
        Long manutencaoId = 1L;
        manutencao.setStatus(StatusManutencao.APROVADA);
        ManutencaoConclusaoDTO conclusaoDTO = new ManutencaoConclusaoDTO();
        conclusaoDTO.setDescricaoServico("serviço");
        conclusaoDTO.setCustoReal(BigDecimal.ONE);
        conclusaoDTO.setTempoExecucao(10);

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));

        ResourceConflictException exception = assertThrows(ResourceConflictException.class, () -> {
            manutencaoService.concluir(manutencaoId, conclusaoDTO);
        });

        assertTrue(exception.getMessage().contains("A manutenção não pode ser concluída"));
        verify(manutencaoRepository, never()).save(any(Manutencao.class));
        verify(ativoRepository, never()).save(any(Ativo.class));
    }

    @Test
    @DisplayName("Deve cancelar uma manutenção com sucesso quando o status é SOLICITADA")
    void cancelar_quandoStatusSolicitada_deveMudarStatusParaCancelada() {
        Long manutencaoId = 1L;
        manutencao.setStatus(StatusManutencao.SOLICITADA);
        ManutencaoCancelDTO cancelDTO = new ManutencaoCancelDTO();
        cancelDTO.setMotivo("Teste de cancelamento");

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));
        when(manutencaoRepository.save(any(Manutencao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ManutencaoResponseDTO response = manutencaoService.cancelar(manutencaoId, cancelDTO);

        assertNotNull(response);
        assertEquals(StatusManutencao.CANCELADA, response.getStatus());

        verify(ativoRepository, never()).save(any(Ativo.class));

        ArgumentCaptor<Manutencao> manutencaoCaptor = ArgumentCaptor.forClass(Manutencao.class);
        verify(manutencaoRepository).save(manutencaoCaptor.capture());
        assertEquals(StatusManutencao.CANCELADA, manutencaoCaptor.getValue().getStatus());
        assertEquals(cancelDTO.getMotivo(), manutencaoCaptor.getValue().getObservacoes());
    }

    @Test
    @DisplayName("Deve cancelar uma manutenção EM_ANDAMENTO e restaurar o status do ativo")
    void cancelar_quandoStatusEmAndamento_deveMudarStatusParaCanceladaERestaurarAtivo() {
        Long manutencaoId = 1L;
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        ativo.setStatus(StatusAtivo.EM_MANUTENCAO);
        ManutencaoCancelDTO cancelDTO = new ManutencaoCancelDTO();
        cancelDTO.setMotivo("Cancelamento durante execução");

        when(manutencaoRepository.findById(manutencaoId)).thenReturn(Optional.of(manutencao));
        when(ativoRepository.save(any(Ativo.class))).thenReturn(ativo);
        when(manutencaoRepository.save(any(Manutencao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ManutencaoResponseDTO response = manutencaoService.cancelar(manutencaoId, cancelDTO);

        assertNotNull(response);
        assertEquals(StatusManutencao.CANCELADA, response.getStatus());

        ArgumentCaptor<Ativo> ativoCaptor = ArgumentCaptor.forClass(Ativo.class);
        verify(ativoRepository).save(ativoCaptor.capture());
        assertEquals(StatusAtivo.ATIVO, ativoCaptor.getValue().getStatus());

        ArgumentCaptor<Manutencao> manutencaoCaptor = ArgumentCaptor.forClass(Manutencao.class);
        verify(manutencaoRepository).save(manutencaoCaptor.capture());
        assertEquals(StatusManutencao.CANCELADA, manutencaoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Deve deletar uma manutenção com sucesso quando ela existe")
    void deletar_quandoManutencaoExiste_deveInvocarDeleteDoRepositorio() {
        Long manutencaoId = 1L;
        when(manutencaoRepository.existsById(manutencaoId)).thenReturn(true);
        doNothing().when(manutencaoRepository).deleteById(manutencaoId);

        assertDoesNotThrow(() -> manutencaoService.deletar(manutencaoId));

        verify(manutencaoRepository, times(1)).existsById(manutencaoId);
        verify(manutencaoRepository, times(1)).deleteById(manutencaoId);
    }
}