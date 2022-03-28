package dev.mayuna.lostarkbot.api.exceptions;

import dev.mayuna.lostarkbot.api.misc.ApiRequest;
import lombok.Getter;

public class MissingPathParametersException extends RuntimeException {

    private final @Getter String url;
    private final @Getter ApiRequest apiRequest;

    public MissingPathParametersException(String url, ApiRequest apiRequest) {
        super("Missing endpoint parameters: " + url);

        this.url = url;
        this.apiRequest = apiRequest;
    }
}
