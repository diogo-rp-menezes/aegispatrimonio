package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Group;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByName(String name);

    @EntityGraph(attributePaths = {"permissions"})
    List<Group> findAll();
}
