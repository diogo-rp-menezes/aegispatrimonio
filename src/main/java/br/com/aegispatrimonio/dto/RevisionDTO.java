package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.audit.CustomRevisionEntity;
import org.hibernate.envers.RevisionType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record RevisionDTO(
    Number id,
    LocalDateTime revisionDate,
    String username,
    String revisionType
) {
    public static RevisionDTO from(CustomRevisionEntity revisionEntity, RevisionType revisionType) {
        LocalDateTime date = Instant.ofEpochMilli(revisionEntity.getTimestamp())
                                    .atZone(ZoneId.of("UTC"))
                                    .toLocalDateTime();
        return new RevisionDTO(
            revisionEntity.getId(),
            date,
            revisionEntity.getUsername(),
            revisionType.name()
        );
    }
}
