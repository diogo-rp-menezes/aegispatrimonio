package br.com.aegispatrimonio.security.oauth2;

import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import br.com.aegispatrimonio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    protected OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        // Different providers might have different attribute names for email
        String email = null;
        if (oAuth2User.getAttributes().containsKey("email")) {
            email = (String) oAuth2User.getAttributes().get("email");
        }

        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"), "Email not found from OAuth2 provider");
        }

        Optional<Usuario> userOptional = usuarioRepository.findByEmail(email);
        Usuario usuario;

        if (userOptional.isPresent()) {
            usuario = userOptional.get();
            usuario = updateExistingUser(usuario, oAuth2User);
        } else {
            usuario = registerNewUser(oAuth2UserRequest, oAuth2User);
        }

        return CustomUserDetails.create(usuario, oAuth2User.getAttributes());
    }

    private Usuario registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        Usuario usuario = new Usuario();

        String email = (String) oAuth2User.getAttributes().get("email");
        usuario.setEmail(email);
        usuario.setPassword(""); // Empty password for OAuth2 users (they shouldn't login via form with this)
        usuario.setRole("ROLE_USER"); // Default role
        usuario.setStatus(Status.ATIVO);

        return usuarioRepository.save(usuario);
    }

    private Usuario updateExistingUser(Usuario existingUser, OAuth2User oAuth2User) {
        // Here we could update name or picture if stored
        return usuarioRepository.save(existingUser);
    }
}
