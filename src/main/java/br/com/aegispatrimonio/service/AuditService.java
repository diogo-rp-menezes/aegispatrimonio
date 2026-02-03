package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.audit.CustomRevisionEntity;
import br.com.aegispatrimonio.context.TenantContext;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.EntityRevisionDTO;
import br.com.aegispatrimonio.dto.RevisionDTO;
import br.com.aegispatrimonio.mapper.AtivoMapper;
import br.com.aegispatrimonio.model.Ativo;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final EntityManager entityManager;
    private final AtivoMapper ativoMapper;

    public AuditService(EntityManager entityManager, AtivoMapper ativoMapper) {
        this.entityManager = entityManager;
        this.ativoMapper = ativoMapper;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<EntityRevisionDTO<AtivoDTO>> getAtivoHistory(Long id) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        Long currentFilialId = TenantContext.getFilialId();

        AuditQuery query = reader.createQuery()
                .forRevisionsOfEntity(Ativo.class, false, true)
                .add(AuditEntity.id().eq(id));

        // Enforce Tenancy Isolation on the Audit Trail
        if (currentFilialId != null) {
            query.add(AuditEntity.revisionProperty("filialId").eq(currentFilialId));
        }

        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> {
                    Ativo entity = (Ativo) result[0];
                    CustomRevisionEntity revisionEntity = (CustomRevisionEntity) result[1];
                    RevisionType revisionType = (RevisionType) result[2];

                    AtivoDTO dto = ativoMapper.toDTO(entity);
                    RevisionDTO revisionDTO = RevisionDTO.from(revisionEntity, revisionType);

                    return new EntityRevisionDTO<>(dto, revisionDTO);
                })
                .collect(Collectors.toList());
    }
}
