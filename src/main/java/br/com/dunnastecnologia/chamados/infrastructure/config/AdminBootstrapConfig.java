package br.com.dunnastecnologia.chamados.infrastructure.config;

import br.com.dunnastecnologia.chamados.domain.model.Administrador;
import br.com.dunnastecnologia.chamados.domain.model.StatusChamado;
import br.com.dunnastecnologia.chamados.infrastructure.repository.StatusChamadoRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrapConfig {

    private static final String STATUS_SOLICITADO = "Solicitado";
    private static final String STATUS_FINALIZADO = "Finalizado";
    private static final String STATUS_ATRASADO = "Atrasado";

    @Bean
    public CommandLineRunner createDefaultAdministrator(
            UsuarioRepository usuarioRepository,
            StatusChamadoRepository statusChamadoRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.enabled:true}") boolean adminBootstrapEnabled,
            @Value("${app.bootstrap.admin.nome:Administrador}") String adminNome,
            @Value("${app.bootstrap.admin.email:admin@condominio.local}") String adminEmail,
            @Value("${app.bootstrap.admin.senha:admin123}") String adminSenha
    ) {
        return args -> {
            ensureDefaultStatuses(statusChamadoRepository);

            if (!adminBootstrapEnabled || usuarioRepository.existsAdministrador()) {
                return;
            }

            var usuarioExistente = usuarioRepository.findByEmail(adminEmail).orElse(null);
            if (usuarioExistente != null) {
                if (usuarioExistente instanceof Administrador administrador) {
                    administrador.setNome(adminNome);
                    administrador.setSenha(passwordEncoder.encode(adminSenha));
                    administrador.setAtivo(Boolean.TRUE);
                    usuarioRepository.save(administrador);
                }
                return;
            }

            Administrador administrador = new Administrador();
            administrador.setNome(adminNome);
            administrador.setEmail(adminEmail);
            administrador.setSenha(passwordEncoder.encode(adminSenha));
            usuarioRepository.save(administrador);
        };
    }

    private void ensureDefaultStatuses(StatusChamadoRepository statusChamadoRepository) {
        StatusChamado solicitado = statusChamadoRepository.findByNome(STATUS_SOLICITADO)
                .orElseGet(() -> {
                    StatusChamado status = new StatusChamado();
                    status.setNome(STATUS_SOLICITADO);
                    status.setInicialPadrao(Boolean.FALSE);
                    return statusChamadoRepository.save(status);
                });

        statusChamadoRepository.findByNome(STATUS_FINALIZADO)
                .orElseGet(() -> {
                    StatusChamado status = new StatusChamado();
                    status.setNome(STATUS_FINALIZADO);
                    status.setInicialPadrao(Boolean.FALSE);
                    return statusChamadoRepository.save(status);
                });

        statusChamadoRepository.findByNome(STATUS_ATRASADO)
                .orElseGet(() -> {
                    StatusChamado status = new StatusChamado();
                    status.setNome(STATUS_ATRASADO);
                    status.setInicialPadrao(Boolean.FALSE);
                    return statusChamadoRepository.save(status);
                });

        statusChamadoRepository.findByInicialPadraoTrue()
                .filter(statusAtual -> !statusAtual.getId().equals(solicitado.getId()))
                .ifPresent(statusAtual -> {
                    statusAtual.setInicialPadrao(Boolean.FALSE);
                    statusChamadoRepository.save(statusAtual);
                });

        solicitado.setInicialPadrao(Boolean.TRUE);
        statusChamadoRepository.save(solicitado);
    }
}
