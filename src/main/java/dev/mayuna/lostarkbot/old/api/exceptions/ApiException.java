package dev.mayuna.lostarkbot.old.api.exceptions;

import dev.mayuna.lostarkbot.old.api.misc.ApiError;
import lombok.Getter;

public class ApiException extends RuntimeException {

    private final @Getter ApiError apiError;

    public ApiException(ApiError apiError) {
        super("CraftAPI responded with error! Code: " + apiError.getStatus() + "; Message: " + apiError.getError());
        this.apiError = apiError;
    }
}
