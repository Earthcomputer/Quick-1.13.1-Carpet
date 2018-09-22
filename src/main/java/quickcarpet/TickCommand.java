package quickcarpet;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextComponentString;

public class TickCommand {

    private static final SimpleCommandExceptionType NOT_WARPING_EXCEPTION = new SimpleCommandExceptionType(new TextComponentString("Not warping"));
    private static final SimpleCommandExceptionType ALREADY_WARPING_EXCEPTION = new SimpleCommandExceptionType(new TextComponentString("Already warping"));
    
    public static float tickRate = 20;
    public static long mspt = 50;
    
    public static CommandSource tickWarpSender;
    public static int scheduledTicksToWarp = 0;
    public static int ticksWarped = 0;
    public static long tickWarpStart;
    
    public static void setTickRate(float newRate) {
        tickRate = newRate;
        mspt = (long) (1000 / tickRate);
    }
    
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("tick")
                .then(literal("rate")
                    .executes(ctx -> getTickRate(ctx.getSource()))
                    .then(argument("newRate", FloatArgumentType.floatArg(0, 1000))
                        .executes(ctx -> setTickRate(ctx.getSource(), FloatArgumentType.getFloat(ctx, "newRate")))))
                .then(literal("warp")
                    .executes(ctx -> tickWarp(ctx.getSource(), 0))
                    .then(argument("scheduledTicks", IntegerArgumentType.integer(0))
                        .executes(ctx -> tickWarp(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "scheduledTicks"))))));
    }
    
    public static int getTickRate(CommandSource source) {
        source.sendFeedback(new TextComponentString("Tick rate: " + tickRate), false);
        return Math.round(tickRate);
    }
    
    public static int setTickRate(CommandSource source, float newRate) {
        if (newRate == 0)
            newRate = 20;
        setTickRate(newRate);
        source.sendFeedback(new TextComponentString("Tick rate set to " + newRate), true);
        return 0;
    }
    
    public static int tickWarp(CommandSource source, int scheduledTicks) throws CommandSyntaxException {
        if (scheduledTicksToWarp != 0 && scheduledTicks != 0) {
            throw ALREADY_WARPING_EXCEPTION.create();
        } else if (scheduledTicksToWarp == 0 && scheduledTicks == 0) {
            throw NOT_WARPING_EXCEPTION.create();
        }
        if (scheduledTicks == 0) {
            printCompleteMessage();
        } else {
            tickWarpSender = source;
        }
        scheduledTicksToWarp = scheduledTicks;
        ticksWarped = 0;
        tickWarpStart = Util.nanoTime();
        if (scheduledTicks == 0) {
            source.sendFeedback(new TextComponentString("Warp interrupted"), true);
        } else {
            source.sendFeedback(new TextComponentString("Warp speed..."), true);
        }
        return 0;
    }
    
    public static boolean continueTickWarp() {
        if (ticksWarped >= scheduledTicksToWarp) {
            printCompleteMessage();
            scheduledTicksToWarp = 0;
            return false;
        } else {
            ticksWarped++;
            return true;
        }
    }
    
    private static void printCompleteMessage() {
        long endTime = Util.nanoTime();
        long mspt = (endTime - tickWarpStart) / 1000000 / ticksWarped;
        tickWarpSender.sendFeedback(new TextComponentString("... Finished warp of " + ticksWarped + " ticks with " + mspt + " MSPT"), true);
        tickWarpSender = null;
    }
    
}
