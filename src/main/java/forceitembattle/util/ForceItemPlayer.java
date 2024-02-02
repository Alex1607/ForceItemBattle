package forceitembattle.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class ForceItemPlayer {

    private Player player;
    private List<ForceItem> foundItems;
    private Material currentMaterial;
    private int remainingJokers;
    private Integer currentScore;

    public ForceItemPlayer(Player player, List<ForceItem> foundItems, Material currentMaterial, int remainingJokers, Integer currentScore) {
        this.player = player;
        this.foundItems = foundItems;
        this.currentMaterial = currentMaterial;
        this.remainingJokers = remainingJokers;
        this.currentScore = currentScore;
    }

    public Player player() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<ForceItem> foundItems() {
        return foundItems;
    }

    public void addFoundItemToList(ForceItem forceItem) {
        this.foundItems.add(forceItem);
    }

    public Material currentMaterial() {
        return currentMaterial;
    }

    public void setCurrentMaterial(Material currentMaterial) {
        this.currentMaterial = currentMaterial;
    }

    public int remainingJokers() {
        return remainingJokers;
    }

    public void setRemainingJokers(int remainingJokers) {
        this.remainingJokers = remainingJokers;
    }

    public Integer currentScore() {
        return currentScore;
    }

    public void setCurrentScore(Integer currentScore) {
        this.currentScore = currentScore;
    }
}
