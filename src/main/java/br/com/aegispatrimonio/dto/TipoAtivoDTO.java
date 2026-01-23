package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.CategoriaContabil;

public record TipoAtivoDTO(Long id, String nome, CategoriaContabil categoriaContabil, String esquemaAtributos) {
    public TipoAtivoDTO(Long id, String nome, CategoriaContabil categoriaContabil) {
        this(id, nome, categoriaContabil, null);
    }
}
