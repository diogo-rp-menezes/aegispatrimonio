package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.FilialCreateDTO;
import br.com.aegispatrimonio.dto.FilialDTO;
import br.com.aegispatrimonio.dto.FilialUpdateDTO;
import br.com.aegispatrimonio.mapper.FilialMapper;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.repository.FilialRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilialService {

    private final FilialRepository filialRepository;
    private final FilialMapper filialMapper;

    public FilialService(FilialRepository filialRepository, FilialMapper filialMapper) {
        this.filialRepository = filialRepository;
        this.filialMapper = filialMapper;
    }

    @Transactional(readOnly = true)
    public List<FilialDTO> listarTodos() {
        return filialRepository.findAll()
                .stream()
                .map(filialMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FilialDTO buscarPorId(Long id) {
        return filialRepository.findById(id)
                .map(filialMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada com ID: " + id));
    }

    @Transactional
    public FilialDTO criar(FilialCreateDTO filialCreateDTO) {
        Filial filial = filialMapper.toEntity(filialCreateDTO);
        Filial filialSalva = filialRepository.save(filial);
        return filialMapper.toDTO(filialSalva);
    }

    @Transactional
    public FilialDTO atualizar(Long id, FilialUpdateDTO filialUpdateDTO) {
        Filial filial = filialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada com ID: " + id));

        filial.setNome(filialUpdateDTO.nome());
        filial.setCodigo(filialUpdateDTO.codigo());
        filial.setTipo(filialUpdateDTO.tipo());
        filial.setCnpj(filialUpdateDTO.cnpj());
        filial.setEndereco(filialUpdateDTO.endereco());
        filial.setStatus(filialUpdateDTO.status());

        Filial filialAtualizada = filialRepository.save(filial);
        return filialMapper.toDTO(filialAtualizada);
    }

    @Transactional
    public void deletar(Long id) {
        filialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada com ID: " + id));
        filialRepository.deleteById(id);
    }
}
