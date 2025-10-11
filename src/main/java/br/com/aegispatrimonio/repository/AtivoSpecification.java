package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusAtivo;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class AtivoSpecification {

    public static Specification<Ativo> build(String nome, Long tipoAtivoId, Long localizacaoId, StatusAtivo status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (nome != null && !nome.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
            }

            if (tipoAtivoId != null) {
                predicates.add(criteriaBuilder.equal(root.get("tipoAtivo").get("id"), tipoAtivoId));
            }

            if (localizacaoId != null) {
                predicates.add(criteriaBuilder.equal(root.get("localizacao").get("id"), localizacaoId));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Otimização N+1: Evita queries separadas para buscar entidades relacionadas.
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                 root.fetch("tipoAtivo", jakarta.persistence.criteria.JoinType.LEFT);
                 root.fetch("localizacao", jakarta.persistence.criteria.JoinType.LEFT);
                 // CORREÇÃO: Renomeado para corresponder à entidade Ativo
                 root.fetch("funcionarioResponsavel", jakarta.persistence.criteria.JoinType.LEFT);
                 root.fetch("fornecedor", jakarta.persistence.criteria.JoinType.LEFT);
            }
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
