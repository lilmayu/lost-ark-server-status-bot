package dev.mayuna.lostarkbot.old.api.misc;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class ApiError extends ApiResponse {

    protected @Getter @SerializedName("detail") String error;
}
