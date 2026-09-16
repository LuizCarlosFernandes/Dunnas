package br.com.dunnastecnologia.chamados.infrastructure.service.support;

import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;

public final class InputValidationSupport {

    private InputValidationSupport() {
    }

    public static String normalizeRequiredText(
            String value,
            String requiredMessage,
            String maxLengthMessage,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(requiredMessage);
        }

        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new BusinessRuleException(maxLengthMessage);
        }

        return normalized;
    }

    public static void validateMaxBytes(long value, long maxBytes, String maxBytesMessage) {
        if (value > maxBytes) {
            throw new BusinessRuleException(maxBytesMessage);
        }
    }
}
