package dev.mayuna.lostarkbot.api.misc;

public class InternalServerError extends ApiError {

    public InternalServerError() {
        this.error = "Internal Server Error";
    }
}
