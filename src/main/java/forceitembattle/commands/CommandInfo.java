package forceitembattle.commands;

import forceitembattle.ForceItemBattle;
import forceitembattle.util.RecipeInventory;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandInfo implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player)) return false;

        ItemStack item;
        if (ForceItemBattle.getTimer().isRunning()) {
            item = new ItemStack(ForceItemBattle.getGamemanager().getCurrentMaterial(player));
        } else {
            item = player.getInventory().getItemInMainHand();
        }

        if (item.getType() == Material.AIR) {
            player.sendMessage("§cYou need to hold an item in your hand!");
            return false;
        }

        RecipeInventory.showRecipe(player, item);

        return false;
    }

}
