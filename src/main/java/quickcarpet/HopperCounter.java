package quickcarpet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;

public class HopperCounter {
    public static final HashMap<EnumDyeColor, HashMap<String, Long>> hopper_counter = new HashMap<>();
    public static final HashMap<EnumDyeColor, Long> hopper_counter_start_tick = new HashMap<>();
    public static final HashMap<EnumDyeColor, Long> hopper_counter_start_millis = new HashMap<>();

    static {
        for (EnumDyeColor color : EnumDyeColor.values()) {
            hopper_counter.put(color, new HashMap<String, Long>());
            hopper_counter_start_tick.put(color, 0L);
            hopper_counter_start_millis.put(color, 0L);
        }
    }

    public static void count_hopper_items(World worldIn, EnumDyeColor color, ItemStack itemstack) {
        ItemStack testStack = itemstack.copy();
        testStack.setCount(1);
        String item_name = testStack.getDisplayName().getFormattedText();
        int count = itemstack.getCount();
        // LOG.error(String.format("Received %d %s for %s", count, item_name,
        // color_string));
        if (hopper_counter_start_tick.get(color) == 0L) {
            hopper_counter_start_tick.put(color, (long) worldIn.getServer().getTickCounter());
            hopper_counter_start_millis.put(color, Util.milliTime());
        }

        long curr_count = hopper_counter.get(color).getOrDefault(item_name, 0L);
        hopper_counter.get(color).put(item_name, curr_count + count);

    }

    public static void reset_hopper_counter(World worldIn, EnumDyeColor color) {
        if (color == null) {
            for (EnumDyeColor clr : EnumDyeColor.values()) {

                hopper_counter.put(clr, new HashMap<String, Long>());
                hopper_counter_start_tick.put(clr, (long) worldIn.getServer().getTickCounter());
                hopper_counter_start_millis.put(clr, Util.milliTime());
            }
        } else {
            hopper_counter.put(color, new HashMap<String, Long>());
            hopper_counter_start_tick.put(color, (long) worldIn.getServer().getTickCounter());
            hopper_counter_start_millis.put(color, Util.milliTime());
        }
    }

    public static List<ITextComponent> query_hopper_all_stats(MinecraftServer server, boolean realtime) {
        List<ITextComponent> lst = new ArrayList<>();

        for (EnumDyeColor clr : EnumDyeColor.values()) {
            List<ITextComponent> temp = query_hopper_stats_for_color(server, clr, realtime, false);
            if (temp.size() > 1) {
                lst.addAll(temp);
                lst.add(new TextComponentString(""));
            }
        }
        if (lst.size() == 0) {
            lst.add(new TextComponentString("No items have been counted yet."));
        }
        return lst;
    }

    public static List<ITextComponent> query_hopper_stats_for_color(MinecraftServer server, EnumDyeColor color,
            boolean realtime, boolean brief) {
        List<ITextComponent> lst = new ArrayList<>();

        if (hopper_counter.get(color) == null)
            return lst;

        if (hopper_counter.get(color).isEmpty()) {
            if (brief) {
                lst.add(new TextComponentString("" + TextFormatting.GRAY + color + ": -, -/h, - min "));
            } else {
                lst.add(new TextComponentString(String.format("No items for %s yet", color)));
            }
            return lst;
        }
        long total = 0L;
        for (String item_name : hopper_counter.get(color).keySet()) {
            total += hopper_counter.get(color).get(item_name);
        }
        long total_ticks = 0L;
        if (realtime) {
            total_ticks = (Util.milliTime() - hopper_counter_start_millis.get(color)) / 50L + 1L;
        } else {
            total_ticks = (long) server.getTickCounter() - hopper_counter_start_tick.get(color) + 1L;
        }
        if (total == 0L) {
            if (brief) {
                lst.add(new TextComponentString(TextFormatting.AQUA
                        + String.format("%s: 0, 0/h, %.1f min ", color, total_ticks * 1.0 / (20 * 60))));
            } else {
                ITextComponent msg = new TextComponentString(
                        TextFormatting.WHITE + String.format("w No items for %s yet (%.2f min.%s)", color,
                                total_ticks * 1.0 / (20 * 60), (realtime ? " - real time" : "")));
                msg.appendSibling(makeResetButton(color));
                lst.add(msg);
            }
            return lst;
        }

        if (!brief) {
            ITextComponent msg = new TextComponentString(TextFormatting.WHITE + String.format(
                    "Items for %s (%.2f min.%s), total: %d, (%.1f/h):", color, total_ticks * 1.0 / (20 * 60),
                    (realtime ? " - real time" : ""), total, total * 1.0 * (20 * 60 * 60) / total_ticks));
            msg.appendSibling(makeResetButton(color));
            lst.add(msg);
            for (String item_name : hopper_counter.get(color).keySet()) {
                lst.add(new TextComponentString(
                        String.format(" - %s: %d, %.1f/h", item_name, hopper_counter.get(color).get(item_name),
                                hopper_counter.get(color).get(item_name) * 1.0 * (20 * 60 * 60) / total_ticks)));
            }
        } else {
            lst.add(new TextComponentString(TextFormatting.AQUA + String.format("c %s: %d, %d/h, %.1f min ", color,
                    total, total * (20 * 60 * 60) / total_ticks, total_ticks * 1.0 / (20 * 60))));
        }
        return lst;
    }

    private static ITextComponent makeResetButton(EnumDyeColor color) {
        ITextComponent resetButton = new TextComponentString(
                "" + TextFormatting.DARK_RED + TextFormatting.BOLD + "[X]");
        resetButton.getStyle().setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(TextFormatting.GRAY + "reset")));
        resetButton.getStyle()
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/counter " + color + " reset"));
        return resetButton;
    }
}
