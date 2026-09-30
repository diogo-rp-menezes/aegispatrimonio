package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    List<RevokedToken> findByUsuarioId(Long usuarioId);

    @Modifying
    @Query("DELETE FROM RevokedToken r WHERE r.expiresAt <= :now")
    int deleteExpired(@Param("now") long now);
}
