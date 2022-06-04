package dev.mayuna.lostarkbot.old.api.misc;

import com.google.gson.JsonObject;

public interface ApiRequest {

    /**
     * Endpoint's URL after "https://api.hetzner.cloud/v1"
     *
     * @return Endpoint's URL
     */
    String getURL();

    /**
     * HTTP Request method of Endpoint
     *
     * @return {@link HttpRequestType}
     */
    HttpRequestType getRequestMethod();

    PathParameter[] getPathParameters();

    JsonObject getRequestBody();
}
