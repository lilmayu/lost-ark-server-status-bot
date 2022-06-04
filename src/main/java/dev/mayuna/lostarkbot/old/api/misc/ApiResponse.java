package dev.mayuna.lostarkbot.old.api.misc;

import com.google.gson.annotations.SerializedName;
import dev.mayuna.lostarkbot.old.api.Api;
import lombok.Getter;
import lombok.Setter;

public class ApiResponse {

    private @Getter @Setter Api api;

    private @Getter @SerializedName("status") int status = 0;

    public void setApiResponse(ApiResponse apiResponse) {
        this.status = apiResponse.status;
    }
}
