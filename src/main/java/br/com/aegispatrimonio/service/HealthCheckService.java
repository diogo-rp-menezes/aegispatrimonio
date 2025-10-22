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
    private final FuncionarioRepository funcionarioRepository;

    public HealthCheckService(AtivoRepository ativoRepository, AtivoDetalheHardwareRepository detalheHardwareRepository, DiscoRepository discoRepository, MemoriaRepository memoriaRepository, AdaptadorRedeRepository adaptadorRedeRepository, HealthCheckMapper healthCheckMapper, FuncionarioRepository funcionarioRepository) {
        this.ativoRepository = ativoRepository;
        this.detalheHardwareRepository = detalheHardwareRepository;
        this.discoRepository = discoRepository;
        this.memoriaRepository = memoriaRepository;
        this.adaptadorRedeRepository = adaptadorRedeRepository;
        this.healthCheckMapper = healthCheckMapper;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional
    public void updateHealthCheck(Long ativoId, HealthCheckDTO healthCheckDTO) {
        Usuario usuarioLogado = getUsuarioLogado();
        Ativo ativo = ativoRepository.findByIdWithDetails(ativoId)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + ativoId));

        if (!isAdmin(usuarioLogado)) {
            Funcionario funcionarioPrincipal = usuarioLogado.getFuncionario();
            if (funcionarioPrincipal == null) {
                 throw new AccessDeniedException("Usuário não está associado a um funcionário.");
            }

            Funcionario funcionarioLogado = funcionarioRepository.findById(funcionarioPrincipal.getId())
                    .orElseThrow(() -> new AccessDeniedException("Funcionário associado ao usuário não foi encontrado no sistema."));

            if (funcionarioLogado.getFiliais().stream().noneMatch(f -> f.getId().equals(ativo.getFilial().getId()))) {
                throw new AccessDeniedException("Você não tem permissão para atualizar o health check de ativos desta filial.");
            }
        }

        AtivoDetalheHardware detalhes = detalheHardwareRepository.findById(ativoId)
                .orElseGet(() -> {
                    AtivoDetalheHardware novoDetalhe = new AtivoDetalheHardware();
                    novoDetalhe.setAtivo(ativo);
                    novoDetalhe.setId(ativo.getId());
                    // Não persistimos aqui para evitar dupla invocação de save() nos testes; a persistência ocorrerá após o mapeamento
                    return novoDetalhe;
                });

        // Se a entidade já existia, ou se foi recém-criada, agora 'detalhes' é a instância gerenciada com ID.
        // Aplicar as atualizações do DTO
        healthCheckMapper.updateEntityFromDto(detalhes, healthCheckDTO);
        // Salvar novamente para aplicar as atualizações (se houverem)
        detalheHardwareRepository.save(detalhes);
        detalheHardwareRepository.flush(); // Garante que as atualizações sejam persistidas

        final AtivoDetalheHardware finalDetalhes = detalhes;

        // Limpa e recria os componentes (Discos, Memórias, etc.)
        if (finalDetalhes.getId() != null) {
            discoRepository.deleteByAtivoDetalheHardwareId(finalDetalhes.getId());
            memoriaRepository.deleteByAtivoDetalheHardwareId(finalDetalhes.getId());
            adaptadorRedeRepository.deleteByAtivoDetalheHardwareId(finalDetalhes.getId());
        }

        if (healthCheckDTO.discos() != null) {
            List<Disco> discos = healthCheckDTO.discos().stream().map(healthCheckMapper::toEntity).collect(Collectors.toList());
            discos.forEach(d -> d.setAtivoDetalheHardware(finalDetalhes));
            discoRepository.saveAll(discos);
        }

        if (healthCheckDTO.memorias() != null) {
            List<Memoria> memorias = healthCheckDTO.memorias().stream().map(healthCheckMapper::toEntity).collect(Collectors.toList());
            memorias.forEach(m -> m.setAtivoDetalheHardware(finalDetalhes));
            memoriaRepository.saveAll(memorias);
        }

        if (healthCheckDTO.adaptadoresRede() != null) {
            List<AdaptadorRede> adaptadores = healthCheckDTO.adaptadoresRede().stream().map(healthCheckMapper::toEntity).collect(Collectors.toList());
            adaptadores.forEach(a -> a.setAtivoDetalheHardware(finalDetalhes));
            adaptadorRedeRepository.saveAll(adaptadores);
        }
    }

    private Usuario getUsuarioLogado() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUsuario();
    }

    private boolean isAdmin(Usuario usuario) {
        return "ROLE_ADMIN".equals(usuario.getRole());
    }
}
