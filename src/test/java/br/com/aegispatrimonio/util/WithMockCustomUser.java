package br.com.aegispatrimonio.util;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    long id() default 1L;

    String email() default "test@aegis.com";

    String role() default "ROLE_USER";

    long funcionarioId() default 0L; // 0 ou -1 para indicar sem funcionário
}
