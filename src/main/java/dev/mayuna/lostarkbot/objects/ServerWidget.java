package dev.mayuna.lostarkbot.objects;

import dev.mayuna.mayusjdautils.managed.ManagedMessage;
import lombok.Getter;

public class ServerWidget {

    private @Getter ManagedMessage managedMessage;

    public ServerWidget() {

    }

    public ServerWidget(ManagedMessage managedMessage) {
        this.managedMessage = managedMessage;
    }

    public String getName() {
        return managedMessage.getName();
    }
}
