package dev.mayuna.lostarkbot.objects.other;

import com.google.gson.annotations.Expose;
import dev.mayuna.lostarkbot.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class StatusWhitelistObject {

    private @Getter @Expose Type type;
    private @Getter @Setter @Expose String status; // Possible values: GOOD, BUSY, FULL, MAINTENANCE, OFFLINE

    public StatusWhitelistObject(Type type, String status) {
        this.type = type;
        this.status = status;
    }

    public enum Type {
        FROM,
        TO;

        public static Type get(String string) {
            for (Type type : values())
                if (type.name().equalsIgnoreCase(string))
                    return type;

            return null;
        }
    }

    @Override
    public String toString() {
        String prettyStatus = status;

        if (prettyStatus.equals("GOOD")) {
            prettyStatus = "ONLINE";
        }

        return Utils.prettyString(prettyStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof StatusWhitelistObject))
            return false;
        StatusWhitelistObject that = (StatusWhitelistObject) o;
        return Objects.equals(status, that.status) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, type);
    }
}
