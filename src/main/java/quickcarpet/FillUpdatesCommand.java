package quickcarpet;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;

import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TextComponentString;

public class FillUpdatesCommand {

    public static boolean fillUpdates = true;
    public static boolean updatesEnabled = true;
    
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("fill")
                .then(literal("updates")
                    .executes(ctx -> getFillUpdates(ctx.getSource()))
                    .then(argument("fillUpdates", BoolArgumentType.bool())
                        .executes(ctx -> setFillUpdates(ctx.getSource(), BoolArgumentType.getBool(ctx, "fillUpdates"))))));
    }
    
    public static int getFillUpdates(CommandSource source) {
        source.sendFeedback(new TextComponentString("Fill updates: " + fillUpdates), false);
        return fillUpdates ? 1 : 0;
    }
    
    public static int setFillUpdates(CommandSource source, boolean newFillUpdates) {
        fillUpdates = newFillUpdates;
        source.sendFeedback(new TextComponentString("Fill updates set to " + fillUpdates), true);
        return 0;
    }
    
}
