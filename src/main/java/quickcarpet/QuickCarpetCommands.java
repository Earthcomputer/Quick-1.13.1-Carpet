package quickcarpet;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.util.ResourceLocation;

public class QuickCarpetCommands {

    public static void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
        FillLimitCommand.register(dispatcher);
        FillUpdatesCommand.register(dispatcher);
        TickCommand.register(dispatcher);
        StructureCommand.register(dispatcher);
    }
    
    public static void registerCustomArgumentTypes() {
        ArgumentTypes.register(new ResourceLocation("quickcarpet:template"), TemplateArgument.class, new ArgumentSerializer<>(TemplateArgument::template));
        ArgumentTypes.register(new ResourceLocation("quickcarpet:long"), LongArgumentType.class, new ArgumentSerializer<>(LongArgumentType::longArg));
    }
    
}
