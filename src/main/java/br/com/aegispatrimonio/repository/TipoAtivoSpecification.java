package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.TipoAtivo;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class TipoAtivoSpecification {

    public static Specification<TipoAtivo> build(String nome, String categoriaContabil) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (nome != null && !nome.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
            }

            if (categoriaContabil != null && !categoriaContabil.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("categoriaContabil")), "%" + categoriaContabil.toLowerCase() + "%"));
            }

            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
