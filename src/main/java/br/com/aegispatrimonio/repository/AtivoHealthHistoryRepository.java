package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.AtivoHealthHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtivoHealthHistoryRepository extends JpaRepository<AtivoHealthHistory, Long> {

    List<AtivoHealthHistory> findByAtivoIdAndMetricaOrderByDataRegistroAsc(Long ativoId, String metrica);

    List<AtivoHealthHistory> findByAtivoIdAndComponenteInAndMetricaOrderByDataRegistroAsc(Long ativoId, List<String> componentes, String metrica);
}
