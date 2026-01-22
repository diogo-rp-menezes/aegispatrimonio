package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.model.StatusAtivo;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAtivoService {
    List<AtivoDTO> listarTodos(Pageable pageable, Long filialId, Long tipoAtivoId, StatusAtivo status);
    AtivoDTO buscarPorId(Long id);
    AtivoDTO criar(AtivoCreateDTO ativoCreateDTO);
    AtivoDTO atualizar(Long id, AtivoUpdateDTO ativoUpdateDTO);
    void deletar(Long id);
}
