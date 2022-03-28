package dev.mayuna.lostarkbot.api.unofficial;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.mayuna.lostarkbot.api.misc.ApiRequest;
import dev.mayuna.lostarkbot.api.misc.ApiResponse;
import dev.mayuna.lostarkbot.api.misc.HttpRequestType;
import dev.mayuna.lostarkbot.api.misc.PathParameter;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsPostObject;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsObject;
import dev.mayuna.lostarkbot.util.logging.Logger;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Forums extends ApiResponse {

    private @Getter @SerializedName("data") ForumsPostObject[] forumsPostObjects;

    public void remove(Collection<ForumsPostObject> forumsPostObjects) {
        List<ForumsPostObject> list = new ArrayList<>(Arrays.stream(this.forumsPostObjects).toList());
        list.removeAll(forumsPostObjects);
        this.forumsPostObjects = list.toArray(new ForumsPostObject[0]);
    }

    public static class Request implements ApiRequest {

        private final @Getter ForumsCategory forumsCategory;

        public Request(ForumsCategory forumsCategory) {
            this.forumsCategory = forumsCategory;
        }

        @Override
        public String getURL() {
            return "/v2/forums/{category}";
        }

        @Override
        public HttpRequestType getRequestMethod() {
            return HttpRequestType.GET;
        }

        @Override
        public PathParameter[] getPathParameters() {
            return new PathParameter[]{
                    new PathParameter("category", forumsCategory.getId())
            };
        }

        @Override
        public JsonObject getRequestBody() {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Forums{" +
                "forumsPostObjects=" + Arrays.toString(forumsPostObjects) +
                '}';
    }
}
