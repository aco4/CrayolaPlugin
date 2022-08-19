package com.gmail.anthonythegu.crayolaplugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.gmail.anthonythegu.crayolaplugin.Outcome.parseOutcome;

public class Prayer {

    private final Main plugin = Main.getPlugin(Main.class);
    private final BukkitTask loop;
    private final List<String> actions = new ArrayList<>();
    private final Player player;
    private final World world;
    private final FileConfiguration config;
    private final int UPPER_ANGLE_THRESHOLD;
    private final int LOWER_ANGLE_THRESHOLD;
    private final int NODS_REQUIRED;
    private final int NOD_THRESHOLD;
    private final int PRAYER_INTENSITY;
    private final int PRAYER_RANGE;

    public Prayer(Player p) {
        player = p;
        world = p.getWorld();
        config = plugin.getConfig();
        UPPER_ANGLE_THRESHOLD = config.getInt("upper-angle-threshold", -30);
        LOWER_ANGLE_THRESHOLD = config.getInt("lower-angle-threshold", 45);
        NODS_REQUIRED = config.getInt("nods-required", 25);
        NOD_THRESHOLD = config.getInt("nod-threshold", 6);
        PRAYER_INTENSITY = config.getInt("prayer-intensity", 20);
        PRAYER_RANGE = config.getInt("prayer-range", 3);

        BukkitScheduler scheduler = Bukkit.getScheduler();
        this.loop = scheduler.runTaskTimer(plugin, () -> {

            float pitch = player.getLocation().getPitch();

            String previousAction = "";
            if (actions.size() > 0) {
                previousAction = actions.get(actions.size()-1);
            }

            if (pitch < UPPER_ANGLE_THRESHOLD && !Objects.equals(previousAction, "up")) {
                completeAction("up");
            }
            if (pitch > LOWER_ANGLE_THRESHOLD && !Objects.equals(previousAction, "down")) {
                completeAction("down");
            }

            if (!player.isSneaking()) {
                this.end();
            }

        }, 1, 1L);
    }

    private void end() {
        this.loop.cancel();
    }

    private void completeAction(String direction) {
        if (actions.size() < NODS_REQUIRED) { // Don't increase past required size to prevent bloating
            actions.add(direction);
        }
        Block worshipBlock = null;
        if (actions.size() > NOD_THRESHOLD) {
            worshipBlock = worship();
        }
        if (actions.size() >= NODS_REQUIRED) {
            if (worshipBlock != null) {
                itemCheck(worshipBlock);
            }
        }
    }

    private void itemCheck(Block mainBlock) {

        BoundingBox box = BoundingBox.of(mainBlock).expand(1d); // 3x3x3 area

        Collection<Entity> entities = world.getNearbyEntities(box);

        for (Entity offering: entities) {
            if (offering instanceof Item) {

                final ItemStack itemStack = ((Item) offering).getItemStack();

                final Material material = itemStack.getType();
                final Set<String> allGroups = config.getConfigurationSection("prayer-result").getKeys(false);


                // Match this item to its group
                List<String> groupsContainingItem = new ArrayList<>();
                for (String group: allGroups) {
                    final List<String> items = config.getStringList("prayer-result." + group + ".items");
                    if (items.contains(material.toString())) {
                        groupsContainingItem.add(group);
                    }
                }
                String itemGroupName;
                if (groupsContainingItem.size() == 1) { // Item is in one group
                    itemGroupName = groupsContainingItem.get(0);
                } else if (groupsContainingItem.size() == 0) { // Item is in no groups
                    itemGroupName = "group-default";
                } else { // Item is in more than one group
                    int index = ThreadLocalRandom.current().nextInt(0, groupsContainingItem.size()-1);
                    itemGroupName = groupsContainingItem.get(index);
                }

                // Total the weights in this group
                final Set<String> itemGroupActions =  config.getConfigurationSection("prayer-result." + itemGroupName + ".actions").getKeys(false);
                int totalWeight = 0;
                for (String action: itemGroupActions) {
                    totalWeight = totalWeight + config.getInt("prayer-result." + itemGroupName + ".actions." + action + ".weight");
                }

                // Select weight
                int selectedWeight = ThreadLocalRandom.current().nextInt(1, totalWeight + 1);

                // Get action from weight
                Map<String, Integer> actionWeights = new HashMap<>();
                actionWeights.put("strong-bless", config.getInt("prayer-result." + itemGroupName + ".actions.strong-bless.weight"));
                actionWeights.put("weak-bless", config.getInt("prayer-result." + itemGroupName + ".actions.weak-bless.weight"));
                actionWeights.put("ignore", config.getInt("prayer-result." + itemGroupName + ".actions.ignore.weight"));
                actionWeights.put("weak-curse", config.getInt("prayer-result." + itemGroupName + ".actions.weak-curse.weight"));
                actionWeights.put("strong-curse", config.getInt("prayer-result." + itemGroupName + ".actions.strong-curse.weight"));

                int weigher = 0;
                String selectedAction = null;
                for (String action: actionWeights.keySet()) {
                    weigher = weigher + actionWeights.get(action);
                    if (selectedWeight <= weigher) {
                        selectedAction = action;
                        break;
                    }
                }

                // Execute outcomes
                List<String> outcomes = config.getStringList("prayer-result." + itemGroupName + ".actions." + selectedAction + ".outcomes");
                for (String outcomeString: outcomes) {

                    Outcome outcome = parseOutcome(outcomeString, player, (Item) offering);

                    if (outcome != null) {
                        outcome.execute();
                    } else {
                        System.out.println("Invalid outcome detected in config.yml: " + outcomeString + ". Error located in " + itemGroupName + " under " + selectedAction + ".");
                    }
                }

                // Effects
                world.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation(), 64, 0.5, 0.5, 0.5, 0.1);
                switch (selectedAction) {
                    case "strong-bless", "weak-bless":
                        final String blessMessage = ChatColor.translateAlternateColorCodes('&', config.getString("bless-message").replace("{player}", player.getName()));
                        if (Objects.requireNonNull(config.getString("enable-broadcast", "true")).equalsIgnoreCase("true")) {
                            Bukkit.broadcastMessage(blessMessage);
                        } else {
                            player.sendMessage(blessMessage);
                        }
                        world.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1, 0);
                        break;
                    case "strong-curse", "weak-curse":
                        final String curseMessage = ChatColor.translateAlternateColorCodes('&', config.getString("curse-message").replace("{player}", player.getName()));
                        if (Objects.requireNonNull(config.getString("enable-broadcast", "true")).equalsIgnoreCase("true")) {
                            Bukkit.broadcastMessage(curseMessage);
                        } else {
                            player.sendMessage(curseMessage);
                        }
                        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 0);
                        break;
                    default:
                        world.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1, 0);
                }

                // Consume 1 item
                final int itemAmount = ((Item) offering).getItemStack().getAmount();
                if (itemAmount == 1) {
                    offering.remove();
                }
                if (itemAmount > 1) {
                    ((Item) offering).getItemStack().setAmount(itemAmount-1);
                }

                this.end();
            }
        }
    }

//    private Block worshipOLD() {
//        final Location location = player.getEyeLocation();
//        final Vector locationVector = location.toVector();
//        Vector directionVector = location.getDirection();
//        directionVector.setY(0);
//        final BlockIterator blocks = new BlockIterator(world, locationVector, directionVector, 0, 5);
//
//        while (blocks.hasNext()) {
//            final Block mainWorshipBlock = blocks.next();
//
//            if (!mainWorshipBlock.isPassable()) { // If the block is not air, etc.
//                // Make list of blocks to worship and add main block
//                List<Block> blocksToWorship = new ArrayList<>();
//                blocksToWorship.add(mainWorshipBlock);
//
//                for (int i = 0; i < actions.size()-Math.floor(actions.size()/2F); ++i) { // Add more and more blocks to worship as actions.size() increases
//                    final Vector randVector = Vector.getRandom().subtract(new Vector(0.5d, 0.5d, 0.5d)); // Vector pointing in random xyz (negatives too)
//                    final Vector randBlockVector = randVector.multiply(5); // Scale up
//                    final Block randBlock = centerOf(mainWorshipBlock).add(randBlockVector).getBlock();
//                    blocksToWorship.add(randBlock);
//                }
//
//                world.playSound(centerOf(mainWorshipBlock), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 0);
//
//                for (Block thisBlock: blocksToWorship) { // At each secondary block, create particle effects
//                    if (!thisBlock.isPassable()) { // Check if not passable again since the randomly selected ones might be
//                        worshipBlock(world, thisBlock);
//                    }
//                }
//                return mainWorshipBlock;
//            }
//        }
//        return null;
//    }

    private static Location centerOf(Block block) {
        return block.getLocation().add(new Vector(0.5d, 0.5d, 0.5d));
    }

    private void effectWorship(Location location) {
        world.spawnParticle(Particle.FIREWORKS_SPARK, location, 3, 0.5, 0.5, 0.5, 0.05);
        world.spawnParticle(Particle.END_ROD, location, 3, 0.5, 0.5, 0.5, 0);
    }

    private Block targetWorshipBlock() {
        final Vector start = player.getEyeLocation().toVector();
        final Vector direction = player.getEyeLocation().getDirection();
        direction.setY(0);
        final BlockIterator blocks = new BlockIterator(world, start, direction, 0, 5);

        while (blocks.hasNext()) {
            final Block mainWorshipBlock = blocks.next();

            if (!mainWorshipBlock.isPassable()) {
                return mainWorshipBlock;
            }
        }
        return null;
    }

    private List<Block> getNearbyBlocks(Block mainWorshipBlock, int count, int range) {
        List<Block> nearby = new ArrayList<>();

        for (int i = 0; i < count; ++i) {
            final Vector randVector = Vector.getRandom().subtract(new Vector(0.5d, 0.5d, 0.5d)).multiply(2); // Shift down to include negative values, double to rescale to 1
            randVector.multiply(range);
            nearby.add(centerOf(mainWorshipBlock).add(randVector).getBlock());
        }
        return nearby;
    }

    private Block worship() {
        final Block mainBlock = targetWorshipBlock();
        List<Block> blocksToWorship = getNearbyBlocks(mainBlock, Math.min(PRAYER_INTENSITY, actions.size() - NOD_THRESHOLD), PRAYER_RANGE);

        if (mainBlock != null) {
            world.playSound(centerOf(mainBlock), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 0);

            for (Block block : blocksToWorship) {
                if (!block.isPassable()) {
                    effectWorship(centerOf(block));
                }
            }
            return mainBlock;
        }
        return null;
    }
}