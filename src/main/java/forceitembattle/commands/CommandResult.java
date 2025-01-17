package forceitembattle.commands;

import forceitembattle.ForceItemBattle;
import forceitembattle.settings.GameSetting;
import forceitembattle.util.FinishInventory;
import forceitembattle.util.ForceItemPlayer;
import forceitembattle.util.ForceItemPlayerStats;
import forceitembattle.util.PlayerStat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class CommandResult implements CommandExecutor {

    private final ForceItemBattle forceItemBattle;
    public int place;

    public CommandResult(ForceItemBattle forceItemBattle) {
        this.forceItemBattle = forceItemBattle;
        this.forceItemBattle.getCommand("result").setExecutor(this);

        this.place = -1;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (this.forceItemBattle.getTimer().getTime() > 0) return false;
        if (!(commandSender instanceof Player player)) return false;
        if (args.length == 0) {
            if (!player.isOp()) {
                return false;
            }

            if (this.forceItemBattle.getGamemanager().getForceItemPlayerMap().isEmpty() || this.place == 0) {
                player.sendMessage("No more players left.");
                return false;
            }

            Map<UUID, ForceItemPlayer> sortedMapDesc = this.forceItemBattle.getGamemanager().sortByValue(this.forceItemBattle.getGamemanager().getForceItemPlayerMap(), false);
            if (this.place == -1) this.place = sortedMapDesc.size();
            UUID uuid = (UUID) sortedMapDesc.keySet().toArray()[this.place - 1];

            Bukkit.getOnlinePlayers().forEach(players -> new FinishInventory(this.forceItemBattle, this.forceItemBattle.getGamemanager().getForceItemPlayer(uuid), this.place, true).open(players));

            if (forceItemBattle.getSettings().isSettingEnabled(GameSetting.STATS)) {
                ForceItemPlayer forceItemPlayer = this.forceItemBattle.getGamemanager().getForceItemPlayer(uuid);
                ForceItemPlayerStats forceItemPlayerStats = forceItemBattle.getStatsManager().playerStats(forceItemPlayer.getPlayer().getName());
                forceItemBattle.getStatsManager().addToStats(PlayerStat.TRAVELLED, forceItemPlayerStats, forceItemBattle.getStatsManager().calculateDistance(forceItemPlayer.getPlayer()));

                if (forceItemPlayerStats.getHighestScore() < forceItemPlayer.getCurrentScore()) {
                    forceItemBattle.getStatsManager().addToStats(PlayerStat.HIGHEST_SCORE, forceItemPlayerStats, forceItemPlayer.getCurrentScore());
                }

                if (place == 1) {
                    forceItemBattle.getStatsManager().addToStats(PlayerStat.GAMES_WON, forceItemPlayerStats, 1);
                }
            }

            this.place--;

        } else if (args.length == 1) {
            new FinishInventory(this.forceItemBattle, this.forceItemBattle.getGamemanager().getForceItemPlayer(UUID.fromString(args[0])), null, false).open(player);
            return true;
        }

        return false;
    }
}
