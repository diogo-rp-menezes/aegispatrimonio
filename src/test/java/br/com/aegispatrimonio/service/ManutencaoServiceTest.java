package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.ManutencaoCancelDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoConclusaoDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoInicioDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoRequestDTO;
import br.com.aegispatrimonio.exception.ResourceConflictException;
import br.com.aegispatrimonio.exception.ResourceNotFoundException;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FornecedorRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import br.com.aegispatrimonio.repository.ManutencaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManutencaoServiceTest {

    @Mock
    private ManutencaoRepository manutencaoRepository;
    @Mock
    private AtivoRepository ativoRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private FornecedorRepository fornecedorRepository;

    @InjectMocks
    private ManutencaoService manutencaoService;

    private Ativo ativo;
    private Funcionario solicitante;
    private Funcionario tecnico;
    private Manutencao manutencao;
    private Filial filial;

    @BeforeEach
    void setUp() {
        filial = new Filial();
        filial.setId(1L);

        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setStatus(StatusAtivo.ATIVO);
        ativo.setFilial(filial);

        solicitante = new Funcionario();
        solicitante.setId(1L);
        solicitante.setFiliais(Set.of(filial));

        tecnico = new Funcionario();
        tecnico.setId(2L);
        tecnico.setFiliais(Set.of(filial));

        manutencao = new Manutencao();
        manutencao.setId(1L);
        manutencao.setAtivo(ativo);
        manutencao.setSolicitante(solicitante);
    }

    @Test
    @DisplayName("Criar: Deve criar uma manutenção com sucesso")
    void criar_quandoValido_deveSalvarManutencao() {
        ManutencaoRequestDTO request = new ManutencaoRequestDTO(1L, TipoManutencao.CORRETIVA, 1L, null, null, "Problema", null, null, null, null);
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(solicitante));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        assertDoesNotThrow(() -> manutencaoService.criar(request));

        verify(manutencaoRepository).save(any(Manutencao.class));
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção ao criar manutenção para ativo não ATIVO")
    void criar_quandoAtivoNaoAtivo_deveLancarExcecao() {
        ativo.setStatus(StatusAtivo.EM_MANUTENCAO);
        ManutencaoRequestDTO request = new ManutencaoRequestDTO(1L, TipoManutencao.CORRETIVA, 1L, null, null, "Problema", null, null, null, null);
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        assertThrows(ResourceConflictException.class, () -> manutencaoService.criar(request));
    }

    @Test
    @DisplayName("Aprovar: Deve aprovar uma manutenção SOLICITADA")
    void aprovar_quandoStatusSolicitada_deveMudarStatusParaAprovada() {
        manutencao.setStatus(StatusManutencao.SOLICITADA);
        when(manutencaoRepository.findById(1L)).thenReturn(Optional.of(manutencao));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        manutencaoService.aprovar(1L);

        assertEquals(StatusManutencao.APROVADA, manutencao.getStatus());
        verify(manutencaoRepository).save(manutencao);
    }

    @Test
    @DisplayName("Iniciar: Deve iniciar uma manutenção APROVADA")
    void iniciar_quandoAprovada_deveMudarStatusParaEmAndamento() {
        manutencao.setStatus(StatusManutencao.APROVADA);
        ManutencaoInicioDTO inicioDTO = new ManutencaoInicioDTO();
        inicioDTO.setTecnicoId(2L);

        when(manutencaoRepository.findById(1L)).thenReturn(Optional.of(manutencao));
        when(funcionarioRepository.findById(2L)).thenReturn(Optional.of(tecnico));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        manutencaoService.iniciar(1L, inicioDTO);

        assertEquals(StatusManutencao.EM_ANDAMENTO, manutencao.getStatus());
        assertEquals(StatusAtivo.EM_MANUTENCAO, ativo.getStatus());
        verify(ativoRepository).save(ativo);
        verify(manutencaoRepository).save(manutencao);
    }

    @Test
    @DisplayName("Concluir: Deve concluir uma manutenção EM_ANDAMENTO")
    void concluir_quandoEmAndamento_deveMudarStatusParaConcluida() {
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        ManutencaoConclusaoDTO conclusaoDTO = new ManutencaoConclusaoDTO("Serviço feito", BigDecimal.TEN, 60);

        when(manutencaoRepository.findById(1L)).thenReturn(Optional.of(manutencao));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        manutencaoService.concluir(1L, conclusaoDTO);

        assertEquals(StatusManutencao.CONCLUIDA, manutencao.getStatus());
        assertEquals(StatusAtivo.ATIVO, ativo.getStatus());
        verify(ativoRepository).save(ativo);
        verify(manutencaoRepository).save(manutencao);
    }

    @Test
    @DisplayName("Deletar: Deve deletar uma manutenção SOLICITADA")
    void deletar_quandoSolicitada_deveDeletar() {
        manutencao.setStatus(StatusManutencao.SOLICITADA);
        when(manutencaoRepository.findById(1L)).thenReturn(Optional.of(manutencao));

        manutencaoService.deletar(1L);

        verify(manutencaoRepository).delete(manutencao);
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção ao deletar manutenção que não está SOLICITADA")
    void deletar_quandoNaoSolicitada_deveLancarExcecao() {
        manutencao.setStatus(StatusManutencao.APROVADA);
        when(manutencaoRepository.findById(1L)).thenReturn(Optional.of(manutencao));

        assertThrows(ResourceConflictException.class, () -> manutencaoService.deletar(1L));
    }
}
