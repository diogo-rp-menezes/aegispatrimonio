package br.com.aegispatrimonio.dto;

import java.util.List;

public record LoginResponseDTO(String token, List<FilialSimpleDTO> filiais, List<String> roles) {
}
