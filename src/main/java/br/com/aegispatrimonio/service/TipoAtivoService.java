package br.com.aegispatrimonio.service;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.aegispatrimonio.model.TipoAtivo;
import br.com.aegispatrimonio.repository.TipoAtivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TipoAtivoService {

    private final TipoAtivoRepository tipoAtivoRepository;

    @Transactional
    public TipoAtivo criar(TipoAtivo tipoAtivo) {
        log.info("Criando novo tipo de ativo: {}", tipoAtivo.getNome());
        validarNomeUnico(tipoAtivo.getNome());
        return tipoAtivoRepository.save(tipoAtivo);
    }

    @Transactional(readOnly = true)
    public List<TipoAtivo> listarTodos() {
        log.debug("Listando todos os tipos de ativo");
        return tipoAtivoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<TipoAtivo> buscarPorId(Long id) {
        log.debug("Buscando tipo de ativo por ID: {}", id);
        return tipoAtivoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<TipoAtivo> buscarPorNome(String nome) {
        log.debug("Buscando tipo de ativo por nome: {}", nome);
        return tipoAtivoRepository.findByNome(nome);
    }

    @Transactional
    public TipoAtivo atualizar(Long id, TipoAtivo tipoAtivoAtualizado) {
        log.info("Atualizando tipo de ativo ID: {}", id);
        TipoAtivo tipoExistente = tipoAtivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de ativo não encontrado"));
        
        if (!tipoExistente.getNome().equals(tipoAtivoAtualizado.getNome())) {
            validarNomeUnico(tipoAtivoAtualizado.getNome());
        }
        
        tipoExistente.setNome(tipoAtivoAtualizado.getNome());
        tipoExistente.setDescricao(tipoAtivoAtualizado.getDescricao());
        tipoExistente.setCategoriaContabil(tipoAtivoAtualizado.getCategoriaContabil());
        tipoExistente.setIcone(tipoAtivoAtualizado.getIcone());
        
        return tipoAtivoRepository.save(tipoExistente);
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando tipo de ativo ID: {}", id);
        TipoAtivo tipoAtivo = tipoAtivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de ativo não encontrado"));
        tipoAtivoRepository.delete(tipoAtivo);
    }

    private void validarNomeUnico(String nome) {
        if (tipoAtivoRepository.existsByNome(nome)) {
            throw new RuntimeException("Já existe um tipo de ativo com o nome: " + nome);
        }
    }
}