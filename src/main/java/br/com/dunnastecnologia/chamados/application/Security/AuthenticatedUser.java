package br.com.dunnastecnologia.chamados.application.Security;

import java.util.UUID;

public record AuthenticatedUser(UUID id,
                                String username,
                                String role) {
}
