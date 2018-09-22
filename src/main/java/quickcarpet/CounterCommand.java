package quickcarpet;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.text.TextComponentString;

public class CounterCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> root = Commands.literal("counter");
        root = addSubcommands(root, null);
        for (EnumDyeColor color : EnumDyeColor.values()) {
            LiteralArgumentBuilder<CommandSource> colored = Commands.literal(color.getName());
            colored = addSubcommands(colored, color);
            root = root.then(colored);
        }
        dispatcher.register(root);
    }
    
    private static LiteralArgumentBuilder<CommandSource> addSubcommands(LiteralArgumentBuilder<CommandSource> node, EnumDyeColor color) {
        return node
                .executes(cmd -> queryHopperStats(cmd.getSource(), false, color))
                .then(Commands.literal("realtime")
                        .executes(cmd -> queryHopperStats(cmd.getSource(), true, color)))
                .then(Commands.literal("reset")
                        .executes(cmd -> resetHopperCounter(cmd.getSource(), color)));
    }
    
    private static int queryHopperStats(CommandSource source, boolean realtime, EnumDyeColor color) {
        if (color == null)
            HopperCounter.query_hopper_all_stats(source.getServer(), realtime).forEach(msg -> source.sendFeedback(msg, false));
        else
            HopperCounter.query_hopper_stats_for_color(source.getServer(), color, realtime, false).forEach(msg -> source.sendFeedback(msg, false));
        return 0;
    }
    
    private static int resetHopperCounter(CommandSource source, EnumDyeColor color) {
        HopperCounter.reset_hopper_counter(source.getWorld(), color);
        source.sendFeedback(new TextComponentString(String.format("%s counters restarted", color == null ? "All" : color)), true);
        return 0;
    }
    
}
