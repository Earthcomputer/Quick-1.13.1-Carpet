package quickcarpet;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class TemplateArgument implements ArgumentType<ResourceLocation>
{
    
    public static TemplateArgument template()
    {
        return new TemplateArgument();
    }

    public static ResourceLocation getTemplate(CommandContext<CommandSource> context, String name)
    {
        return (ResourceLocation)context.getArgument(name, ResourceLocation.class);
    }

    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException
    {
        return new ResourceLocation(reader.readString());
    }
    
    public static <S> CompletableFuture<Suggestions> suggestTriggers(CommandContext<S> context, SuggestionsBuilder builder)
    {
        TemplateManager manager = MinecraftServer.INSTANCE.getWorld(DimensionType.OVERWORLD).getStructureTemplateManager();
        return ISuggestionProvider.suggest(StructureCommand.listStructures(manager).stream().map(ResourceLocation::toString).map(s -> s.contains(" ") ? "\"" + s + "\"" : s).collect(Collectors.toList()), builder);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        return suggestTriggers(context, builder);
    }
}