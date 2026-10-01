package br.com.dunnastecnologia.chamados.infrastructure.controller.web;


import br.com.dunnastecnologia.chamados.infrastructure.controller.api.AdminReservaApiController;
import br.com.dunnastecnologia.chamados.infrastructure.controller.api.AreaComumApiController;
import br.com.dunnastecnologia.chamados.infrastructure.controller.api.MoradorReservaApiController;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.UnauthorizedOperationException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handler dedicado as features de Reserva e Area Comum.
 * Evita alterar o comprotamento dos controller de chamado já existentes nesse mesmo pacote.
 */

@ControllerAdvice(assignableTypes = {
        MoradorReservaWebController.class,
        AdminReservaWebController.class,
        MoradorReservaApiController.class,
        AdminReservaApiController.class,
        AdminAreaComumWebController.class,
        AreaComumApiController.class
})
@Hidden
public class ReservaExceptionHandler {

    private final WebControllerSupport support;

    public ReservaExceptionHandler(WebControllerSupport support) {
        this.support = support;
    }

    @ExceptionHandler({
            BusinessRuleException.class,
            ResourceNotFoundException.class,
            UnauthorizedOperationException.class,
            IllegalArgumentException.class
    })
    public String handleKnowExceptions(
            RuntimeException exception,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Authentication authentication
    ) {
        return redirectComMensagem(exception.getMessage(), request, redirectAttributes, authentication);
    }

    /**
     * Timeout ou impossibilidade de adquirir o lock pessimista da area durante aprovacao
     */

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public String handleConflitoDeLock(
            PessimisticLockingFailureException exception,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Authentication authentication
    ){
        String mensagem = "Outra decisão está em andaremo para esta área. Tente novamente em instantes.";
        return redirectComMensagem(mensagem, request, redirectAttributes, authentication);
    }

    private String redirectComMensagem(
            String mensagem,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        redirectAttributes.addFlashAttribute("errirMensage", mensagem);
        String referer = request.getHeader("Referer");
        if(referer != null && referer.isBlank()) {
            return "redirect:" + referer;
        }

        if(support.isAuthenticated(authentication)) {
            return "redirect:" + support.homePathForRole(support.authenticatedUser(authentication).role());
        }

        return "redirect:/login";
    }

    /**
     * Volta para a pagian de origem somente se o Referer for do mesmo host
     * evitando redirecionar o usuário para um endereço externo
     */
    private String sameOiriginRefererPath(HttpServletRequest request){
        String referer = request.getHeader("Referer");
        if(referer == null || referer.isBlank()){
            return null;
        }
        try {
            java.net.URI uri = java.net.URI.create(referer);

            if(uri.getHost() == null || uri.getHost().equalsIgnoreCase(request.getServerName())){
                return null;
            }
            String path = uri.getRawPath();
            if(path == null || path.isBlank()){
                return null;
            }

            return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
        } catch (IllegalArgumentException exception){
            return null;
        }
    }
}
