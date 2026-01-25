package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.TipoAlerta;
import java.time.LocalDateTime;

public record AlertaDTO(
    Long id,
    Long ativoId,
    String ativoNome,
    TipoAlerta tipo,
    String titulo,
    String mensagem,
    LocalDateTime dataCriacao,
    boolean lido,
    LocalDateTime dataLeitura
) {}
