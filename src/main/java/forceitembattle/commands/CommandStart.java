package forceitembattle.commands;

import forceitembattle.ForceItemBattle;
import forceitembattle.manager.Gamemanager;
import forceitembattle.settings.GameSetting;
import forceitembattle.settings.GameSettings;
import forceitembattle.settings.preset.GamePreset;
import forceitembattle.util.ForceItemPlayer;
import forceitembattle.util.GameState;
import forceitembattle.util.PlayerStat;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandStart implements CommandExecutor {

    private ForceItemBattle forceItemBattle;

    public CommandStart(ForceItemBattle forceItemBattle) {
        this.forceItemBattle = forceItemBattle;
        this.forceItemBattle.getCommand("start").setTabCompleter(new TabCompletion(forceItemBattle));
        this.forceItemBattle.getCommand("start").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player player) {
            if(args.length == 1) {
                if(this.forceItemBattle.getSettings().getGamePreset(args[0]) == null) {
                    player.sendMessage("§e" + args[0] + " §cdoes not exist in presets.");
                    return false;
                }

                GamePreset gamePreset = this.forceItemBattle.getSettings().getGamePreset(args[0]);
                this.forceItemBattle.getGamemanager().setCurrentGamePreset(gamePreset);
                this.performCommand(gamePreset, player, args);

            } else if (args.length == 2) {
                try {
                    this.performCommand(null, player, args);

                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Usage: /start <time in min> <jokers>");
                    sender.sendMessage(ChatColor.RED + "<time> and <jokers> have to be numbers");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /start <time in min> <jokers>");
            }
        }



        return false;
    }

    private void performCommand(GamePreset gamePreset, Player player, String[] args) {
        int countdown = (gamePreset != null ? gamePreset.countdown() * 60 : (Integer.parseInt(args[0]) * 60));
        int jokers = (gamePreset != null ? gamePreset.jokers() : (Integer.parseInt(args[1])));
        this.forceItemBattle.getTimer().setTime(countdown);
        this.forceItemBattle.getGamemanager().initializeMats();

        if(gamePreset == null) {
            if (Integer.parseInt(args[1]) > 64) {
                player.sendMessage(ChatColor.RED + "The maximum amount of jokers is 64.");
                return;
            }
        }

        new BukkitRunnable() {

            int seconds = 11;
            @Override
            public void run() {
                seconds--;
                if(seconds == 0) {
                    cancel();
                    startGame(gamePreset, jokers);
                    return;
                }
                if(seconds < 6) Bukkit.getOnlinePlayers().forEach(players -> players.playSound(players.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1, 1));

                String finalSubTitle = getString();
                Bukkit.getOnlinePlayers().forEach(players -> players.sendTitle("§a" + seconds, finalSubTitle, 0, 20, 10));
            }

            private String getString() {
                String subTitle = "";

                switch(seconds) {
                    case 9, 8 -> subTitle = "§f» §6" + (forceItemBattle.getTimer().getTime() / 60) + " minutes §f«";
                    case 7, 6 -> subTitle = "§f» §6" + jokers + " Joker §f«";
                    case 5 -> subTitle = "§f» §6/info & /infowiki §f«";
                    case 4 -> subTitle = "§f» §6/spawn & /bed §f«";
                    case 3, 2 -> subTitle = "§f» §6Collect as many items as possible §f«";
                    case 1 -> subTitle = "§f» §6Have fun! §f«";
                }

                return subTitle;
            }
        }.runTaskTimer(this.forceItemBattle, 0L, 20L);
    }

    private void startGame(GamePreset gamePreset, Integer jokersAmount) {
        World world = Bukkit.getWorld("world");
        assert world != null;
        Location spawnLocation = world.getSpawnLocation();
        this.forceItemBattle.setSpawnLocation(spawnLocation);

        Bukkit.getOnlinePlayers().forEach(player -> {

            ForceItemPlayer forceItemPlayer = this.forceItemBattle.getGamemanager().getForceItemPlayer(player.getUniqueId());
            forceItemPlayer.setRemainingJokers(jokersAmount);

            player.sendMessage(" ");
            player.sendMessage("§8» §6§lMystery Item Battle §8«");
            player.sendMessage(" ");
            player.sendMessage("  §8● §7Duration §8» §a" + this.forceItemBattle.getTimer().getTime() / 60 + " minutes");
            player.sendMessage("  §8● §7Joker §8» §a" + jokersAmount);
            for(GameSetting gameSettings : GameSetting.values()) {
                player.sendMessage("  §8● §7" + gameSettings.displayName() + " §8» §a" + (this.forceItemBattle.getSettings().isSettingEnabled(gameSettings) ? "§2✔" : "§4✘"));
            }
            player.sendMessage(" ");
            player.sendMessage(" §8● §7Useful Commands:");
            player.sendMessage("  §8» §6/info");
            player.sendMessage("  §8» §6/infowiki");
            player.sendMessage("  §8» §6/spawn");
            player.sendMessage("  §8» §6/bed");
            player.sendMessage("");

            player.setHealth(20);
            player.setSaturation(20);
            player.getInventory().clear();
            player.getInventory().setItem(4, Gamemanager.getJokers(jokersAmount));

            player.getInventory().addItem(new ItemStack(Material.STONE_AXE));
            player.getInventory().addItem(new ItemStack(Material.STONE_PICKAXE));

            player.setLevel(0);
            player.setExp(0);
            player.setWalkSpeed(0.2f);
            player.setStatistic(Statistic.TIME_SINCE_REST, 72000); // 1hr = 3600 seconds * 20 ticks
            player.getPassengers().forEach(Entity::remove);
            player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(spawnLocation);
            player.playSound(player, Sound.BLOCK_END_PORTAL_SPAWN, 1, 1);

            if(this.forceItemBattle.getSettings().isSettingEnabled(GameSetting.NETHER)) {
                this.forceItemBattle.getBackpack().createBackpack(player);
            }

            if(!this.forceItemBattle.getSettings().isSettingEnabled(GameSetting.NETHER)) {
                forceItemPlayer.createItemDisplay();
            }

            if(this.forceItemBattle.getSettings().isSettingEnabled(GameSetting.STATS)) {
                this.forceItemBattle.getStatsManager().addToStats(PlayerStat.GAMES_PLAYED, this.forceItemBattle.getStatsManager().playerStats(player.getName()), 1);
            }


        });
        Bukkit.getWorld("world").setTime(0);

        this.forceItemBattle.getGamemanager().setCurrentGameState(GameState.MID_GAME);
    }
}
