package dev.mayuna.lostarkbot.listeners;

import dev.mayuna.lostarkbot.Main;
import net.dv8tion.jda.api.events.GatewayPingEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

public class ShardWatcher implements EventListener {

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof GatewayPingEvent gatewayPingEvent) {
            Main.getMayuShardManager().onGatewayPing(gatewayPingEvent);
        }
    }
}
