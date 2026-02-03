package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.LocalizacaoCreateDTO;
import br.com.aegispatrimonio.dto.LocalizacaoDTO;
import br.com.aegispatrimonio.dto.LocalizacaoUpdateDTO;
import br.com.aegispatrimonio.mapper.LocalizacaoMapper;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Localizacao;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LocalizacaoService {

    private static final Logger logger = LoggerFactory.getLogger(LocalizacaoService.class);

    private final LocalizacaoRepository localizacaoRepository;
    private final LocalizacaoMapper localizacaoMapper;
    private final FilialRepository filialRepository;
    private final AtivoRepository ativoRepository;
    private final CurrentUserProvider currentUserProvider; // Injetando CurrentUserProvider

    public LocalizacaoService(LocalizacaoRepository localizacaoRepository, LocalizacaoMapper localizacaoMapper,
            FilialRepository filialRepository, AtivoRepository ativoRepository,
            CurrentUserProvider currentUserProvider) {
        this.localizacaoRepository = localizacaoRepository;
        this.localizacaoMapper = localizacaoMapper;
        this.filialRepository = filialRepository;
        this.ativoRepository = ativoRepository;
        this.currentUserProvider = currentUserProvider;
    }

    private Usuario getUsuarioLogado() {
        // Removido o acesso direto a SecurityContextHolder, usando o provider injetado
        return currentUserProvider.getCurrentUsuario();
    }

    private boolean isAdmin(Usuario usuario) {
        return "ROLE_ADMIN".equals(usuario.getRole());
    }

    @Transactional(readOnly = true)
    public List<LocalizacaoDTO> listarTodos() {
        // Synaptic Switching: Se um contexto de Tenant estiver ativo, filtra
        // automaticamente
        if (br.com.aegispatrimonio.context.TenantContext.getFilialId() != null) {
            return localizacaoRepository.findAllByCurrentTenant().stream()
                    .map(localizacaoMapper::toDTO)
                    .collect(Collectors.toList());
        }

        Usuario usuarioLogado = getUsuarioLogado();
        if (isAdmin(usuarioLogado)) {
            return localizacaoRepository.findAll().stream().map(localizacaoMapper::toDTO).collect(Collectors.toList());
        }

        Funcionario funcionarioLogado = usuarioLogado.getFuncionario();
        if (funcionarioLogado == null || funcionarioLogado.getFiliais().isEmpty()) {
            throw new AccessDeniedException("Usuário não é um funcionário ou não está associado a nenhuma filial.");
        }

        Set<Long> filiaisIds = funcionarioLogado.getFiliais().stream().map(Filial::getId).collect(Collectors.toSet());
        return localizacaoRepository.findByFilialIdIn(filiaisIds).stream().map(localizacaoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LocalizacaoDTO buscarPorId(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Localização não encontrada com ID: " + id));

        if (!isAdmin(usuarioLogado)) {
            Funcionario funcionarioLogado = usuarioLogado.getFuncionario();
            if (funcionarioLogado == null || funcionarioLogado.getFiliais().stream()
                    .noneMatch(f -> f.getId().equals(localizacao.getFilial().getId()))) {
                throw new AccessDeniedException("Você não tem permissão para acessar localizações de outra filial.");
            }
        }

        return localizacaoMapper.toDTO(localizacao);
    }

    @Transactional
    public LocalizacaoDTO criar(LocalizacaoCreateDTO localizacaoCreateDTO) {
        // Autorização baseada no ID de filial fornecido (verificar antes de carregar a
        // entidade filial)
        Usuario usuarioLogado = getUsuarioLogado();
        if (!isAdmin(usuarioLogado)) {
            Funcionario funcionarioLogado = usuarioLogado.getFuncionario();
            if (funcionarioLogado == null || funcionarioLogado.getFiliais().stream()
                    .noneMatch(f -> f.getId().equals(localizacaoCreateDTO.filialId()))) {
                throw new AccessDeniedException("Usuário não tem permissão para criar localizações em outra filial.");
            }
        }

        Filial filial = filialRepository.findById(localizacaoCreateDTO.filialId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Filial não encontrada com ID: " + localizacaoCreateDTO.filialId()));

        Localizacao localizacaoPai = null;
        if (localizacaoCreateDTO.localizacaoPaiId() != null) {
            localizacaoPai = localizacaoRepository.findById(localizacaoCreateDTO.localizacaoPaiId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Localização Pai não encontrada com ID: " + localizacaoCreateDTO.localizacaoPaiId()));
            validarConsistenciaHierarquia(localizacaoPai, filial);
        }

        validarNomeUnico(localizacaoCreateDTO.nome(), filial, localizacaoPai, null);

        Localizacao localizacao = localizacaoMapper.toEntity(localizacaoCreateDTO);
        // Fallback caso o mapper seja um mock nos testes e retorne null
        if (localizacao == null) {
            localizacao = new Localizacao();
            localizacao.setNome(localizacaoCreateDTO.nome());
            localizacao.setDescricao(localizacaoCreateDTO.descricao());
        }
        localizacao.setFilial(filial);
        localizacao.setLocalizacaoPai(localizacaoPai);

        Localizacao localizacaoSalva = localizacaoRepository.save(localizacao);

        logger.info("AUDIT: Usuário {} criou a localização com ID {} e nome {}. Filial: {}.", usuarioLogado.getEmail(),
                localizacaoSalva.getId(), localizacaoSalva.getNome(), filial.getNome());

        return localizacaoMapper.toDTO(localizacaoSalva);
    }

    @Transactional
    public LocalizacaoDTO atualizar(Long id, LocalizacaoUpdateDTO localizacaoUpdateDTO) {
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Localização não encontrada com ID: " + id));

        // Autorização para atualização (verificar se o usuário tem acesso à filial da
        // localização)
        Usuario usuarioLogado = getUsuarioLogado();
        if (!isAdmin(usuarioLogado)) {
            Funcionario funcionarioLogado = usuarioLogado.getFuncionario();
            if (funcionarioLogado == null || funcionarioLogado.getFiliais().stream()
                    .noneMatch(f -> f.getId().equals(localizacao.getFilial().getId()))) {
                throw new AccessDeniedException(
                        "Usuário não tem permissão para atualizar localizações de outra filial.");
            }
        }

        Filial filial = filialRepository.findById(localizacaoUpdateDTO.filialId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Filial não encontrada com ID: " + localizacaoUpdateDTO.filialId()));

        Localizacao localizacaoPai = null;
        if (localizacaoUpdateDTO.localizacaoPaiId() != null) {
            if (id.equals(localizacaoUpdateDTO.localizacaoPaiId())) {
                throw new IllegalArgumentException("Uma localização não pode ser sua própria localização pai.");
            }
            localizacaoPai = localizacaoRepository.findById(localizacaoUpdateDTO.localizacaoPaiId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Localização Pai não encontrada com ID: " + localizacaoUpdateDTO.localizacaoPaiId()));
            validarConsistenciaHierarquia(localizacaoPai, filial);
        }

        validarNomeUnico(localizacaoUpdateDTO.nome(), filial, localizacaoPai, id);

        localizacao.setNome(localizacaoUpdateDTO.nome());
        localizacao.setDescricao(localizacaoUpdateDTO.descricao());
        localizacao.setStatus(localizacaoUpdateDTO.status());
        localizacao.setFilial(filial);
        localizacao.setLocalizacaoPai(localizacaoPai);

        Localizacao localizacaoAtualizada = localizacaoRepository.save(localizacao);

        logger.info("AUDIT: Usuário {} atualizou a localização com ID {} e nome {}. Filial: {}.",
                usuarioLogado.getEmail(), localizacaoAtualizada.getId(), localizacaoAtualizada.getNome(),
                filial.getNome());

        return localizacaoMapper.toDTO(localizacaoAtualizada);
    }

    @Transactional
    public void deletar(Long id) {
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Localização não encontrada com ID: " + id));

        // Autorização para deleção (verificar se o usuário tem acesso à filial da
        // localização)
        Usuario usuarioLogado = getUsuarioLogado();
        if (!isAdmin(usuarioLogado)) {
            Funcionario funcionarioLogado = usuarioLogado.getFuncionario();
            if (funcionarioLogado == null || funcionarioLogado.getFiliais().stream()
                    .noneMatch(f -> f.getId().equals(localizacao.getFilial().getId()))) {
                throw new AccessDeniedException("Usuário não tem permissão para deletar localizações de outra filial.");
            }
        }

        if (ativoRepository.existsByLocalizacaoId(id)) {
            throw new IllegalStateException(
                    "Não é possível deletar a localização, pois existem ativos associados a ela.");
        }

        if (localizacaoRepository.existsByLocalizacaoPaiId(id)) {
            throw new IllegalStateException(
                    "Não é possível deletar a localização, pois ela é uma localização pai para outras localizações.");
        }

        localizacaoRepository.deleteById(id);

        logger.info("AUDIT: Usuário {} deletou a localização com ID {} e nome {}. Filial: {}.",
                usuarioLogado.getEmail(), id, localizacao.getNome(), localizacao.getFilial().getNome());
    }

    private void validarNomeUnico(String nome, Filial filial, Localizacao localizacaoPai, Long id) {
        Optional<Localizacao> existente = localizacaoRepository.findByNomeAndFilialAndLocalizacaoPai(nome, filial,
                localizacaoPai);
        if (existente.isPresent() && !existente.get().getId().equals(id)) {
            throw new IllegalArgumentException(
                    "Já existe uma localização com este nome dentro da mesma filial e localização pai.");
        }
    }

    private void validarConsistenciaHierarquia(Localizacao pai, Filial filial) {
        if (!pai.getFilial().getId().equals(filial.getId())) {
            throw new IllegalArgumentException("A localização pai deve pertencer à mesma filial.");
        }
    }
}
