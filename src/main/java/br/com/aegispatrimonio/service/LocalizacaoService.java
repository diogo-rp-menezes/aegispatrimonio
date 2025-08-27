package br.com.aegispatrimonio.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.aegispatrimonio.model.Localizacao;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalizacaoService {

    private final LocalizacaoRepository localizacaoRepository;

    @Transactional
    public Localizacao criar(Localizacao localizacao) {
        log.info("Criando nova localização: {}", localizacao.getNome());
        return localizacaoRepository.save(localizacao);
    }

    @Transactional(readOnly = true)
    public List<Localizacao> listarPorFilial(Long filialId) {
        log.debug("Listando localizações por filial: {}", filialId);
        return localizacaoRepository.findByFilialId(filialId);
    }

    @Transactional(readOnly = true)
    public List<Localizacao> buscarPorNome(String nome) {
        log.debug("Buscando localizações por nome: {}", nome);
        return localizacaoRepository.findByNomeContaining(nome);
    }
}