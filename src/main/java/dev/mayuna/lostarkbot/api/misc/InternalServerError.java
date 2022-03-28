package dev.mayuna.lostarkbot.api.misc;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class InternalServerError extends ApiError {

    public InternalServerError() {
        this.error = "Internal Server Error";
    }
}
