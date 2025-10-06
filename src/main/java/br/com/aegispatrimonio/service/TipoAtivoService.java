package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.TipoAtivoCreateDTO;
import br.com.aegispatrimonio.dto.TipoAtivoDTO;
import br.com.aegispatrimonio.mapper.TipoAtivoMapper;
import br.com.aegispatrimonio.model.TipoAtivo;
import br.com.aegispatrimonio.repository.TipoAtivoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TipoAtivoService {

    private final TipoAtivoRepository tipoAtivoRepository;
    private final TipoAtivoMapper tipoAtivoMapper;

    public TipoAtivoService(TipoAtivoRepository tipoAtivoRepository, TipoAtivoMapper tipoAtivoMapper) {
        this.tipoAtivoRepository = tipoAtivoRepository;
        this.tipoAtivoMapper = tipoAtivoMapper;
    }

    @Transactional(readOnly = true)
    public List<TipoAtivoDTO> listarTodos() {
        return tipoAtivoRepository.findAll().stream()
                .map(tipoAtivoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TipoAtivoDTO buscarPorId(Long id) {
        return tipoAtivoRepository.findById(id)
                .map(tipoAtivoMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de Ativo não encontrado com ID: " + id));
    }

    @Transactional
    public TipoAtivoDTO criar(TipoAtivoCreateDTO tipoAtivoCreateDTO) {
        TipoAtivo tipoAtivo = tipoAtivoMapper.toEntity(tipoAtivoCreateDTO);
        TipoAtivo tipoAtivoSalvo = tipoAtivoRepository.save(tipoAtivo);
        return tipoAtivoMapper.toDTO(tipoAtivoSalvo);
    }

    @Transactional
    public TipoAtivoDTO atualizar(Long id, TipoAtivoCreateDTO tipoAtivoUpdateDTO) {
        TipoAtivo tipoAtivo = tipoAtivoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de Ativo não encontrado com ID: " + id));
        tipoAtivo.setNome(tipoAtivoUpdateDTO.nome());
        TipoAtivo tipoAtivoAtualizado = tipoAtivoRepository.save(tipoAtivo);
        return tipoAtivoMapper.toDTO(tipoAtivoAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        tipoAtivoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de Ativo não encontrado com ID: " + id));
        tipoAtivoRepository.deleteById(id);
    }
}
