package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.AtivoHealthHistoryDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.dto.query.AtivoQueryParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAtivoService {
    Page<AtivoDTO> listarTodos(Pageable pageable, AtivoQueryParams queryParams);
    AtivoDTO buscarPorId(Long id);
    AtivoDTO criar(AtivoCreateDTO ativoCreateDTO);
    AtivoDTO atualizar(Long id, AtivoUpdateDTO ativoUpdateDTO);
    void deletar(Long id);
    List<AtivoHealthHistoryDTO> getHealthHistory(Long ativoId);
}
