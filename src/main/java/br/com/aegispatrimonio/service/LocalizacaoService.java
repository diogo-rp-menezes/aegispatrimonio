package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.LocalizacaoCreateDTO;
import br.com.aegispatrimonio.dto.LocalizacaoDTO;
import br.com.aegispatrimonio.dto.LocalizacaoUpdateDTO;
import br.com.aegispatrimonio.mapper.LocalizacaoMapper;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Localizacao;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import br.com.aegispatrimonio.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LocalizacaoService {

    private final LocalizacaoRepository localizacaoRepository;
    private final LocalizacaoMapper localizacaoMapper;
    private final FilialRepository filialRepository;
    private final AtivoRepository ativoRepository;

    public LocalizacaoService(LocalizacaoRepository localizacaoRepository, LocalizacaoMapper localizacaoMapper, FilialRepository filialRepository, AtivoRepository ativoRepository) {
        this.localizacaoRepository = localizacaoRepository;
        this.localizacaoMapper = localizacaoMapper;
        this.filialRepository = filialRepository;
        this.ativoRepository = ativoRepository;
    }

    private Pessoa getPessoaLogada() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getPessoa();
    }

    private boolean isAdmin(Pessoa pessoa) {
        return "ROLE_ADMIN".equals(pessoa.getRole());
    }

    @Transactional(readOnly = true)
    public List<LocalizacaoDTO> listarTodos() {
        Pessoa pessoaLogada = getPessoaLogada();
        if (isAdmin(pessoaLogada)) {
            return localizacaoRepository.findAll().stream().map(localizacaoMapper::toDTO).collect(Collectors.toList());
        }
        Long filialId = pessoaLogada.getFilial().getId();
        return localizacaoRepository.findByFilialId(filialId).stream().map(localizacaoMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LocalizacaoDTO buscarPorId(Long id) {
        Pessoa pessoaLogada = getPessoaLogada();
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Localização não encontrada com ID: " + id));

        if (!isAdmin(pessoaLogada) && !localizacao.getFilial().getId().equals(pessoaLogada.getFilial().getId())) {
            throw new AccessDeniedException("Você não tem permissão para acessar localizações de outra filial.");
        }

        return localizacaoMapper.toDTO(localizacao);
    }

    @Transactional
    public LocalizacaoDTO criar(LocalizacaoCreateDTO localizacaoCreateDTO) {
        Pessoa pessoaLogada = getPessoaLogada();

        if (!isAdmin(pessoaLogada) && !localizacaoCreateDTO.filialId().equals(pessoaLogada.getFilial().getId())) {
            throw new AccessDeniedException("Você só pode criar localizações para a sua própria filial.");
        }

        Filial filial = filialRepository.findById(localizacaoCreateDTO.filialId())
                .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada com ID: " + localizacaoCreateDTO.filialId()));

        Localizacao localizacaoPai = null;
        if (localizacaoCreateDTO.localizacaoPaiId() != null) {
            localizacaoPai = localizacaoRepository.findById(localizacaoCreateDTO.localizacaoPaiId())
                    .orElseThrow(() -> new EntityNotFoundException("Localização Pai não encontrada com ID: " + localizacaoCreateDTO.localizacaoPaiId()));
            validarConsistenciaHierarquia(localizacaoPai, filial);
        }

        validarNomeUnico(localizacaoCreateDTO.nome(), filial, localizacaoPai, null);

        Localizacao localizacao = localizacaoMapper.toEntity(localizacaoCreateDTO);
        localizacao.setFilial(filial);
        localizacao.setLocalizacaoPai(localizacaoPai);

        Localizacao localizacaoSalva = localizacaoRepository.save(localizacao);
        return localizacaoMapper.toDTO(localizacaoSalva);
    }

    @Transactional
    public LocalizacaoDTO atualizar(Long id, LocalizacaoUpdateDTO localizacaoUpdateDTO) {
        Pessoa pessoaLogada = getPessoaLogada();
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Localização não encontrada com ID: " + id));

        if (!isAdmin(pessoaLogada)) {
            if (!localizacao.getFilial().getId().equals(pessoaLogada.getFilial().getId())) {
                throw new AccessDeniedException("Você não tem permissão para editar localizações de outra filial.");
            }
            if (localizacaoUpdateDTO.filialId() != null && !localizacao.getFilial().getId().equals(localizacaoUpdateDTO.filialId())) {
                throw new AccessDeniedException("Você não tem permissão para transferir localizações entre filiais.");
            }
        }

        Filial filial = filialRepository.findById(localizacaoUpdateDTO.filialId())
                .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada com ID: " + localizacaoUpdateDTO.filialId()));

        Localizacao localizacaoPai = null;
        if (localizacaoUpdateDTO.localizacaoPaiId() != null) {
            if (id.equals(localizacaoUpdateDTO.localizacaoPaiId())) {
                throw new IllegalArgumentException("Uma localização não pode ser sua própria localização pai.");
            }
            localizacaoPai = localizacaoRepository.findById(localizacaoUpdateDTO.localizacaoPaiId())
                    .orElseThrow(() -> new EntityNotFoundException("Localização Pai não encontrada com ID: " + localizacaoUpdateDTO.localizacaoPaiId()));
            validarConsistenciaHierarquia(localizacaoPai, filial);
        }

        validarNomeUnico(localizacaoUpdateDTO.nome(), filial, localizacaoPai, id);

        localizacao.setNome(localizacaoUpdateDTO.nome());
        localizacao.setDescricao(localizacaoUpdateDTO.descricao());
        localizacao.setStatus(localizacaoUpdateDTO.status());
        localizacao.setFilial(filial);
        localizacao.setLocalizacaoPai(localizacaoPai);

        Localizacao localizacaoAtualizada = localizacaoRepository.save(localizacao);
        return localizacaoMapper.toDTO(localizacaoAtualizada);
    }

    @Transactional
    public void deletar(Long id) {
        Pessoa pessoaLogada = getPessoaLogada();
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Localização não encontrada com ID: " + id));

        if (!isAdmin(pessoaLogada) && !localizacao.getFilial().getId().equals(pessoaLogada.getFilial().getId())) {
            throw new AccessDeniedException("Você não tem permissão para deletar localizações de outra filial.");
        }

        if (ativoRepository.existsByLocalizacaoId(id)) {
            throw new IllegalStateException("Não é possível deletar a localização, pois existem ativos associados a ela.");
        }

        if (localizacaoRepository.existsByLocalizacaoPaiId(id)) {
            throw new IllegalStateException("Não é possível deletar a localização, pois ela é uma localização pai para outras localizações.");
        }

        localizacaoRepository.delete(localizacao);
    }

    private void validarNomeUnico(String nome, Filial filial, Localizacao localizacaoPai, Long id) {
        Optional<Localizacao> existente = localizacaoRepository.findByNomeAndFilialAndLocalizacaoPai(nome, filial, localizacaoPai);
        if (existente.isPresent() && !existente.get().getId().equals(id)) {
            throw new IllegalArgumentException("Já existe uma localização com este nome dentro da mesma filial e localização pai.");
        }
    }

    private void validarConsistenciaHierarquia(Localizacao pai, Filial filial) {
        if (!pai.getFilial().getId().equals(filial.getId())) {
            throw new IllegalArgumentException("A localização pai deve pertencer à mesma filial.");
        }
    }
}
