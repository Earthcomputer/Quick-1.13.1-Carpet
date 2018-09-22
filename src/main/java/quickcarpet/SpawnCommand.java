package quickcarpet;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.arguments.BlockPosArgument.blockPos;
import static net.minecraft.command.arguments.BlockPosArgument.getBlockPos;
import static net.minecraft.command.arguments.BlockPosArgument.getLoadedBlockPos;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.dimension.DimensionType;

public class SpawnCommand {

    private static final SimpleCommandExceptionType ALREADY_TRACKING_EXCEPTION = new SimpleCommandExceptionType(new TextComponentString("You are already tracking spawning."));
    
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("spawn")
                .then(literal("list")
                    .executes(ctx -> spawnList(ctx.getSource(), new BlockPos(ctx.getSource().getPos())))
                    .then(argument("pos", blockPos())
                        .executes(ctx -> spawnList(ctx.getSource(), getLoadedBlockPos(ctx, "pos")))))
                .then(literal("tracking")
                    .executes(ctx -> sendTrackingReport(ctx.getSource()))
                    .then(literal("start")
                        .executes(ctx -> startTracking(ctx.getSource(), null, null))
                        .then(argument("posA", blockPos())
                            .then(argument("posB", blockPos())
                                .executes(ctx -> startTracking(ctx.getSource(), getBlockPos(ctx, "posA"), getBlockPos(ctx, "posB"))))))
                    .then(literal("stop")
                        .executes(ctx -> stopTracking(ctx.getSource())))
                    .then(literal("hostile")
                        .executes(ctx -> recentSpawnsTracking(ctx.getSource(), "hostile")))
                    .then(literal("passive")
                        .executes(ctx -> recentSpawnsTracking(ctx.getSource(), "passive")))
                    .then(literal("water")
                        .executes(ctx -> recentSpawnsTracking(ctx.getSource(), "water")))
                    .then(literal("ambient")
                        .executes(ctx -> recentSpawnsTracking(ctx.getSource(), "ambient"))))
                .then(makeTest())
                .then(literal("mocking")
                    .then(argument("doMock", bool())
                        .executes(ctx -> spawnMocking(ctx.getSource(), getBool(ctx, "doMock")))))
                .then(literal("rates")
                    .executes(ctx -> getGeneralMobcaps(ctx.getSource()))
                    .then(literal("reset")
                        .executes(ctx -> resetSpawnRates(ctx.getSource())))
                    .then(literal("hostile")
                        .then(argument("multiplier", integer(0, 1000))
                            .executes(ctx -> setSpawnRates(ctx.getSource(), "hostile", getInteger(ctx, "multiplier")))))
                    .then(literal("passive")
                        .then(argument("multiplier", integer(0, 1000))
                            .executes(ctx -> setSpawnRates(ctx.getSource(), "passive", getInteger(ctx, "multiplier")))))
                    .then(literal("water")
                        .then(argument("multiplier", integer(0, 1000))
                            .executes(ctx -> setSpawnRates(ctx.getSource(), "water", getInteger(ctx, "multiplier")))))
                    .then(literal("ambient")
                        .then(argument("multiplier", integer(0, 1000))
                            .executes(ctx -> setSpawnRates(ctx.getSource(), "ambient", getInteger(ctx, "multiplier"))))))
                .then(literal("mobcaps")
                    .executes(ctx -> getGeneralMobcaps(ctx.getSource()))
                    .then(literal("set")
                        .executes(ctx -> getGeneralMobcaps(ctx.getSource()))
                        .then(argument("newMobcap", integer(0))
                            .executes(ctx -> setMobcap(ctx.getSource(), getInteger(ctx, "newMobcap")))))
                    .then(argument("dimension", DimensionArgument.func_212595_a())
                        .executes(ctx -> printMobcapsForDimension(ctx.getSource(), DimensionArgument.func_212592_a(ctx, "dimension")))))
                .then(literal("entities")
                    .executes(ctx -> getGeneralMobcaps(ctx.getSource()))
                    .then(literal("hostile")
                        .executes(ctx -> printEntitiesByType(ctx.getSource(), "hostiles")))
                    .then(literal("passive")
                        .executes(ctx -> printEntitiesByType(ctx.getSource(), "passive")))
                    .then(literal("water")
                        .executes(ctx -> printEntitiesByType(ctx.getSource(), "water")))
                    .then(literal("ambient")
                        .executes(ctx -> printEntitiesByType(ctx.getSource(), "ambient")))));
    }
    
    private static ArgumentBuilder<CommandSource, ?> makeTest() {
        ArgumentBuilder<CommandSource, ?> test = literal("test");
        test.executes(ctx -> spawnTest(ctx.getSource(), 72000, null));
        
        ArgumentBuilder<CommandSource, ?> warp = argument("warp", integer(20, 720000));
        warp.executes(ctx -> spawnTest(ctx.getSource(), getInteger(ctx, "warp"), null));
        test.then(warp);
        
        for (EnumDyeColor color : EnumDyeColor.values()) {
            ArgumentBuilder<CommandSource, ?> colorArg = literal(color.getName());
            colorArg.executes(ctx -> spawnTest(ctx.getSource(), getInteger(ctx, "warp"), color));
            warp.then(colorArg);
        }
        
        return test;
    }
    
    private static int spawnList(CommandSource source, BlockPos pos) {
        SpawnReporter.report(pos, source.getWorld()).forEach(line -> source.sendFeedback(line, false));
        return 0;
    }
    
    private static int sendTrackingReport(CommandSource source) {
        SpawnReporter.tracking_report(source.getWorld()).forEach(line -> source.sendFeedback(line, false));
        return 0;
    }
    
    private static int startTracking(CommandSource source, BlockPos posA, BlockPos posB) throws CommandSyntaxException {
        if (SpawnReporter.track_spawns != 0) {
            throw ALREADY_TRACKING_EXCEPTION.create();
        }
        
        BlockPos lsl = null, usl = null;
        if (posA != null) {
            lsl = new BlockPos(Math.min(posA.getX(), posB.getX()), Math.min(posA.getY(), posB.getY()), Math.min(posA.getZ(), posB.getZ()));
            usl = new BlockPos(Math.max(posA.getX(), posB.getX()), Math.max(posA.getY(), posB.getY()), Math.max(posA.getZ(), posB.getZ()));
        }
        SpawnReporter.reset_spawn_stats(false);
        SpawnReporter.track_spawns = (long) source.getWorld().getServer().getTickCounter();
        SpawnReporter.lower_spawning_limit = lsl;
        SpawnReporter.upper_spawning_limit = usl;
        
        source.sendFeedback(new TextComponentString("Spawning tracking started."), true);
        return 0;
    }
    
    private static int stopTracking(CommandSource source) {
        SpawnReporter.tracking_report(source.getWorld()).forEach(line -> source.sendFeedback(line, false));
        SpawnReporter.reset_spawn_stats(false);
        SpawnReporter.track_spawns = 0L;
        SpawnReporter.lower_spawning_limit = null;
        SpawnReporter.upper_spawning_limit = null;
        
        source.sendFeedback(new TextComponentString("Spawning tracking stopped."), true);
        return 0;
    }
    
    private static int recentSpawnsTracking(CommandSource source, String family) {
        SpawnReporter.recent_spawns(source.getWorld(), family).forEach(line -> source.sendFeedback(line, false));
        return 0;
    }
    
    private static int spawnTest(CommandSource source, int warp, EnumDyeColor color) throws CommandSyntaxException {
        SpawnReporter.reset_spawn_stats(false);
        SpawnReporter.track_spawns = (long) source.getWorld().getServer().getTickCounter();
        HopperCounter.reset_hopper_counter(source.getWorld(), color);
        
        TickCommand.scheduledTicksToWarp = 0;
        TickCommand.tickWarp(source, warp);
        
        source.sendFeedback(new TextComponentString("Started spawn test for " + warp + " ticks"), true);
        
        return 0;
    }
    
    private static int spawnMocking(CommandSource source, boolean domock) {
        if (domock) {
            SpawnReporter.initialize_mocking();
            source.sendFeedback(new TextComponentString("Mock spawns started, Spawn statistics reset"), true);
        } else {
            SpawnReporter.stop_mocking();
            source.sendFeedback(new TextComponentString("Normal mob spawning, Spawn statistics reset"), true);
        }
        return 0;
    }
    
    private static int getGeneralMobcaps(CommandSource source) {
        SpawnReporter.print_general_mobcaps(source.getWorld()).forEach(line -> source.sendFeedback(line, false));
        return 0;
    }
    
    private static int resetSpawnRates(CommandSource source) {
        for (String s : SpawnReporter.spawn_tries.keySet()) {
            SpawnReporter.spawn_tries.put(s,1);
        }
        getGeneralMobcaps(source);
        return 0;
    }
    
    private static int setSpawnRates(CommandSource source, String family, int rates) {
        SpawnReporter.spawn_tries.put(family, rates);
        getGeneralMobcaps(source);
        return 0;
    }
    
    private static int setMobcap(CommandSource source, int newMobcap) {
        double desiredRatio = (double) newMobcap / EnumCreatureType.MONSTER.getMaxNumberOfCreature();
        SpawnReporter.mobcap_exponent = 4 * Math.log(desiredRatio) / Math.log(2);
        
        source.sendFeedback(new TextComponentString("Mobcaps for hostile mobs changed to " + newMobcap + ", other groups will follow"), true);
        
        return 0;
    }
    
    private static int printMobcapsForDimension(CommandSource source, DimensionType dimension) {
        SpawnReporter.printMobcapsForDimension(source.getWorld(), dimension.getId(), dimension.toString())
            .forEach(line -> source.sendFeedback(line, false));
        return 0;
    }
    
    private static int printEntitiesByType(CommandSource source, String family) {
        SpawnReporter.printEntitiesByType(family, source.getWorld()).forEach(line -> source.sendFeedback(line, false));
        return 0;
    }
    
}
