package dev.mayuna.lostarkbot.old.api.misc;

import lombok.Getter;

public class HttpError {

    private final @Getter int code;
    private final @Getter Exception exception;

    public HttpError(int code, Exception exception) {
        this.code = code;
        this.exception = exception;
    }

    public boolean isSuccessful() {
        return String.valueOf(code).startsWith("2");
    }
}
