package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.model.Usuario;

public interface CurrentUserProvider {
    Usuario getCurrentUsuario();
}
