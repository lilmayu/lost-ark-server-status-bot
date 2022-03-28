package dev.mayuna.lostarkbot.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.mayuna.lostarkbot.api.exceptions.ApiException;
import dev.mayuna.lostarkbot.api.exceptions.HttpException;
import dev.mayuna.lostarkbot.api.exceptions.MissingPathParametersException;
import dev.mayuna.lostarkbot.api.misc.*;
import dev.mayuna.lostarkbot.api.unofficial.UnofficialLostArkApi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ApiRestAction<T extends ApiResponse> {

    private final Class<T> clazz;
    private final ApiRequest apiRequest;
    private Api api;

    private Consumer<HttpError> httpErrorCallback = httpError -> {
        throw new HttpException(httpError);
    };
    private Consumer<ApiError> apiErrorCallback = apiError -> {
        throw new ApiException(apiError);
    };
    private BiConsumer<JsonObject, T> successCallback = (responseBody, object) -> {};

    public ApiRestAction(Class<T> clazz, ApiRequest apiRequest, Api api) {
        this.clazz = clazz;
        this.apiRequest = apiRequest;
        this.api = api;
    }

    public ApiRestAction<T> onHttpError(Consumer<HttpError> httpErrorConsumer) {
        this.httpErrorCallback = httpErrorConsumer;
        return this;
    }

    public ApiRestAction<T> onApiError(Consumer<ApiError> apiErrorConsumer) {
        this.apiErrorCallback = apiErrorConsumer;
        return this;
    }

    public ApiRestAction<T> onSuccess(BiConsumer<JsonObject, T> successConsumer) {
        this.successCallback = successConsumer;
        return this;
    }

    /**
     * TODO: Java doc
     *
     * @return Responded object or true if it was DELETE request without response body (204) and was successful. If request failed, returns null.
     */
    public T execute() {
        String stringUrl = api.getApiEndpoint() + apiRequest.getURL();

        if (apiRequest.getPathParameters() != null && apiRequest.getPathParameters().length != 0) {
            for (PathParameter pathParameter : apiRequest.getPathParameters()) {
                stringUrl = stringUrl.replace("{" + pathParameter.getParameter() + "}", pathParameter.getReplacement());
            }
        }

        if (stringUrl.contains("{") || stringUrl.contains("}")) {
            throw new MissingPathParametersException(stringUrl, apiRequest);
        }

        URL url;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Exception occurred while creating URL object", exception);
        }

        int httpResponseCode = -1;
        JsonObject responseBody;

        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                    .uri(url.toURI());

            if (api.getToken() != null) {
                httpRequestBuilder.header("Authorization", "Bearer " + api.getToken());
            }

            switch (apiRequest.getRequestMethod()) {
                case GET:
                    httpRequestBuilder.GET();
                    break;
                case POST:
                    httpRequestBuilder.setHeader("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(apiRequest.getRequestBody().toString()));
                    break;
                case PUT:
                    httpRequestBuilder.setHeader("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.ofString(apiRequest.getRequestBody().toString()));
                    break;
                case DELETE:
                    httpRequestBuilder.DELETE();
                    break;
            }

            HttpResponse<String> httpResponse = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            httpResponseCode = httpResponse.statusCode();

            if (apiRequest.getRequestMethod() == HttpRequestType.DELETE && httpResponseCode == 204) {
                T deleteResponse = (T) new DeleteResponse(httpResponseCode);

                successCallback.accept(null, deleteResponse);
                return deleteResponse;
            }

            if (httpResponse.body().equals("Internal Server Error")) {
                apiErrorCallback.accept(new InternalServerError());
                return null;
            }

            responseBody = JsonParser.parseString(httpResponse.body()).getAsJsonObject();

            if (api instanceof UnofficialLostArkApi) {
                Gson gson = new Gson();

                if (responseBody.has("detail")) {
                    apiErrorCallback.accept(gson.fromJson(responseBody, ApiError.class));
                    return null;
                }

                ApiResponse apiResponse = new Gson().fromJson(responseBody, ApiResponse.class);
                T t;

                if (responseBody.get("data").isJsonArray()) {
                    t = gson.fromJson(responseBody, clazz);
                } else {
                    t = gson.fromJson(responseBody.getAsJsonObject("data"), clazz);
                }

                t.setApiResponse(apiResponse);
                t.setApi(api);
                successCallback.accept(responseBody, t);
                return t;
            }

            if (responseBody.has("error")) {
                apiErrorCallback.accept(new Gson().fromJson(responseBody, ApiError.class));
                return null;
            }
        } catch (IOException | URISyntaxException | InterruptedException exception) {
            httpErrorCallback.accept(new HttpError(httpResponseCode, exception));
            return null;
        }

        return null;
    }

    public ApiRestAction<T> setApi(Api api) {
        this.api = api;
        return this;
    }
}
