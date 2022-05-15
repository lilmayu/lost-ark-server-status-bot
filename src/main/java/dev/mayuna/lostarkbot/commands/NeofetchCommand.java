package dev.mayuna.lostarkbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.sun.management.OperatingSystemMXBean;
import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.lostarkbot.util.SpecialRateLimiter;
import dev.mayuna.lostarkbot.util.Utils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NeofetchCommand extends SlashCommand {

    public static final String ANSI_LOGO_AND_BODY =
            """
                       [0m
                       [0m[32m..,,,*,,,,,..[33m....         ...[31m...,,,[35m,*,,,.     [35m{user}[0m@[35mMayu's Lost Ark Bot[0m
                       [32m .,@@@@@@@@@@@[33m@@@@/,,,,,,/#@@[31m@@@@@@[35m@@@@@@,.   {dashes}
                       [32m .,,%@@%**,,,[33m,,,*%@@&**(@@@*,,[31m,,,,**[35m/@@@*,.   [35mVersion[0m: {bot_version}
                       [32m   .,,@,,..[33m.  .,,*%@@//@@@**,.[31m.  ..,,[35m@(,.     [35mPlatform[0m: {java_implementation} Java {java_version}{java_idk}
                       [32m     ..%   [33m   .,,*@@@((@@@*,,.[31m      *[35m.        [35mHost[0m: Hetzner
                       [0m            [33m   .,@@@@((@@@@*,.                [35mUptime[0m: {uptime} (restart at 3:00 UTC+1)
                       [0m            [33m   .,*@@@((@@@*,,.                [35mCPU[0m: {cpu_usage}%
                       [0m            [33m   .,*&@@((@@@*,.                 [35mMemory[0m: {memory_used}MB / {memory_total}MB
                       [0m            [33m    .,%@@//&@@,,.                 [35mRate Limiter[0m: {requests_last} ~ {requests_current} requests
                       [0m            [33m    .,*@@//&@#,.                  [35mDiscord[0m: {shards} shards, {guilds} guilds
                       [0m            [33m    .,,@@//&@#,.
                       [0m            [33m     .,@@**&@,,.                  [30m███[41m[31m███[44m[32m███[33m███[34m███[0m[35m███[44m[36m███[37m███[0m
                       [0m            [33m     .,@@**&@,.                   [40m   [41m   [42m   [43m   [44m   [45m   [46m   [47m   [0m
                       [0m            [33m     .,#@**&@,.
                       [0m            [33m      .(@**&@,.
                       [0m            [33m      .,@,,&(.
                       [0m            [33m       .@,,&/.
                       [0m            [33m       .@..&.
                       [0m
                    """;
    public static final String ANSI_LOGO_NO_COLOR =
            """
                    ..,,,*,,,,,......         ......,,,,*,,,.
                     .,@@@@@@@@@@@@@@@/,,,,,,/#@@@@@@@@@@@@@@,.
                     .,,%@@%**,,,,,,*%@@&**(@@@*,,,,,,**/@@@*,.
                       .,,@,,...  .,,*%@@//@@@**,..  ..,,@(,. 
                         ..%      .,,*@@@((@@@*,,.      *.
                                   .,@@@@((@@@@*,.
                                   .,*@@@((@@@*,,.
                                   .,*&@@((@@@*,.
                                    .,%@@//&@@,,.
                                    .,*@@//&@#,.
                                    .,,@@//&@#,.
                                     .,@@**&@,,.
                                     .,@@**&@,.
                                     .,#@**&@,.
                                      .(@**&@,.
                                      .,@,,&(.
                                       .@,,&/.
                                       .@..&.
                    """;
    public static final String NEOFETCH_BODY_NO_COLOR =
            """
                    {user}@Mayu's Lost Ark Bot
                    {dashes}
                    Version: {bot_version}
                    Platform: {java_implementation} Java {java_version}, {java_idk}
                    Host: Hetzner
                    Uptime: {uptime} (restart at 3:00 UTC+1)
                    CPU: {cpu_usage}%
                    Memory: {memory_used}MB / {memory_total}MB
                    Rate Limiter: {requests_last} ~ {requests_current} requests
                    Discord: {shards} shards, {guilds} guilds
                                        
                    ████████████████████████
                    ████████████████████████""";

    private static @Getter Timer cpuWorker;
    private static @Getter double cpuUsage;

    public NeofetchCommand() {
        this.name = "neofetch";
        this.help = "Shows information about this bot in a aesthetic way.";

        List<OptionData> options = new ArrayList<>(1);
        options.add(new OptionData(OptionType.STRING, "phone", "Phone view", false).addChoice("True", "true"));
        this.options = options;

        cpuWorker = new Timer("CPU-Worker");
        cpuWorker.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
                int availableProcessors = operatingSystemMXBean.getAvailableProcessors();
                long prevUpTime = runtimeMXBean.getUptime();
                long prevProcessCpuTime = operatingSystemMXBean.getProcessCpuTime();

                try {
                    Thread.sleep(500);
                } catch (Exception ignored) {
                }

                operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                long upTime = runtimeMXBean.getUptime();
                long processCpuTime = operatingSystemMXBean.getProcessCpuTime();
                long elapsedCpu = processCpuTime - prevProcessCpuTime;
                long elapsedTime = upTime - prevUpTime;

                cpuUsage = Math.min(99F, elapsedCpu / (elapsedTime * 10000F * availableProcessors));
            }
        }, 0, 1);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (!Utils.makeEphemeral(event, true)) {
            return;
        }

        InteractionHook interactionHook = event.getHook();

        OptionMapping actionOption = event.getOption("phone");

        String content = "```ansi\n";

        if (actionOption == null) {
            content += ANSI_LOGO_AND_BODY;
        } else {
            content += ANSI_LOGO_NO_COLOR + "\n" + NEOFETCH_BODY_NO_COLOR;
        }

        content += "```";

        interactionHook.editOriginal(process(event.getUser(), content)).queue();
    }

    private String process(User user, String content) {
        String userReplacement = user.getName() + "#" + user.getDiscriminator();
        content = content.replace("{user}", userReplacement);

        String dashes = "";
        for (int i = 0; i < (userReplacement + "@Mayu's Lost Ark Bot").length(); i++) {
            dashes += "-";
        }

        content = content.replace("{dashes}", dashes)
                .replace("{bot_version}", Constants.VERSION)
                .replace("{java_version}", System.getProperty("java.version"))
                .replace("{java_implementation}", System.getProperty("java.runtime.name").substring(0, System.getProperty("java.runtime.name").indexOf(" ")))
                .replace("{java_idk}", System.getProperty("java.vendor.version") == null ? "" : ", " + System.getProperty("java.vendor.version"))
                .replace("{uptime}", Main.getUptime())
                .replace("{cpu_usage}", String.format("%.2f", cpuUsage))
                .replace("{memory_used}", String.format("%.1f", (Runtime.getRuntime().maxMemory() - Math.abs(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().maxMemory())) / (1024.0 * 1024.0)))
                .replace("{memory_total}", String.format("%.1f", Runtime.getRuntime().maxMemory() / (1024.0 * 1024.0)))
                .replace("{requests_last}", String.valueOf(SpecialRateLimiter.getLastRequestCount()))
                .replace("{requests_current}", String.valueOf(SpecialRateLimiter.getCurrentRequestCount()))
                .replace("{shards}", String.valueOf(Main.getMayuShardManager().get().getShardsTotal()))
                .replace("{guilds}", String.valueOf(Main.getMayuShardManager().get().getGuilds().size()));

        return content;
    }
}
