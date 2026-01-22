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
    private FornecedorRepository fornecedorRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private CurrentUserProvider currentUserProvider; // Adicionado mock para CurrentUserProvider

    @InjectMocks
    private ManutencaoService manutencaoService;

    private Ativo ativo;
    private Funcionario solicitante;
    private Funcionario tecnico;
    private Manutencao manutencao;
    private Filial filial;
    private Usuario adminUser; // Adicionado para mockar o usuário

    @BeforeEach
    void setUp() {
        filial = new Filial();
        filial.setId(1L);

        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setStatus(StatusAtivo.ATIVO);
        ativo.setFilial(filial);
        ativo.setNome("Notebook Dell");
        ativo.setNumeroPatrimonio("NTB-001");

        solicitante = new Funcionario();
        solicitante.setId(10L);
        solicitante.setFiliais(Set.of(filial));
        solicitante.setNome("Solicitante Teste");

        tecnico = new Funcionario();
        tecnico.setId(20L);
        tecnico.setFiliais(Set.of(filial));
        tecnico.setNome("Tecnico Teste");

        manutencao = new Manutencao();
        manutencao.setId(100L);
        manutencao.setAtivo(ativo);
        manutencao.setSolicitante(solicitante);
        manutencao.setStatus(StatusManutencao.SOLICITADA);

        adminUser = new Usuario();
        adminUser.setId(1L);
        adminUser.setRole("ROLE_ADMIN");
        lenient().when(currentUserProvider.getCurrentUsuario()).thenReturn(adminUser); // Mockando o usuário logado (lenient para evitar UnnecessaryStubbing)
    }

    @Test
    @DisplayName("Criar: Deve criar manutenção com sucesso para ativo ATIVO")
    void criar_comAtivoAtivo_deveCriarComSucesso() {
        ManutencaoRequestDTO request = new ManutencaoRequestDTO(1L, TipoManutencao.CORRETIVA, 10L, null, null, "Tela quebrada", null, null, null, null);
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));
        when(funcionarioRepository.findById(10L)).thenReturn(Optional.of(solicitante));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        assertNotNull(manutencaoService.criar(request));
        verify(manutencaoRepository).save(any(Manutencao.class));
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção para ativo com status diferente de ATIVO")
    void criar_comAtivoNaoAtivo_deveLancarExcecao() {
        ativo.setStatus(StatusAtivo.EM_MANUTENCAO);
        ManutencaoRequestDTO request = new ManutencaoRequestDTO(1L, TipoManutencao.CORRETIVA, 10L, null, null, "Tela quebrada", null, null, null, null);
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        assertThrows(ResourceConflictException.class, () -> manutencaoService.criar(request));
    }

    @Test
    @DisplayName("Aprovar: Deve aprovar manutenção com status SOLICITADA")
    void aprovar_comStatusSolicitada_deveAprovar() {
        when(manutencaoRepository.findById(100L)).thenReturn(Optional.of(manutencao));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        manutencaoService.aprovar(100L);

        assertEquals(StatusManutencao.APROVADA, manutencao.getStatus());
        verify(manutencaoRepository).save(manutencao);
    }

    @Test
    @DisplayName("Aprovar: Deve lançar exceção para status diferente de SOLICITADA")
    void aprovar_comStatusDiferente_deveLancarExcecao() {
        manutencao.setStatus(StatusManutencao.APROVADA);
        when(manutencaoRepository.findById(100L)).thenReturn(Optional.of(manutencao));

        assertThrows(ResourceConflictException.class, () -> manutencaoService.aprovar(100L));
    }

    @Test
    @DisplayName("Iniciar: Deve iniciar manutenção com status APROVADA")
    void iniciar_comStatusAprovada_deveIniciar() {
        manutencao.setStatus(StatusManutencao.APROVADA);
        ManutencaoInicioDTO inicioDTO = new ManutencaoInicioDTO(20L);
        when(manutencaoRepository.findById(100L)).thenReturn(Optional.of(manutencao));
        when(funcionarioRepository.findById(20L)).thenReturn(Optional.of(tecnico));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        manutencaoService.iniciar(100L, inicioDTO);

        assertEquals(StatusManutencao.EM_ANDAMENTO, manutencao.getStatus());
        assertEquals(StatusAtivo.EM_MANUTENCAO, ativo.getStatus());
        verify(ativoRepository).save(ativo);
        verify(manutencaoRepository).save(manutencao);
    }

    @Test
    @DisplayName("Iniciar: Deve lançar exceção se técnico não pertence à filial do ativo")
    void iniciar_comTecnicoDeOutraFilial_deveLancarExcecao() {
        manutencao.setStatus(StatusManutencao.APROVADA);
        
        // CORREÇÃO: A filial mockada deve ter um ID para evitar NullPointerException na lógica de validação.
        Filial outraFilial = new Filial();
        outraFilial.setId(99L);

        Funcionario tecnicoOutraFilial = new Funcionario();
        tecnicoOutraFilial.setId(30L);
        tecnicoOutraFilial.setFiliais(Set.of(outraFilial));
        ManutencaoInicioDTO inicioDTO = new ManutencaoInicioDTO(30L);

        when(manutencaoRepository.findById(100L)).thenReturn(Optional.of(manutencao));
        when(funcionarioRepository.findById(30L)).thenReturn(Optional.of(tecnicoOutraFilial));

        assertThrows(IllegalArgumentException.class, () -> manutencaoService.iniciar(100L, inicioDTO));
    }

    @Test
    @DisplayName("Concluir: Deve concluir manutenção com status EM_ANDAMENTO")
    void concluir_comStatusEmAndamento_deveConcluir() {
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        ManutencaoConclusaoDTO conclusaoDTO = new ManutencaoConclusaoDTO("Serviço finalizado", BigDecimal.TEN, 60);
        when(manutencaoRepository.findById(100L)).thenReturn(Optional.of(manutencao));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        manutencaoService.concluir(100L, conclusaoDTO);

        assertEquals(StatusManutencao.CONCLUIDA, manutencao.getStatus());
        assertEquals(StatusAtivo.ATIVO, ativo.getStatus());
        assertEquals("Serviço finalizado", manutencao.getDescricaoServico());
        verify(ativoRepository).save(ativo);
        verify(manutencaoRepository).save(manutencao);
    }

    @Test
    @DisplayName("Cancelar: Deve cancelar manutenção com status SOLICITADA")
    void cancelar_comStatusSolicitada_deveCancelar() {
        ManutencaoCancelDTO cancelDTO = new ManutencaoCancelDTO("Motivo teste");
        when(manutencaoRepository.findById(100L)).thenReturn(Optional.of(manutencao));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        manutencaoService.cancelar(100L, cancelDTO);

        assertEquals(StatusManutencao.CANCELADA, manutencao.getStatus());
        assertEquals(StatusAtivo.ATIVO, ativo.getStatus()); // Garante que o status do ativo não mudou
        verify(ativoRepository, never()).save(any(Ativo.class));
        verify(manutencaoRepository).save(manutencao);
    }

    @Test
    @DisplayName("Cancelar: Deve cancelar manutenção EM_ANDAMENTO e reativar o ativo")
    void cancelar_comStatusEmAndamento_deveCancelarEReativarAtivo() {
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        ativo.setStatus(StatusAtivo.EM_MANUTENCAO);
        ManutencaoCancelDTO cancelDTO = new ManutencaoCancelDTO("Motivo teste");
        when(manutencaoRepository.findById(100L)).thenReturn(Optional.of(manutencao));
        when(manutencaoRepository.save(any(Manutencao.class))).thenReturn(manutencao);

        manutencaoService.cancelar(100L, cancelDTO);

        assertEquals(StatusManutencao.CANCELADA, manutencao.getStatus());
        assertEquals(StatusAtivo.ATIVO, ativo.getStatus());
        verify(ativoRepository).save(ativo);
        verify(manutencaoRepository).save(manutencao);
    }

    @Test
    @DisplayName("Cancelar: Deve lançar exceção para manutenção CONCLUIDA")
    void cancelar_comStatusConcluida_deveLancarExcecao() {
        manutencao.setStatus(StatusManutencao.CONCLUIDA);
        ManutencaoCancelDTO cancelDTO = new ManutencaoCancelDTO("Motivo teste");
        when(manutencaoRepository.findById(100L)).thenReturn(Optional.of(manutencao));

        assertThrows(ResourceConflictException.class, () -> manutencaoService.cancelar(100L, cancelDTO));
    }

    @Test
    @DisplayName("Deletar: Deve deletar manutenção com status SOLICITADA")
    void deletar_comStatusSolicitada_deveDeletar() {
        when(manutencaoRepository.findById(100L)).thenReturn(Optional.of(manutencao));

        manutencaoService.deletar(100L);

        verify(manutencaoRepository).delete(manutencao);
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção para status diferente de SOLICITADA")
    void deletar_comStatusDiferente_deveLancarExcecao() {
        manutencao.setStatus(StatusManutencao.APROVADA);
        when(manutencaoRepository.findById(100L)).thenReturn(Optional.of(manutencao));

        assertThrows(ResourceConflictException.class, () -> manutencaoService.deletar(100L));
    }

    @Test
    @DisplayName("CustoTotalPorAtivo: Deve retornar o custo total de manutenções concluídas")
    void custoTotalPorAtivo_deveRetornarSoma() {
        when(ativoRepository.existsById(1L)).thenReturn(true);
        when(manutencaoRepository.findCustoTotalManutencaoPorAtivo(1L)).thenReturn(new BigDecimal("150.75"));

        BigDecimal custo = manutencaoService.custoTotalPorAtivo(1L);

        assertEquals(new BigDecimal("150.75"), custo);
    }

    @Test
    @DisplayName("CustoTotalPorAtivo: Deve retornar ZERO se não houver custos")
    void custoTotalPorAtivo_semCustos_deveRetornarZero() {
        when(ativoRepository.existsById(1L)).thenReturn(true);
        when(manutencaoRepository.findCustoTotalManutencaoPorAtivo(1L)).thenReturn(null);

        BigDecimal custo = manutencaoService.custoTotalPorAtivo(1L);

        assertEquals(BigDecimal.ZERO, custo);
    }

    @Test
    @DisplayName("CustoTotalPorAtivo: Deve lançar exceção se ativo não existe")
    void custoTotalPorAtivo_comAtivoInexistente_deveLancarExcecao() {
        when(ativoRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> manutencaoService.custoTotalPorAtivo(99L));
    }
}
