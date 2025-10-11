package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.mapper.HealthCheckMapper;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthCheckService {

    private final AtivoRepository ativoRepository;
    private final AtivoDetalheHardwareRepository detalheHardwareRepository;
    private final DiscoRepository discoRepository;
    private final MemoriaRepository memoriaRepository;
    private final AdaptadorRedeRepository adaptadorRedeRepository;
    private final HealthCheckMapper healthCheckMapper;

    public HealthCheckService(AtivoRepository ativoRepository, AtivoDetalheHardwareRepository detalheHardwareRepository, DiscoRepository discoRepository, MemoriaRepository memoriaRepository, AdaptadorRedeRepository adaptadorRedeRepository, HealthCheckMapper healthCheckMapper) {
        this.ativoRepository = ativoRepository;
        this.detalheHardwareRepository = detalheHardwareRepository;
        this.discoRepository = discoRepository;
        this.memoriaRepository = memoriaRepository;
        this.adaptadorRedeRepository = adaptadorRedeRepository;
        this.healthCheckMapper = healthCheckMapper;
    }

    @Transactional
    public void updateHealthCheck(Long ativoId, HealthCheckDTO healthCheckDTO) {
        Usuario usuarioLogado = getUsuarioLogado(); // CORREÇÃO
        Ativo ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + ativoId));

        // --- Validação de Segurança ---
        if (!isAdmin(usuarioLogado)) { // CORREÇÃO
            Funcionario funcionarioLogado = usuarioLogado.getFuncionario();
            if (funcionarioLogado == null || funcionarioLogado.getFiliais().stream().noneMatch(f -> f.getId().equals(ativo.getFilial().getId()))) {
                throw new AccessDeniedException("Você não tem permissão para atualizar o health check de ativos desta filial.");
            }
        }

        // --- Lógica de Atualização ---
        AtivoDetalheHardware detalhes = detalheHardwareRepository.findById(ativoId)
                .orElseGet(() -> {
                    AtivoDetalheHardware novoDetalhe = new AtivoDetalheHardware();
                    novoDetalhe.setAtivo(ativo);
                    return novoDetalhe;
                });

        // Atualiza os campos principais
        healthCheckMapper.updateEntityFromDto(detalhes, healthCheckDTO);
        detalheHardwareRepository.save(detalhes);

        // Limpa e recria os componentes (Discos, Memórias, etc.)
        discoRepository.deleteByAtivoDetalheHardwareId(detalhes.getId());
        memoriaRepository.deleteByAtivoDetalheHardwareId(detalhes.getId());
        adaptadorRedeRepository.deleteByAtivoDetalheHardwareId(detalhes.getId());

        // Salva os novos componentes
        if (healthCheckDTO.discos() != null) {
            List<Disco> discos = healthCheckDTO.discos().stream().map(healthCheckMapper::toEntity).collect(Collectors.toList());
            discos.forEach(d -> d.setAtivoDetalheHardware(detalhes));
            discoRepository.saveAll(discos);
        }

        if (healthCheckDTO.memorias() != null) {
            List<Memoria> memorias = healthCheckDTO.memorias().stream().map(healthCheckMapper::toEntity).collect(Collectors.toList());
            memorias.forEach(m -> m.setAtivoDetalheHardware(detalhes));
            memoriaRepository.saveAll(memorias);
        }

        if (healthCheckDTO.adaptadoresRede() != null) {
            List<AdaptadorRede> adaptadores = healthCheckDTO.adaptadoresRede().stream().map(healthCheckMapper::toEntity).collect(Collectors.toList());
            adaptadores.forEach(a -> a.setAtivoDetalheHardware(detalhes));
            adaptadorRedeRepository.saveAll(adaptadores);
        }
    }

    // CORREÇÃO: Métodos auxiliares atualizados para usar a entidade Usuario
    private Usuario getUsuarioLogado() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUsuario();
    }

    private boolean isAdmin(Usuario usuario) {
        return "ROLE_ADMIN".equals(usuario.getRole());
    }
}
