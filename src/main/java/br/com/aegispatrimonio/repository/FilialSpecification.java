package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Filial;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class FilialSpecification {

    public static Specification<Filial> build(String nome, String codigo) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (nome != null && !nome.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
            }

            if (codigo != null && !codigo.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("codigo"), codigo));
            }

            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
