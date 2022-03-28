package dev.mayuna.lostarkbot.api.exceptions;

import dev.mayuna.lostarkbot.api.misc.HttpError;
import lombok.Getter;

public class HttpException extends RuntimeException {

    private final @Getter HttpError httpError;

    public HttpException(HttpError httpError) {
        super("During requesting, HTTP Error occurred! Code: " + httpError.getCode(), httpError.getException());
        this.httpError = httpError;
    }
}
