package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.MovimentacaoRequestDTO;
import br.com.aegispatrimonio.exception.ResourceConflictException;
import br.com.aegispatrimonio.exception.ResourceNotFoundException;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import br.com.aegispatrimonio.repository.MovimentacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

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
    private FuncionarioRepository funcionarioRepository;

    @InjectMocks
    private MovimentacaoService movimentacaoService;

    private Ativo ativo;
    private Funcionario funcionarioOrigem, funcionarioDestino;
    private Localizacao localizacaoOrigem, localizacaoDestino;
    private Movimentacao movimentacao;
    private Filial filial;

    @BeforeEach
    void setUp() {
        filial = new Filial();
        filial.setId(1L);

        localizacaoOrigem = new Localizacao();
        localizacaoOrigem.setId(1L);
        localizacaoOrigem.setFilial(filial);

        localizacaoDestino = new Localizacao();
        localizacaoDestino.setId(2L);
        localizacaoDestino.setFilial(filial);

        funcionarioOrigem = new Funcionario();
        funcionarioOrigem.setId(1L);
        funcionarioOrigem.setFiliais(Set.of(filial));

        funcionarioDestino = new Funcionario();
        funcionarioDestino.setId(2L);
        funcionarioDestino.setFiliais(Set.of(filial));

        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setStatus(StatusAtivo.ATIVO);
        ativo.setFilial(filial);
        ativo.setLocalizacao(localizacaoOrigem);
        ativo.setFuncionarioResponsavel(funcionarioOrigem);

        movimentacao = new Movimentacao();
        movimentacao.setId(1L);
        movimentacao.setAtivo(ativo);
        movimentacao.setFuncionarioOrigem(funcionarioOrigem);
        movimentacao.setFuncionarioDestino(funcionarioDestino);
        movimentacao.setLocalizacaoOrigem(localizacaoOrigem);
        movimentacao.setLocalizacaoDestino(localizacaoDestino);
        movimentacao.setStatus(StatusMovimentacao.PENDENTE);
    }

    @Test
    @DisplayName("Criar: Deve criar movimentação com sucesso")
    void criar_quandoValido_deveSalvar() {
        MovimentacaoRequestDTO request = new MovimentacaoRequestDTO(1L, 1L, 2L, 1L, 2L, LocalDate.now(), "Motivo", null);
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));
        when(localizacaoRepository.findById(2L)).thenReturn(Optional.of(localizacaoDestino));
        when(funcionarioRepository.findById(2L)).thenReturn(Optional.of(funcionarioDestino));
        when(movimentacaoRepository.existsByAtivoIdAndStatus(1L, StatusMovimentacao.PENDENTE)).thenReturn(false);
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenReturn(movimentacao);

        assertDoesNotThrow(() -> movimentacaoService.criar(request));
        verify(movimentacaoRepository).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção se já existe movimentação pendente")
    void criar_quandoExistePendente_deveLancarExcecao() {
        MovimentacaoRequestDTO request = new MovimentacaoRequestDTO(1L, 1L, 2L, 1L, 2L, LocalDate.now(), "Motivo", null);
        when(movimentacaoRepository.existsByAtivoIdAndStatus(1L, StatusMovimentacao.PENDENTE)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> movimentacaoService.criar(request));
    }

    @Test
    @DisplayName("Efetivar: Deve efetivar movimentação pendente")
    void efetivarMovimentacao_quandoPendente_deveAtualizarAtivoEStatus() {
        when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(movimentacao));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenReturn(movimentacao);

        movimentacaoService.efetivarMovimentacao(1L);

        assertEquals(StatusMovimentacao.EFETIVADA, movimentacao.getStatus());
        assertEquals(localizacaoDestino, ativo.getLocalizacao());
        assertEquals(funcionarioDestino, ativo.getFuncionarioResponsavel());
        verify(ativoRepository).save(ativo);
    }

    @Test
    @DisplayName("Efetivar: Deve lançar exceção se não estiver pendente")
    void efetivarMovimentacao_quandoNaoPendente_deveLancarExcecao() {
        movimentacao.setStatus(StatusMovimentacao.EFETIVADA);
        when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(movimentacao));

        assertThrows(ResourceConflictException.class, () -> movimentacaoService.efetivarMovimentacao(1L));
    }

    @Test
    @DisplayName("Cancelar: Deve cancelar movimentação pendente")
    void cancelarMovimentacao_quandoPendente_deveMudarStatus() {
        when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(movimentacao));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenReturn(movimentacao);

        movimentacaoService.cancelarMovimentacao(1L, "Motivo teste");

        assertEquals(StatusMovimentacao.CANCELADA, movimentacao.getStatus());
        assertEquals("Motivo teste", movimentacao.getObservacoes());
    }

    @Test
    @DisplayName("Deletar: Deve deletar movimentação pendente")
    void deletar_quandoPendente_deveChamarDelete() {
        when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(movimentacao));

        movimentacaoService.deletar(1L);

        verify(movimentacaoRepository).delete(movimentacao);
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se não estiver pendente")
    void deletar_quandoNaoPendente_deveLancarExcecao() {
        movimentacao.setStatus(StatusMovimentacao.EFETIVADA);
        when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(movimentacao));

        assertThrows(ResourceConflictException.class, () -> movimentacaoService.deletar(1L));
    }
}
