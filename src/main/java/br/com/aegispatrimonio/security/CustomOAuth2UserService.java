package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    protected OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");

        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"), "Email not found from OAuth2 provider");
        }

        Optional<Usuario> userOptional = usuarioRepository.findByEmail(email);
        Usuario usuario;

        if (userOptional.isPresent()) {
            usuario = userOptional.get();
            // Here we could update user details if needed
            usuario = updateExistingUser(usuario, oAuth2User);
        } else {
            usuario = registerNewUser(oAuth2UserRequest, oAuth2User);
        }

        return oAuth2User;
    }

    private Usuario registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        Usuario usuario = new Usuario();
        usuario.setEmail(oAuth2User.getAttribute("email"));
        usuario.setPassword(UUID.randomUUID().toString()); // Placeholder password
        usuario.setStatus(Status.ATIVO);
        usuario.setRole("USER"); // Default role
        return usuarioRepository.save(usuario);
    }

    private Usuario updateExistingUser(Usuario existingUser, OAuth2User oAuth2User) {
        // Update name or image if we had those fields
        return usuarioRepository.save(existingUser);
    }
}
