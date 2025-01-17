package forceitembattle.util;

import forceitembattle.ForceItemBattle;
import org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RecipeInventory extends InventoryBuilder {

    public static final int NEXT_RECIPE_ITEM_SLOT = 8;
    public static final int PREVIOUS_RECIPE_ITEM_SLOT = 0;
    private static final int RESULT_SLOT = 25;
    private static final int STATION_SLOT = 23;
    /**
     * Slots that contain recipe items, the station and result items.
     */
    public static final List<Integer> SLOTS = List.of(
            10, 11, 12,
            19, 20, 21,
            28, 29, 30,
            STATION_SLOT, RESULT_SLOT
    );
    private static final int WORKBENCH_FIRST_ITEM_SLOT = 10;
    private static final int SMITHING_FIRST_ITEM_SLOT = 19;
    private static final int OTHER_FIRST_ITEM_SLOT = 20;

    public RecipeInventory(ForceItemBattle forceItemBattle, RecipeViewer recipeViewer, Player player) {
        super(9 * 5, "§8● §3" +
                materialName(recipeViewer.getItemStack().getType()) +
                " §8» §7" + (recipeViewer.getCurrentRecipeIndex() + 1) + "§8/§7" + recipeViewer.getPages()
        );

        for (int i = 0; i < this.getInventory().getSize(); i++) {
            if (!SLOTS.contains(i)) {
                this.setItem(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("§2").getItemStack());
            }
        }

        if (forceItemBattle.getRecipeManager().closeHandlers.containsKey(recipeViewer.getUuid())) {
            forceItemBattle.getRecipeManager().handleRecipeClose(player);
        }

        forceItemBattle.getRecipeManager().ignoreCloseHandler.put(recipeViewer.getUuid(), false);

        forceItemBattle.getRecipeManager().closeHandlers.put(recipeViewer.getUuid(), () -> forceItemBattle.getRecipeManager().ignoreCloseHandler.remove(player.getUniqueId()));

        this.addUpdateHandler(() -> {
            this.setItem(PREVIOUS_RECIPE_ITEM_SLOT, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§4« §cPrevious Recipe").getItemStack(), event -> {
                if (recipeViewer.getPages() == 1) {
                    return;
                }

                int currentRecipeIndex = recipeViewer.getCurrentRecipeIndex();

                if (currentRecipeIndex == 0) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    return;
                }

                currentRecipeIndex--;
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

                recipeViewer.setCurrentRecipeIndex(currentRecipeIndex);
                recipeViewer.setRecipe(recipeViewer.getRecipes().get(recipeViewer.getCurrentRecipeIndex()));

                new RecipeInventory(forceItemBattle, recipeViewer, player).open(player);
            });

            this.setItem(NEXT_RECIPE_ITEM_SLOT, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setDisplayName("§2» §aNext Recipe").getItemStack(), inventoryClickEvent -> {
                if (recipeViewer.getPages() == 1) {
                    return;
                }

                int currentRecipeIndex = recipeViewer.getCurrentRecipeIndex();

                if (currentRecipeIndex == (recipeViewer.getPages() - 1)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    return;
                }

                currentRecipeIndex++;

                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

                recipeViewer.setCurrentRecipeIndex(currentRecipeIndex);
                recipeViewer.setRecipe(recipeViewer.getRecipes().get(recipeViewer.getCurrentRecipeIndex()));

                new RecipeInventory(forceItemBattle, recipeViewer, player).open(player);
            });
        });

        List<ItemStack> ingredients = new ArrayList<>();

        if (recipeViewer.getRecipe() instanceof ShapedRecipe shaped) {
            String[] shape = shaped.getShape();

            int rowIndex = 0;
            for (String row : shape) {
                int charIndex = 0;
                for (char c : row.toCharArray()) {
                    int slot = WORKBENCH_FIRST_ITEM_SLOT + rowIndex * 9 + charIndex;

                    RecipeChoice choice = shaped.getChoiceMap().get(c);
                    if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {

                        this.setItem(slot, this.choiceWithLore(materialChoice, recipeViewer));

                    } else if (choice != null) {
                        this.setItem(slot, new ItemStack(choice.getItemStack()));
                    }
                    charIndex++;
                }
                rowIndex++;
            }

        }
        if (recipeViewer.getRecipe() instanceof ShapelessRecipe shapeless) {
            for (RecipeChoice recipeChoice : shapeless.getChoiceList()) {
                if (recipeChoice instanceof RecipeChoice.MaterialChoice materialChoice) {
                    ingredients.add(this.choiceWithLore(materialChoice, recipeViewer));
                } else if (recipeChoice != null) {
                    ingredients.add(new ItemStack(recipeChoice.getItemStack()));
                }
            }

            int index = 0;
            for (ItemStack ingredient : ingredients) {
                this.setItem(convertItemIndexToInventorySlot(WORKBENCH_FIRST_ITEM_SLOT, index), ingredient);
                index++;
            }
        }
        if (recipeViewer.getRecipe() instanceof CookingRecipe<?> furnace) {
            if (furnace.getInputChoice() instanceof RecipeChoice.MaterialChoice materialChoice) {
                this.setItem(OTHER_FIRST_ITEM_SLOT, this.choiceWithLore(materialChoice, recipeViewer));
            }

        }
        if (recipeViewer.getRecipe() instanceof SmithingTrimRecipe smithing) {
            ingredients.add(smithing.getBase().getItemStack());
            ingredients.add(smithing.getTemplate().getItemStack());
            ingredients.add(smithing.getAddition().getItemStack());

            int index = 0;
            for (ItemStack ingredient : ingredients) {
                this.setItem(OTHER_FIRST_ITEM_SLOT + index, ingredient);
                index++;
            }

        } else if (recipeViewer.getRecipe() instanceof SmithingTransformRecipe smithing) {
            ingredients.add(smithing.getBase().getItemStack());
            ingredients.add(smithing.getTemplate().getItemStack());
            ingredients.add(smithing.getAddition().getItemStack());

            int index = 0;
            for (ItemStack ingredient : ingredients) {
                this.setItem(SMITHING_FIRST_ITEM_SLOT + index, ingredient);
                index++;
            }

        } else if (recipeViewer.getRecipe() instanceof SmithingRecipe smithing) {
            // Unknown smithing recipe?
            ingredients.add(smithing.getAddition().getItemStack());
            ingredients.add(smithing.getBase().getItemStack());

            int index = 0;
            for (ItemStack ingredient : ingredients) {
                this.setItem(SMITHING_FIRST_ITEM_SLOT + index, ingredient);
                index++;
            }

        }
        if (recipeViewer.getRecipe() instanceof MerchantRecipe merchant) {
            ItemStack fixed = new ItemStack(merchant.getResult().getType(), 1, (byte) 0);
            ingredients.add(fixed);

            this.setItem(OTHER_FIRST_ITEM_SLOT, fixed);

        }
        if (recipeViewer.getRecipe() instanceof StonecuttingRecipe stonecutting) {
            ItemStack fixed = new ItemStack(stonecutting.getInput());
            ingredients.add(fixed);

            this.setItem(OTHER_FIRST_ITEM_SLOT, fixed);
        }

        this.setItem(RESULT_SLOT, recipeViewer.getRecipe().getResult());
        this.setItem(STATION_SLOT, getStationItem(recipeViewer.getRecipe()));


        this.addClickHandler(inventoryClickEvent -> {
            if (inventoryClickEvent.getClickedInventory() == null) {
                return;
            }

            inventoryClickEvent.setCancelled(true);

            if (!inventoryClickEvent.getClickedInventory().equals(inventoryClickEvent.getView().getTopInventory())) {
                return;
            }

            if (!RecipeInventory.SLOTS.contains(inventoryClickEvent.getSlot())) {
                return;
            }

            if (inventoryClickEvent.getClick().isShiftClick()) {
                ItemStack itemStack = inventoryClickEvent.getCurrentItem();
                if (itemStack == null) {
                    return;
                }
                if (Bukkit.getRecipesFor(itemStack).isEmpty()) {
                    player.sendMessage("§cThere is no recipe for this item. Just find it lol");
                    return;
                }
                recipeViewer.setCurrentRecipeIndex(0);
                recipeViewer.setItemStack(itemStack);
                if (Bukkit.getRecipesFor(recipeViewer.getItemStack()).size() > 1) {
                    recipeViewer.setRecipe(Bukkit.getRecipesFor(recipeViewer.getItemStack()).get(recipeViewer.getCurrentRecipeIndex()));
                } else {
                    recipeViewer.setRecipe(Bukkit.getRecipesFor(recipeViewer.getItemStack()).get(0));
                }
                new RecipeInventory(forceItemBattle, recipeViewer, player).open(player);

            } else {
                player.sendMessage("§cSneak click to show recipe for this item!");
            }
        });

        this.addCloseHandler(inventoryCloseEvent -> {
            if (forceItemBattle.getRecipeManager().isShowingRecipe(player) && !forceItemBattle.getRecipeManager().ignoreInventoryClosed(player)) {
                inventoryCloseEvent.getInventory().clear();
                forceItemBattle.getRecipeManager().handleRecipeClose(player);
            }
        });
    }

    private static String materialName(Material type) {
        return WordUtils.capitalize(type.name().replace("_", " ").toLowerCase());
    }

    private ItemStack choiceWithLore(RecipeChoice.MaterialChoice materialChoice, RecipeViewer recipeViewer) {
        List<String> lore = new ArrayList<>();
        ItemBuilder itemBuilder = new ItemBuilder(materialChoice.getChoices().get(0));

        materialChoice.getChoices().subList(1, materialChoice.getChoices().size()).forEach(material -> {
            lore.add(" §8» §3" + materialName(material));

            if (material.name().contains("_PLANKS")) {
                lore.clear();
                lore.add(" §8» §3any wooden plank");
            }

            if (recipeViewer.getItemStack().getType() == Material.SMOKER || recipeViewer.getItemStack().getType() == Material.CAMPFIRE || recipeViewer.getItemStack().getType() == Material.SOUL_CAMPFIRE || recipeViewer.getItemStack().getType() == Material.CHARCOAL) {
                lore.clear();
                lore.add(" §8» §3any wooden log/wood (and stripped variants)");
            }

            // These 2 are hardcoded to only have 1 material choice, which would be dye and flower respectively
            if (recipeViewer.getItemStack().getType() == Material.FIREWORK_STAR) {
                lore.clear();
                lore.add(" §8» §3any dye item");
            }

            if (recipeViewer.getItemStack().getType() == Material.SUSPICIOUS_STEW) {
                lore.clear();
                lore.add(" §8» §3any field flower");
            }
        });

        itemBuilder.setLore(lore);
        lore.clear();

        return itemBuilder.getItemStack();
    }

    private ItemStack getStationItem(Recipe recipe) {
        if (recipe instanceof ShapedRecipe) {
            return new ItemStack(Material.CRAFTING_TABLE);
        } else if (recipe instanceof ShapelessRecipe) {
            return new ItemStack(Material.CRAFTING_TABLE);
        } else if (recipe instanceof FurnaceRecipe) {
            return new ItemStack(Material.FURNACE);
        } else if (recipe instanceof SmithingRecipe) {
            return new ItemStack(Material.SMITHING_TABLE);
        } else if (recipe instanceof SmokingRecipe) {
            return new ItemStack(Material.SMOKER);
        } else if (recipe instanceof BlastingRecipe) {
            return new ItemStack(Material.BLAST_FURNACE);
        } else if (recipe instanceof CampfireRecipe) {
            return new ItemStack(Material.CAMPFIRE);
        } else if (recipe instanceof StonecuttingRecipe) {
            return new ItemStack(Material.STONECUTTER);
        } else if (recipe instanceof MerchantRecipe) {
            return new ItemStack(Material.VILLAGER_SPAWN_EGG);
        } else {
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§cUnknown recipe type: §f" + recipe.getClass().getSimpleName());
                item.setItemMeta(meta);
            }

            return item;
        }
    }

    private int convertItemIndexToInventorySlot(int firstItemSlot, int itemIndex) {
        return firstItemSlot + itemIndex % 3 + 9 * (itemIndex / 3);
    }
}
