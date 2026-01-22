package br.com.aegispatrimonio.audit;

import br.com.aegispatrimonio.context.TenantContext;
import br.com.aegispatrimonio.security.CustomUserDetails;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) revisionEntity;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            customRevisionEntity.setUsername(userDetails.getUsername());
        } else if (authentication != null) {
            customRevisionEntity.setUsername(authentication.getName());
        } else {
            customRevisionEntity.setUsername("SYSTEM");
        }

        Long filialId = TenantContext.getFilialId();
        if (filialId != null) {
            customRevisionEntity.setFilialId(filialId);
        }
    }
}
