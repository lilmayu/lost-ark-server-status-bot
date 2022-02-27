package dev.mayuna.lostarkbot.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    public static List<String> toStringList(JsonArray jsonArray) {
        List<String> stringList = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            stringList.add(jsonElement.getAsString());
        }

        return stringList;
    }

    public static JsonArray toStringJsonArray(List<String> stringList) {
        JsonArray jsonArray = new JsonArray();

        for (String string : stringList) {
            jsonArray.add(string);
        }

        return jsonArray;
    }
}