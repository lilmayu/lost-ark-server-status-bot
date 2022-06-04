package dev.mayuna.lostarkbot.old.api.misc;

import lombok.Getter;

public class DeleteResponse extends ApiResponse {

    private final @Getter int httpResponseCode;

    public DeleteResponse(int httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }
}
