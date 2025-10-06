package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Manutencao;
import br.com.aegispatrimonio.model.StatusManutencao;
import br.com.aegispatrimonio.model.TipoManutencao;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ManutencaoSpecification {

    public static Specification<Manutencao> build(Long ativoId, StatusManutencao status, TipoManutencao tipo,
                                                  Long solicitanteId, Long fornecedorId, LocalDate dataSolicitacaoInicio,
                                                  LocalDate dataSolicitacaoFim, LocalDate dataConclusaoInicio,
                                                  LocalDate dataConclusaoFim) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (ativoId != null) {
                predicates.add(criteriaBuilder.equal(root.get("ativo").get("id"), ativoId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (tipo != null) {
                predicates.add(criteriaBuilder.equal(root.get("tipo"), tipo));
            }
            if (solicitanteId != null) {
                predicates.add(criteriaBuilder.equal(root.get("solicitante").get("id"), solicitanteId));
            }
            if (fornecedorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("fornecedor").get("id"), fornecedorId));
            }
            if (dataSolicitacaoInicio != null && dataSolicitacaoFim != null) {
                predicates.add(criteriaBuilder.between(root.get("dataSolicitacao"), dataSolicitacaoInicio, dataSolicitacaoFim));
            }
            if (dataConclusaoInicio != null && dataConclusaoFim != null) {
                predicates.add(criteriaBuilder.between(root.get("dataConclusao"), dataConclusaoInicio, dataConclusaoFim));
            }

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("ativo", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("solicitante", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("tecnicoResponsavel", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("fornecedor", jakarta.persistence.criteria.JoinType.LEFT);
            }
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
