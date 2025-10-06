package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Pessoa;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PessoaSpecification {

    public static Specification<Pessoa> build(String nome, Long departamentoId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (nome != null && !nome.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
            }

            if (departamentoId != null) {
                predicates.add(criteriaBuilder.equal(root.get("departamento").get("id"), departamentoId));
            }

            // Otimização N+1
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                 root.fetch("departamento", jakarta.persistence.criteria.JoinType.LEFT);
            }
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
