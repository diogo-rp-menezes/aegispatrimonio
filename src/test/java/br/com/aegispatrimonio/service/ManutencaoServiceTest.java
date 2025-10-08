package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.ManutencaoCancelDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoConclusaoDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoInicioDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoRequestDTO;
import br.com.aegispatrimonio.exception.ResourceConflictException;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.ManutencaoRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManutencaoServiceTest {

    @Mock
    private ManutencaoRepository manutencaoRepository;
    @Mock
    private AtivoRepository ativoRepository;
    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private ManutencaoService manutencaoService;

    private Ativo ativo;
    private Pessoa solicitante;
    private Pessoa tecnico;
    private Manutencao manutencao;

    @BeforeEach
    void setUp() {
        Filial filial = new Filial();
        filial.setId(1L);

        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setStatus(StatusAtivo.ATIVO);
        ativo.setFilial(filial);

        solicitante = new Pessoa();
        solicitante.setId(1L);
        solicitante.setFilial(filial);

        tecnico = new Pessoa();
        tecnico.setId(2L);
        tecnico.setFilial(filial);

        manutencao = new Manutencao();
        manutencao.setId(1L);
        manutencao.setAtivo(ativo);
        manutencao.setSolicitante(solicitante);
    }

    @Test
    @DisplayName("Deve criar uma manutenção com sucesso")
    void criar_quandoValido_deveSalvarManutencao() {
        ManutencaoRequestDTO request = new ManutencaoRequestDTO(1L, TipoManutencao.CORRETIVA, 1L, null, null, "Problema", null, null, null, null);
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));
        when(pessoaRepository.findById(1L)).thenReturn(Optional.of(solicitante));
        // CORREÇÃO: Retornar a manutenção com o ativo associado para evitar NullPointerException.
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        manutencaoService.criar(request);

        verify(manutencaoRepository).save(any(Manutencao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar manutenção para ativo não ATIVO")
    void criar_quandoAtivoNaoAtivo_deveLancarExcecao() {
        ativo.setStatus(StatusAtivo.EM_MANUTENCAO);
        ManutencaoRequestDTO request = new ManutencaoRequestDTO(1L, TipoManutencao.CORRETIVA, 1L, null, null, "Problema", null, null, null, null);
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        assertThrows(ResourceConflictException.class, () -> manutencaoService.criar(request));
    }

    @Test
    @DisplayName("Deve aprovar uma manutenção SOLICITADA")
    void aprovar_quandoStatusSolicitada_deveMudarStatusParaAprovada() {
        manutencao.setStatus(StatusManutencao.SOLICITADA);
        when(manutencaoRepository.findById(1L)).thenReturn(Optional.of(manutencao));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        manutencaoService.aprovar(1L);

        assertEquals(StatusManutencao.APROVADA, manutencao.getStatus());
        verify(manutencaoRepository).save(manutencao);
    }

    @Test
    @DisplayName("Deve lançar exceção ao aprovar manutenção que não está SOLICITADA")
    void aprovar_quandoStatusNaoSolicitada_deveLancarExcecao() {
        manutencao.setStatus(StatusManutencao.APROVADA);
        when(manutencaoRepository.findById(1L)).thenReturn(Optional.of(manutencao));

        assertThrows(ResourceConflictException.class, () -> manutencaoService.aprovar(1L));
    }

    @Test
    @DisplayName("Deve iniciar uma manutenção APROVADA")
    void iniciar_quandoAprovada_deveMudarStatusParaEmAndamento() {
        manutencao.setStatus(StatusManutencao.APROVADA);
        ManutencaoInicioDTO inicioDTO = new ManutencaoInicioDTO();
        inicioDTO.setTecnicoId(2L);

        when(manutencaoRepository.findById(1L)).thenReturn(Optional.of(manutencao));
        when(pessoaRepository.findById(2L)).thenReturn(Optional.of(tecnico));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        manutencaoService.iniciar(1L, inicioDTO);

        assertEquals(StatusManutencao.EM_ANDAMENTO, manutencao.getStatus());
        assertEquals(StatusAtivo.EM_MANUTENCAO, ativo.getStatus());
        verify(ativoRepository).save(ativo);
        verify(manutencaoRepository).save(manutencao);
    }

    @Test
    @DisplayName("Deve concluir uma manutenção EM_ANDAMENTO")
    void concluir_quandoEmAndamento_deveMudarStatusParaConcluida() {
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        ManutencaoConclusaoDTO conclusaoDTO = new ManutencaoConclusaoDTO();
        conclusaoDTO.setDescricaoServico("Serviço feito");
        conclusaoDTO.setCustoReal(BigDecimal.TEN);
        conclusaoDTO.setTempoExecucao(60);

        when(manutencaoRepository.findById(1L)).thenReturn(Optional.of(manutencao));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        manutencaoService.concluir(1L, conclusaoDTO);

        assertEquals(StatusManutencao.CONCLUIDA, manutencao.getStatus());
        assertEquals(StatusAtivo.ATIVO, ativo.getStatus());
        verify(ativoRepository).save(ativo);
        verify(manutencaoRepository).save(manutencao);
    }

    @Test
    @DisplayName("Deve cancelar uma manutenção EM_ANDAMENTO e reativar o ativo")
    void cancelar_quandoEmAndamento_deveCancelarEReativarAtivo() {
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        ManutencaoCancelDTO cancelDTO = new ManutencaoCancelDTO();
        cancelDTO.setMotivo("Motivo");

        when(manutencaoRepository.findById(1L)).thenReturn(Optional.of(manutencao));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        manutencaoService.cancelar(1L, cancelDTO);

        assertEquals(StatusManutencao.CANCELADA, manutencao.getStatus());
        assertEquals(StatusAtivo.ATIVO, ativo.getStatus());
        verify(ativoRepository).save(ativo);
        verify(manutencaoRepository).save(manutencao);
    }

    @Test
    @DisplayName("Deve deletar uma manutenção SOLICITADA")
    void deletar_quandoSolicitada_deveDeletar() {
        manutencao.setStatus(StatusManutencao.SOLICITADA);
        when(manutencaoRepository.findById(1L)).thenReturn(Optional.of(manutencao));

        manutencaoService.deletar(1L);

        verify(manutencaoRepository).delete(manutencao);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar manutenção que não está SOLICITADA")
    void deletar_quandoNaoSolicitada_deveLancarExcecao() {
        manutencao.setStatus(StatusManutencao.APROVADA);
        when(manutencaoRepository.findById(1L)).thenReturn(Optional.of(manutencao));

        assertThrows(ResourceConflictException.class, () -> manutencaoService.deletar(1L));
    }
}
