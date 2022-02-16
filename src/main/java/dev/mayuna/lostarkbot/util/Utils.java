package dev.mayuna.lostarkbot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.mayuna.lostarkbot.objects.ServerWidget;
import dev.mayuna.mayusjdautils.managed.ManagedMessage;

public class Utils {

    public static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(ManagedMessage.class, new ServerWidget()) // TODO: ManagedMessage adapter
                .create();
    }

}
