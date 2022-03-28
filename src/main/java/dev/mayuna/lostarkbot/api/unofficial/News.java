package dev.mayuna.lostarkbot.api.unofficial;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.mayuna.lostarkbot.api.misc.ApiRequest;
import dev.mayuna.lostarkbot.api.misc.ApiResponse;
import dev.mayuna.lostarkbot.api.misc.HttpRequestType;
import dev.mayuna.lostarkbot.api.misc.PathParameter;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsObject;
import lombok.Getter;

import java.util.Arrays;

public class News extends ApiResponse {

    private @Getter @SerializedName("data") NewsObject[] newsObjects;

    public static class Request implements ApiRequest {

        private final @Getter NewsCategory newsCategory;

        public Request(NewsCategory newsCategory) {
            this.newsCategory = newsCategory;
        }

        @Override
        public String getURL() {
            return "/news/{category}";
        }

        @Override
        public HttpRequestType getRequestMethod() {
            return HttpRequestType.GET;
        }

        @Override
        public PathParameter[] getPathParameters() {
            return new PathParameter[]{
                    new PathParameter("category", newsCategory.getId())
            };
        }

        @Override
        public JsonObject getRequestBody() {
            return null;
        }
    }

    @Override
    public String toString() {
        return "News{" +
                "newsObjects=" + Arrays.toString(newsObjects) +
                '}';
    }
}
