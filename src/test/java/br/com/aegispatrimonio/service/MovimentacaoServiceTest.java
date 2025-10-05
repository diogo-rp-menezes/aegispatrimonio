package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.MovimentacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.MovimentacaoResponseDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import br.com.aegispatrimonio.repository.MovimentacaoRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

    @InjectMocks
    private MovimentacaoService movimentacaoService;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;
    @Mock
    private AtivoRepository ativoRepository;
    @Mock
    private LocalizacaoRepository localizacaoRepository;
    @Mock
    private PessoaRepository pessoaRepository;

    private Movimentacao movimentacao;
    private Ativo ativo;
    private Localizacao localizacaoOrigem;
    private Localizacao localizacaoDestino;
    private Pessoa pessoaOrigem;
    private Pessoa pessoaDestino;

    @BeforeEach
    void setUp() {
        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setNome("Notebook");
        ativo.setNumeroPatrimonio("NT-001");

        localizacaoOrigem = new Localizacao();
        localizacaoOrigem.setId(1L);
        localizacaoOrigem.setNome("Sala 101");

        localizacaoDestino = new Localizacao();
        localizacaoDestino.setId(2L);
        localizacaoDestino.setNome("Sala 202");

        pessoaOrigem = new Pessoa();
        pessoaOrigem.setId(1L);
        pessoaOrigem.setNome("João");

        pessoaDestino = new Pessoa();
        pessoaDestino.setId(2L);
        pessoaDestino.setNome("Maria");

        movimentacao = new Movimentacao();
        movimentacao.setId(1L);
        movimentacao.setAtivo(ativo);
        movimentacao.setLocalizacaoOrigem(localizacaoOrigem);
        movimentacao.setLocalizacaoDestino(localizacaoDestino);
        movimentacao.setPessoaOrigem(pessoaOrigem);
        movimentacao.setPessoaDestino(pessoaDestino);
        movimentacao.setStatus(StatusMovimentacao.PENDENTE);
    }

    @Test
    @DisplayName("Deve criar uma nova movimentação com sucesso quando os dados são válidos")
    void criar_quandoDadosValidos_deveRetornarMovimentacaoResponseDTO() {
        MovimentacaoRequestDTO request = new MovimentacaoRequestDTO();
        request.setAtivoId(1L);
        request.setLocalizacaoOrigemId(1L);
        request.setLocalizacaoDestinoId(2L);
        request.setPessoaOrigemId(1L);
        request.setPessoaDestinoId(2L);

        when(ativoRepository.existsById(1L)).thenReturn(true);
        when(movimentacaoRepository.existsByAtivoIdAndStatus(1L, StatusMovimentacao.PENDENTE)).thenReturn(false);
        when(localizacaoRepository.existsById(anyLong())).thenReturn(true);
        when(pessoaRepository.existsById(anyLong())).thenReturn(true);

        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));
        when(localizacaoRepository.findById(1L)).thenReturn(Optional.of(localizacaoOrigem));
        when(localizacaoRepository.findById(2L)).thenReturn(Optional.of(localizacaoDestino));
        when(pessoaRepository.findById(1L)).thenReturn(Optional.of(pessoaOrigem));
        when(pessoaRepository.findById(2L)).thenReturn(Optional.of(pessoaDestino));

        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(invocation -> {
            Movimentacao movSalva = invocation.getArgument(0);
            movSalva.setId(2L);
            return movSalva;
        });

        MovimentacaoResponseDTO response = movimentacaoService.criar(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        verify(movimentacaoRepository, times(1)).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar movimentação para ativo com movimentação já pendente")
    void criar_quandoAtivoJaPossuiMovimentacaoPendente_deveLancarExcecao() {
        MovimentacaoRequestDTO request = new MovimentacaoRequestDTO();
        request.setAtivoId(1L);

        when(ativoRepository.existsById(1L)).thenReturn(true);
        when(movimentacaoRepository.existsByAtivoIdAndStatus(1L, StatusMovimentacao.PENDENTE)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> movimentacaoService.criar(request));

        assertTrue(exception.getMessage().contains("Já existe uma movimentação pendente para este ativo"));
        verify(movimentacaoRepository, never()).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("Deve efetivar uma movimentação com sucesso quando o status é PENDENTE")
    void efetivarMovimentacao_quandoStatusPendente_deveMudarStatusParaEfetivadaEAtualizarAtivo() {
        Long movimentacaoId = 1L;
        movimentacao.setStatus(StatusMovimentacao.PENDENTE);

        when(movimentacaoRepository.findById(movimentacaoId)).thenReturn(Optional.of(movimentacao));
        when(ativoRepository.save(any(Ativo.class))).thenReturn(ativo);
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovimentacaoResponseDTO response = movimentacaoService.efetivarMovimentacao(movimentacaoId);

        assertNotNull(response);
        assertEquals(StatusMovimentacao.EFETIVADA, response.getStatus());

        ArgumentCaptor<Ativo> ativoCaptor = ArgumentCaptor.forClass(Ativo.class);
        verify(ativoRepository).save(ativoCaptor.capture());
        assertEquals(localizacaoDestino, ativoCaptor.getValue().getLocalizacao());
        assertEquals(pessoaDestino, ativoCaptor.getValue().getPessoaResponsavel());

        ArgumentCaptor<Movimentacao> movimentacaoCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository).save(movimentacaoCaptor.capture());
        assertEquals(StatusMovimentacao.EFETIVADA, movimentacaoCaptor.getValue().getStatus());
        assertNotNull(movimentacaoCaptor.getValue().getDataEfetivacao());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar efetivar movimentação com status incorreto")
    void efetivarMovimentacao_quandoStatusIncorreto_deveLancarExcecao() {
        Long movimentacaoId = 1L;
        movimentacao.setStatus(StatusMovimentacao.CANCELADA);

        when(movimentacaoRepository.findById(movimentacaoId)).thenReturn(Optional.of(movimentacao));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> movimentacaoService.efetivarMovimentacao(movimentacaoId));

        assertTrue(exception.getMessage().contains("Somente movimentações pendentes podem ser efetivadas"));
        verify(movimentacaoRepository, never()).save(any(Movimentacao.class));
        verify(ativoRepository, never()).save(any(Ativo.class));
    }

    @Test
    @DisplayName("Deve cancelar uma movimentação com sucesso quando o status é PENDENTE")
    void cancelarMovimentacao_quandoStatusPendente_deveMudarStatusParaCancelada() {
        Long movimentacaoId = 1L;
        String motivo = "Cancelamento de teste";
        movimentacao.setStatus(StatusMovimentacao.PENDENTE);

        when(movimentacaoRepository.findById(movimentacaoId)).thenReturn(Optional.of(movimentacao));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovimentacaoResponseDTO response = movimentacaoService.cancelarMovimentacao(movimentacaoId, motivo);

        assertNotNull(response);
        assertEquals(StatusMovimentacao.CANCELADA, response.getStatus());

        verify(ativoRepository, never()).save(any(Ativo.class));

        ArgumentCaptor<Movimentacao> movimentacaoCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository).save(movimentacaoCaptor.capture());
        assertEquals(StatusMovimentacao.CANCELADA, movimentacaoCaptor.getValue().getStatus());
        assertEquals(motivo, movimentacaoCaptor.getValue().getObservacoes());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cancelar movimentação com status incorreto")
    void cancelarMovimentacao_quandoStatusIncorreto_deveLancarExcecao() {
        Long movimentacaoId = 1L;
        movimentacao.setStatus(StatusMovimentacao.EFETIVADA);

        when(movimentacaoRepository.findById(movimentacaoId)).thenReturn(Optional.of(movimentacao));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> movimentacaoService.cancelarMovimentacao(movimentacaoId, "motivo"));

        assertTrue(exception.getMessage().contains("Somente movimentações pendentes podem ser canceladas"));
        verify(movimentacaoRepository, never()).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("Deve buscar uma movimentação por ID e retornar DTO quando encontrada")
    void buscarPorId_quandoMovimentacaoExiste_deveRetornarOptionalDeMovimentacaoResponseDTO() {
        when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(movimentacao));

        Optional<MovimentacaoResponseDTO> resultado = movimentacaoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        MovimentacaoResponseDTO dto = resultado.get();
        assertEquals(movimentacao.getId(), dto.getId());
        assertEquals(movimentacao.getAtivo().getNome(), dto.getAtivoNome());

        verify(movimentacaoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve buscar uma movimentação por ID e retornar vazio quando não encontrada")
    void buscarPorId_quandoMovimentacaoNaoExiste_deveRetornarOptionalVazio() {
        when(movimentacaoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<MovimentacaoResponseDTO> resultado = movimentacaoService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
        verify(movimentacaoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve 'deletar' uma movimentação (soft delete) com sucesso quando ela existe")
    void deletar_quandoMovimentacaoExiste_deveInvocarSoftDeleteDoRepositorio() {
        Long movimentacaoId = 1L;
        when(movimentacaoRepository.findById(movimentacaoId)).thenReturn(Optional.of(movimentacao));

        assertDoesNotThrow(() -> movimentacaoService.deletar(movimentacaoId));

        verify(movimentacaoRepository, times(1)).delete(movimentacao);
    }
}
