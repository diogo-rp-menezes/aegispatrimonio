package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.AtivoRequestDTO;
import br.com.aegispatrimonio.dto.response.AtivoResponseDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtivoServiceTest {

    @InjectMocks
    private AtivoService ativoService;

    @Mock
    private AtivoRepository ativoRepository;
    @Mock
    private TipoAtivoRepository tipoAtivoRepository;
    @Mock
    private LocalizacaoRepository localizacaoRepository;
    @Mock
    private FornecedorRepository fornecedorRepository;
    @Mock
    private PessoaRepository pessoaRepository;

    private Ativo ativo;
    private TipoAtivo tipoAtivo;
    private Localizacao localizacao;
    private Fornecedor fornecedor;
    private Pessoa pessoa;

    @BeforeEach
    void setUp() {
        tipoAtivo = new TipoAtivo();
        tipoAtivo.setId(1L);
        tipoAtivo.setNome("Eletrônicos");

        localizacao = new Localizacao();
        localizacao.setId(1L);
        localizacao.setNome("Sala 101");

        fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        fornecedor.setNome("Fornecedor Tech");

        pessoa = new Pessoa();
        pessoa.setId(1L);
        pessoa.setNome("João da Silva");

        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setNome("Notebook Dell");
        ativo.setNumeroPatrimonio("NT-001");
        ativo.setTipoAtivo(tipoAtivo);
        ativo.setLocalizacao(localizacao);
        ativo.setFornecedor(fornecedor);
        ativo.setPessoaResponsavel(pessoa);
        ativo.setDataAquisicao(LocalDate.now());
        ativo.setValorAquisicao(new BigDecimal("5000.00"));
        ativo.setValorResidual(new BigDecimal("500.00"));
        ativo.setVidaUtilMeses(36);
    }

    @Test
    @DisplayName("Deve criar um novo ativo com sucesso quando os dados são válidos")
    void criar_quandoDadosValidos_deveRetornarAtivoResponseDTO() {
        AtivoRequestDTO request = new AtivoRequestDTO();
        request.setNome("Novo Ativo");
        request.setNumeroPatrimonio("NT-002");
        request.setTipoAtivoId(1L);
        request.setLocalizacaoId(1L);
        request.setFornecedorId(1L);
        request.setPessoaResponsavelId(1L);
        request.setDataAquisicao(LocalDate.now());
        request.setValorAquisicao(new BigDecimal("3000.00"));

        when(ativoRepository.existsByNumeroPatrimonio("NT-002")).thenReturn(false);
        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(tipoAtivo));
        when(localizacaoRepository.findById(1L)).thenReturn(Optional.of(localizacao));
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(pessoaRepository.findById(1L)).thenReturn(Optional.of(pessoa));

        when(ativoRepository.save(any(Ativo.class))).thenAnswer(invocation -> {
            Ativo ativoSalvo = invocation.getArgument(0);
            ativoSalvo.setId(2L);
            return ativoSalvo;
        });

        AtivoResponseDTO response = ativoService.criar(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Novo Ativo", response.getNome());
        verify(ativoRepository, times(1)).existsByNumeroPatrimonio("NT-002");
        verify(ativoRepository, times(1)).save(any(Ativo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar ativo com número de patrimônio duplicado")
    void criar_quandoNumeroPatrimonioJaExiste_deveLancarExcecao() {
        AtivoRequestDTO request = new AtivoRequestDTO();
        request.setNumeroPatrimonio("NT-001");

        when(ativoRepository.existsByNumeroPatrimonio("NT-001")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> ativoService.criar(request));

        assertTrue(exception.getMessage().contains("Número de patrimônio já existe"));
        verify(ativoRepository, never()).save(any(Ativo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar ativo com Tipo de Ativo inexistente")
    void criar_quandoTipoAtivoNaoExiste_deveLancarExcecao() {
        AtivoRequestDTO request = new AtivoRequestDTO();
        request.setNumeroPatrimonio("NT-003");
        request.setTipoAtivoId(99L);

        when(ativoRepository.existsByNumeroPatrimonio("NT-003")).thenReturn(false);
        when(tipoAtivoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> ativoService.criar(request));

        assertTrue(exception.getMessage().contains("Tipo de ativo não encontrado"));
        verify(ativoRepository, never()).save(any(Ativo.class));
    }

    @Test
    @DisplayName("Deve atualizar um ativo com sucesso quando os dados são válidos")
    void atualizar_quandoDadosValidos_deveRetornarAtivoResponseDTO() {
        Long ativoId = 1L;
        AtivoRequestDTO request = new AtivoRequestDTO();
        request.setNome("Notebook Dell Atualizado");
        request.setNumeroPatrimonio(ativo.getNumeroPatrimonio());
        request.setTipoAtivoId(1L);
        request.setLocalizacaoId(1L);
        request.setFornecedorId(1L);
        request.setPessoaResponsavelId(1L);
        request.setDataAquisicao(ativo.getDataAquisicao());
        request.setValorAquisicao(ativo.getValorAquisicao());

        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));
        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(tipoAtivo));
        when(localizacaoRepository.findById(1L)).thenReturn(Optional.of(localizacao));
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(pessoaRepository.findById(1L)).thenReturn(Optional.of(pessoa));
        when(ativoRepository.save(any(Ativo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AtivoResponseDTO response = ativoService.atualizar(ativoId, request);

        assertNotNull(response);
        assertEquals(ativoId, response.getId());
        assertEquals("Notebook Dell Atualizado", response.getNome());
        verify(ativoRepository, times(1)).findById(ativoId);
        verify(ativoRepository, times(1)).save(any(Ativo.class));
        verify(ativoRepository, never()).existsByNumeroPatrimonio(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar para um número de patrimônio duplicado")
    void atualizar_quandoNumeroPatrimonioDuplicado_deveLancarExcecao() {
        Long ativoId = 1L;
        AtivoRequestDTO request = new AtivoRequestDTO();
        request.setNumeroPatrimonio("NT-999");

        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));
        when(ativoRepository.existsByNumeroPatrimonio("NT-999")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ativoService.atualizar(ativoId, request);
        });

        assertTrue(exception.getMessage().contains("Número de patrimônio já existe"));
        verify(ativoRepository, never()).save(any(Ativo.class));
    }

    @Test
    @DisplayName("Deve 'deletar' um ativo (soft delete) com sucesso quando o ativo existe")
    void deletar_quandoAtivoExiste_deveInvocarSoftDeleteDoRepositorio() {
        // --- Arrange (Organizar) ---
        Long ativoId = 1L;
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));

        // --- Act (Agir) ---
        assertDoesNotThrow(() -> ativoService.deletar(ativoId));

        // --- Assert (Verificar) ---
        // Verificamos se o serviço chamou o método 'delete' no repositório.
        // Graças à anotação @SQLDelete na entidade Ativo, esta chamada ao 'delete'
        // será interceptada pelo Hibernate e transformada em um 'UPDATE' que
        // altera o status do ativo para 'BAIXADO', efetivando o soft delete.
        verify(ativoRepository, times(1)).delete(ativo);
    }

    @Test
    @DisplayName("Deve buscar um ativo por ID e retornar DTO quando encontrado")
    void buscarPorId_quandoAtivoExiste_deveRetornarOptionalDeAtivoResponseDTO() {
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        Optional<AtivoResponseDTO> resultado = ativoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        AtivoResponseDTO dto = resultado.get();
        assertEquals(ativo.getId(), dto.getId());
        assertEquals(ativo.getNome(), dto.getNome());
        verify(ativoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve buscar um ativo por ID e retornar vazio quando não encontrado")
    void buscarPorId_quandoAtivoNaoExiste_deveRetornarOptionalVazio() {
        when(ativoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<AtivoResponseDTO> resultado = ativoService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
        verify(ativoRepository, times(1)).findById(99L);
    }
}
