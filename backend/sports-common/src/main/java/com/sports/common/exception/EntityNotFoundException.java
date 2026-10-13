package com.sports.common.exception;

public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String entityName, String identifier) {
        super(String.format("%s with identifier '%s' was not found.", entityName, identifier));
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
