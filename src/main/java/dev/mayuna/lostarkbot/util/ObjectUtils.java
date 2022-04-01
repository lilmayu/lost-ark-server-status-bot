package dev.mayuna.lostarkbot.util;

public class ObjectUtils {


    public static boolean allNotNull(Object... objects) {
        if (objects == null) {
            return false;
        }

        for (Object object : objects) {
            if (object == null) {
                return false;
            }
        }

        return true;
    }

    public static boolean anyNull(Object... objects) {
        return !allNotNull(objects);
    }
}
