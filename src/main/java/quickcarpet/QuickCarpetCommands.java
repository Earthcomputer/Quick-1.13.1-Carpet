package quickcarpet;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;

public class QuickCarpetCommands {

    public static void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
        FillLimitCommand.register(dispatcher);
    }
    
}
