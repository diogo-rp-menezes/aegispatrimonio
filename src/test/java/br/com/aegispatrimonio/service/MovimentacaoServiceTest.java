package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.MovimentacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.MovimentacaoResponseDTO;
import br.com.aegispatrimonio.exception.ResourceConflictException;
import br.com.aegispatrimonio.exception.ResourceNotFoundException;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import br.com.aegispatrimonio.repository.MovimentacaoRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

    @Mock
    private MovimentacaoRepository movimentacaoRepository;
    @Mock
    private AtivoRepository ativoRepository;
    @Mock
    private LocalizacaoRepository localizacaoRepository;
    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private MovimentacaoService movimentacaoService;

    private Ativo ativo;
    private Localizacao localizacaoOrigem, localizacaoDestino;
    private Pessoa pessoaOrigem, pessoaDestino;
    private Filial filial;
    private MovimentacaoRequestDTO requestDTO;
    private Movimentacao movimentacao;
    private final Pageable pageable = Pageable.unpaged();

    @BeforeEach
    void setUp() {
        filial = new Filial();
        filial.setId(1L);
        filial.setNome("Filial A");

        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setStatus(StatusAtivo.ATIVO);
        ativo.setFilial(filial);

        localizacaoOrigem = new Localizacao();
        localizacaoOrigem.setId(1L);
        localizacaoOrigem.setNome("Origem");
        localizacaoOrigem.setFilial(filial);

        localizacaoDestino = new Localizacao();
        localizacaoDestino.setId(2L);
        localizacaoDestino.setNome("Destino");
        localizacaoDestino.setFilial(filial);

        pessoaOrigem = new Pessoa();
        pessoaOrigem.setId(1L);
        pessoaOrigem.setNome("Pessoa Origem");
        pessoaOrigem.setFilial(filial);

        pessoaDestino = new Pessoa();
        pessoaDestino.setId(2L);
        pessoaDestino.setNome("Pessoa Destino");
        pessoaDestino.setFilial(filial);

        ativo.setLocalizacao(localizacaoOrigem);
        ativo.setPessoaResponsavel(pessoaOrigem);

        requestDTO = new MovimentacaoRequestDTO(1L, 1L, 2L, 1L, 2L, LocalDate.now(), "Motivo Teste", null);

        movimentacao = new Movimentacao();
        movimentacao.setId(1L);
        movimentacao.setAtivo(ativo);
        movimentacao.setLocalizacaoOrigem(localizacaoOrigem);
        movimentacao.setPessoaOrigem(pessoaOrigem);
        movimentacao.setLocalizacaoDestino(localizacaoDestino);
        movimentacao.setPessoaDestino(pessoaDestino);
        movimentacao.setStatus(StatusMovimentacao.PENDENTE);
    }

    @Test
    @DisplayName("Deve criar uma movimentação com dados válidos")
    void criar_quandoValido_deveCriarMovimentacao() {
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));
        when(localizacaoRepository.findById(2L)).thenReturn(Optional.of(localizacaoDestino));
        when(pessoaRepository.findById(2L)).thenReturn(Optional.of(pessoaDestino));
        when(movimentacaoRepository.existsByAtivoIdAndStatus(1L, StatusMovimentacao.PENDENTE)).thenReturn(false);
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovimentacaoResponseDTO response = movimentacaoService.criar(requestDTO);

        assertNotNull(response);
        assertEquals(requestDTO.getAtivoId(), response.getAtivoId());
        verify(movimentacaoRepository, times(1)).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar movimentação se já existir uma pendente")
    void criar_quandoMovimentacaoPendenteExiste_deveLancarExcecao() {
        when(movimentacaoRepository.existsByAtivoIdAndStatus(1L, StatusMovimentacao.PENDENTE)).thenReturn(true);
        assertThrows(ResourceConflictException.class, () -> movimentacaoService.criar(requestDTO));
    }

    @Test
    @DisplayName("Deve efetivar uma movimentação pendente")
    void efetivarMovimentacao_quandoPendente_deveEfetivar() {
        when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(movimentacao));
        when(ativoRepository.save(any(Ativo.class))).thenReturn(ativo);
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenReturn(movimentacao);

        MovimentacaoResponseDTO response = movimentacaoService.efetivarMovimentacao(1L);

        assertEquals(StatusMovimentacao.EFETIVADA, response.getStatus());
        assertEquals(localizacaoDestino, ativo.getLocalizacao());
        assertEquals(pessoaDestino, ativo.getPessoaResponsavel());
        verify(ativoRepository, times(1)).save(ativo);
    }

    @Test
    @DisplayName("Deve lançar exceção ao efetivar movimentação que não está pendente")
    void efetivarMovimentacao_quandoNaoPendente_deveLancarExcecao() {
        movimentacao.setStatus(StatusMovimentacao.EFETIVADA);
        when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(movimentacao));

        assertThrows(ResourceConflictException.class, () -> movimentacaoService.efetivarMovimentacao(1L));
    }

    @Test
    @DisplayName("Deve cancelar uma movimentação pendente")
    void cancelarMovimentacao_quandoPendente_deveCancelar() {
        String motivo = "Cancelamento por teste";
        when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(movimentacao));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenReturn(movimentacao);

        MovimentacaoResponseDTO response = movimentacaoService.cancelarMovimentacao(1L, motivo);

        assertEquals(StatusMovimentacao.CANCELADA, response.getStatus());
        assertEquals(motivo, response.getObservacoes());
    }

    @Test
    @DisplayName("Deve deletar uma movimentação pendente")
    void deletar_quandoPendente_deveDeletar() {
        when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(movimentacao));
        doNothing().when(movimentacaoRepository).delete(movimentacao);

        movimentacaoService.deletar(1L);

        verify(movimentacaoRepository, times(1)).delete(movimentacao);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar movimentação que não está pendente")
    void deletar_quandoNaoPendente_deveLancarExcecao() {
        movimentacao.setStatus(StatusMovimentacao.EFETIVADA);
        when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(movimentacao));

        assertThrows(ResourceConflictException.class, () -> movimentacaoService.deletar(1L));
    }

    @Test
    @DisplayName("Deve buscar uma movimentação por ID")
    void buscarPorId_deveRetornarMovimentacao() {
        when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(movimentacao));
        Optional<MovimentacaoResponseDTO> response = movimentacaoService.buscarPorId(1L);
        assertTrue(response.isPresent());
        assertEquals(movimentacao.getId(), response.get().getId());
    }

    @Test
    @DisplayName("Deve listar todas as movimentações")
    void findAll_deveRetornarPaginaDeMovimentacoes() {
        Page<Movimentacao> page = new PageImpl<>(Collections.singletonList(movimentacao));
        when(movimentacaoRepository.findAll(pageable)).thenReturn(page);

        Page<MovimentacaoResponseDTO> result = movimentacaoService.findAll(pageable);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(movimentacaoRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve listar movimentações por ID do Ativo")
    void findByAtivoId_deveRetornarPaginaDeMovimentacoes() {
        Page<Movimentacao> page = new PageImpl<>(Collections.singletonList(movimentacao));
        when(movimentacaoRepository.findByAtivoId(1L, pageable)).thenReturn(page);

        Page<MovimentacaoResponseDTO> result = movimentacaoService.findByAtivoId(1L, pageable);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(movimentacaoRepository).findByAtivoId(1L, pageable);
    }

    @Test
    @DisplayName("Deve listar movimentações por Status")
    void findByStatus_deveRetornarPaginaDeMovimentacoes() {
        Page<Movimentacao> page = new PageImpl<>(Collections.singletonList(movimentacao));
        when(movimentacaoRepository.findByStatus(StatusMovimentacao.PENDENTE, pageable)).thenReturn(page);

        Page<MovimentacaoResponseDTO> result = movimentacaoService.findByStatus(StatusMovimentacao.PENDENTE, pageable);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(movimentacaoRepository).findByStatus(StatusMovimentacao.PENDENTE, pageable);
    }

    @Test
    @DisplayName("Deve listar movimentações por Período")
    void findByPeriodo_deveRetornarPaginaDeMovimentacoes() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(1);
        Page<Movimentacao> page = new PageImpl<>(Collections.singletonList(movimentacao));
        when(movimentacaoRepository.findByDataMovimentacaoBetween(startDate, endDate, pageable)).thenReturn(page);

        Page<MovimentacaoResponseDTO> result = movimentacaoService.findByPeriodo(startDate, endDate, pageable);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(movimentacaoRepository).findByDataMovimentacaoBetween(startDate, endDate, pageable);
    }
}
