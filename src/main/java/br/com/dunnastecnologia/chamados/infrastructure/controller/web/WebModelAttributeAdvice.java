package br.com.dunnastecnologia.chamados.infrastructure.controller.web;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackageClasses = {
        AuthWebController.class,
        HomeWebController.class,
        AdminWebController.class,
        MoradorWebController.class,
        ColaboradorWebController.class
})
@Hidden
public class WebModelAttributeAdvice {

    private final WebControllerSupport support;

    public WebModelAttributeAdvice(WebControllerSupport support) {
        this.support = support;
    }

    @ModelAttribute
    public void populateCommonAttributes(Model model, Authentication authentication) {
        model.addAttribute("appName", "Gerenciador de Chamados");

        if (!support.isAuthenticated(authentication)) {
            return;
        }

        AuthenticatedUser currentUser = support.authenticatedUser(authentication);
        model.addAttribute("currentUserId", currentUser.id());
        model.addAttribute("currentUserEmail", currentUser.username());
        model.addAttribute("currentUserRole", currentUser.role());
        model.addAttribute("currentUserRoleLabel", support.roleLabel(currentUser.role()));
        model.addAttribute("currentUserHome", support.homePathForRole(currentUser.role()));
        model.addAttribute("isAdministrador", "ROLE_ADMINISTRADOR".equals(currentUser.role()));
        model.addAttribute("isColaborador", "ROLE_COLABORADOR".equals(currentUser.role()));
        model.addAttribute("isMorador", "ROLE_MORADOR".equals(currentUser.role()));
    }
}
