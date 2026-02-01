package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.exception.ResourceConflictException;
import br.com.aegispatrimonio.model.Manutencao;
import br.com.aegispatrimonio.model.StatusManutencao;
import br.com.aegispatrimonio.model.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowAprovacaoService {

    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public void aprovar(Manutencao manutencao) {
        validarTransicao(manutencao, StatusManutencao.SOLICITADA, StatusManutencao.APROVADA);
        manutencao.setStatus(StatusManutencao.APROVADA);
        logAudit("aprovou", manutencao);
    }

    @Transactional
    public void iniciar(Manutencao manutencao) {
        validarTransicao(manutencao, StatusManutencao.APROVADA, StatusManutencao.EM_ANDAMENTO);
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        manutencao.setDataInicio(LocalDate.now());
        logAudit("iniciou", manutencao);
    }

    @Transactional
    public void concluir(Manutencao manutencao) {
        validarTransicao(manutencao, StatusManutencao.EM_ANDAMENTO, StatusManutencao.CONCLUIDA);
        manutencao.setStatus(StatusManutencao.CONCLUIDA);
        manutencao.setDataConclusao(LocalDate.now());
        logAudit("concluiu", manutencao);
    }

    @Transactional
    public void cancelar(Manutencao manutencao) {
        if (manutencao.getStatus() == StatusManutencao.CONCLUIDA || manutencao.getStatus() == StatusManutencao.CANCELADA) {
            throw new ResourceConflictException("Manutenção já foi concluída ou cancelada e não pode ser alterada.");
        }
        manutencao.setStatus(StatusManutencao.CANCELADA);
        logAudit("cancelou", manutencao);
    }

    private void validarTransicao(Manutencao manutencao, StatusManutencao statusAtual, StatusManutencao novoStatus) {
        if (manutencao.getStatus() != statusAtual) {
            throw new ResourceConflictException(
                    String.format("Transição inválida: Não é possível mudar de '%s' para '%s'.",
                            manutencao.getStatus(), novoStatus)
            );
        }
    }

    private void logAudit(String acao, Manutencao manutencao) {
        Usuario auditor = currentUserProvider.getCurrentUsuario();
        log.info("AUDIT: Usuário {} {} a manutenção com ID {} para o ativo {}.",
                auditor.getEmail(), acao, manutencao.getId(), manutencao.getAtivo().getId());
    }
}
