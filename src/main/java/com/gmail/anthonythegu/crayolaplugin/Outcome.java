package com.gmail.anthonythegu.crayolaplugin;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Outcome {

    private final String type;
    private final String body;
    private final Player player;
    private final World world;
    private final Item item;

    public Outcome(String type, String body, Player player, Item item) {
        this.type = type;
        this.body = body;
        this.player = player;
        this.world = player.getWorld();
        this.item = item;
    }

    public void execute() {
        switch (type) {
            case "EFFECT": player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(body), 20 * 15, 0)); break;
            case "ITEM": world.dropItemNaturally(item.getLocation(), new ItemStack(Material.matchMaterial(body))); break;
            case "ENTITY": world.spawnEntity(item.getLocation(), EntityType.valueOf(body)); break;
            case "STRIKE_LIGHTNING": world.strikeLightning(player.getLocation()); break;
            case "DOUBLE_ITEM": doubleItem(item, world); break;
            case "INCREASE_HEALTH": changeMaxHealth(player, 2.0d); break;
            case "DECREASE_HEALTH":
                if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() >= 4.0d) {
                    changeMaxHealth(player, -2.0d);
                }
                break;
            case "BIND_ARMORPIECE":
                ItemStack[] armorPieces = player.getInventory().getArmorContents();
                for (ItemStack armorPiece: armorPieces) {
                    if (armorPiece != null && !armorPiece.getEnchantments().containsKey(Enchantment.BINDING_CURSE)) {
                        armorPiece.addEnchantment(Enchantment.BINDING_CURSE, 1);
                        world.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
                        break;
                    }
                }
        }
    }

    private static void doubleItem(Item item, World world) {
        ItemStack newItem1 = item.getItemStack();
        ItemStack newItem2 = item.getItemStack();
        newItem1.setAmount(1);
        newItem2.setAmount(1);
        world.dropItemNaturally(item.getLocation(), newItem1);
        world.dropItemNaturally(item.getLocation(), newItem2);
    }

    private static void changeMaxHealth(Player player, double health) {
        final double maxHP = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHP + health);
    }

    public String getType() {
        return this.type;
    }

    public String getBody() {
        return this.body;
    }

    public static Outcome parseOutcome(String outcome, Player player, Item item) {

        outcome = outcome.toUpperCase();

        if (outcome.startsWith("EFFECT_")) {
            outcome = outcome.replace("EFFECT_", "");
            if (PotionEffectType.getByName(outcome) == null) {
                return null;
            }
            return new Outcome("EFFECT", outcome, player, item);
        }

        if (outcome.startsWith("ITEM_")) {
            outcome = outcome.replace("ITEM_", "");

            if (Material.matchMaterial(outcome) == null) {
                return null;
            }
            return new Outcome("ITEM", outcome, player, item);
        }

        if (outcome.startsWith("ENTITY_")) {
            outcome = outcome.replace("ENTITY_", "");

            try {
                EntityType.valueOf(outcome);
                return new Outcome("ENTITY", outcome, player, item);
            } catch(IllegalArgumentException e) {
                return null;
            }
        }

        if (outcome.startsWith("STRIKE_LIGHTNING") || outcome.startsWith("LIGHTNING_STRIKE")) {
            return new Outcome("STRIKE_LIGHTNING", "STRIKE_LIGHTNING", player, item);
        }
        if (outcome.startsWith("DOUBLE_ITEM") || outcome.startsWith("DOUBLE_ITEMS")) {
            return new Outcome("DOUBLE_ITEM", "DOUBLE_ITEM", player, item);
        }
        if (outcome.startsWith("INCREASE_HEALTH")) {
            return new Outcome("INCREASE_HEALTH", "INCREASE_HEALTH", player, item);
        }
        if (outcome.startsWith("DECREASE_HEALTH")) {
            return new Outcome("DECREASE_HEALTH", "DECREASE_HEALTH", player, item);
        }
        if (outcome.startsWith("BIND_ARMOR")) {
            return new Outcome("BIND_ARMORPIECE", "BIND_ARMORPIECE", player, item);
        }
        return null;
    }
}