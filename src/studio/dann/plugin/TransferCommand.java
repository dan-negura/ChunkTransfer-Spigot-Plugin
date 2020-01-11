package studio.dann.plugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Executor for the select and transfer commands.
 *
 * @author Dan Negura (contact.dann@icloud.com, https://spigot.dann.studio/)
 */
public class TransferCommand implements CommandExecutor {

    /** The plugin's main instance. */
    private Plugin plugin;

    /** Plugin's chat manager. */
    private Chat chat;

    /** Holds the players and the points they selected. */
    private Map<UUID, Location[]> playerSelectionMap;

    /** Initializes a command with a chat. */
    public TransferCommand(Plugin plugin, Chat chat) {
        if (chat == null || plugin == null)
            throw new NullPointerException();
        this.chat = chat;
        this.plugin = plugin;
        this.playerSelectionMap = new HashMap<>();
    }

    /**
     * Responds to /select command and /transferfrom command.
     * Usages: /select [a/b], where 'a' and 'b' are points defined by the player's location.
     *         /transferfrom [world], where world is the name of the world to transfer from.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender == null || command == null || label == null || args == null)
            throw new NullPointerException();
        if (!(sender instanceof Player))
            return false;
        Player player = (Player)sender;

        // Call the right method for this command.
        if (label.equalsIgnoreCase("select"))
            selectCommand(player, args);
        else if (label.equalsIgnoreCase("transferfrom"))
            transferFromCommand(player, args);
        return true;
    }

    /** Executes the transfer command. */
    private void transferFromCommand(Player player, String[] args) {
        if (player == null || args == null)
            throw new NullPointerException();
        final String USAGE_TRANSFER = "Usage: /transferfrom [sourceWorld]";
        final String NO_REFINED_REGION = "You need to select a region first using /select [a/b]";

        // Player doesn't have region yet.
        if (!playerSelectionMap.containsKey(player.getUniqueId())) {
            chat.toPlayer(player, NO_REFINED_REGION);
            return;
        }

        // Get player selection and verify.
        Location[] points = playerSelectionMap.get(player.getUniqueId());
        if (points[0] == null || points[1] == null) {
            chat.toPlayer(player, "Both selection points a/b must be defined.");
            return;
        }
        // No argument.
        if (args.length < 1) {
            chat.toPlayer(player, USAGE_TRANSFER);
            return;
        }
        // Retrieve worlds.
        World sourceWorld = Bukkit.getWorld(args[0]);
        if (sourceWorld == null) {
            chat.toPlayer(player, "World " + args[0] + " doesn't exist.");
            return;
        }
        World destinationWorld = player.getLocation().getWorld();
        if (destinationWorld == null) // unlikely to happen
            return;

        // Perform copy/paste.
        transfer(sourceWorld, destinationWorld, points[0].getChunk().getX(), points[0].getChunk().getZ(),
                points[1].getChunk().getX(), points[1].getChunk().getZ());

        // Notify player.
        chat.toPlayer(player, "Started region transfer. This will take ~0.25 seconds per chunk.");
    }

    /** Registers the position to the provided slot. */
    private void selectCommand(Player player, String[] args) {
        if (player == null || args == null)
            throw new NullPointerException();
        final String[] USAGE_SELECT = new String[] {"Usage: /select [a/b]", "Where 'a' and 'b' are the first and second selection points which will " +
                "define the selected region."};

        // No argument.
        if (args.length < 1) {
            chat.toPlayer(player, USAGE_SELECT);
            return;
        }
        // Set point a.
        if (args[0].equalsIgnoreCase("a")) {
            Location a = player.getLocation();
            if (playerSelectionMap.containsKey(player.getUniqueId()))
                playerSelectionMap.get(player.getUniqueId())[0] = a;
            else
                playerSelectionMap.put(player.getUniqueId(), new Location[] {a, null});
            chat.toPlayer(player, "Point 'a' was set here.");
        }
        // Set point b.
        else if (args[0].equalsIgnoreCase("b")) {
            Location b = player.getLocation();
            if (playerSelectionMap.containsKey(player.getUniqueId()))
                playerSelectionMap.get(player.getUniqueId())[1] = b;
            else
                playerSelectionMap.put(player.getUniqueId(), new Location[]{null, b});
            chat.toPlayer(player, "Point 'b' was set here.");
        }
        // Another letter.
        else
            chat.toPlayer(player, USAGE_SELECT);
    }

    /**
     * Creates a chunk transfer provided with a source world, a destination world, and two chunk coordinates.
     * @param source get chunks from
     * @param destination copy chunks to
     * @param fromChunkX corner chunk 1
     * @param fromChunkZ corner chunk 1
     * @param toChunkX corner chunk 2
     * @param toChunkZ corner chunk 2
     */
    public void transfer(World source, World destination, int fromChunkX, int fromChunkZ, int toChunkX, int toChunkZ) {
        if (source == null || destination == null)
            throw new NullPointerException();

        // Retrieve absolute corners.
        int minX = Math.min(fromChunkX, toChunkX);
        int maxX = Math.max(fromChunkX, toChunkX);
        int minZ = Math.min(fromChunkZ, toChunkZ);
        int maxZ = Math.max(fromChunkZ, toChunkZ);

        // Create queue of chunk pairs containing source and destination.
        LinkedList<Chunk[]> chunkQueue = new LinkedList<>();
        for (int x = minX; x <= maxX; x++)
            for (int z = minZ; z <= maxZ; z++) {
                Chunk sourceChunk = source.getChunkAt(x, z);
                Chunk destinationChunk = destination.getChunkAt(x, z);
                chunkQueue.add(new Chunk[] {sourceChunk, destinationChunk});
            }

        // Run a runnable that takes from the queue and transfers the blocks on 1 second interval.
        final int TICKS_BETWEEN_CHUNK = 10;
        final LinkedList<Integer> taskIdContainer = new LinkedList<>();
        BukkitTask taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (chunkQueue.isEmpty()) {
                Bukkit.getScheduler().cancelTask(taskIdContainer.getFirst());
                return;
            }
            Chunk[] chunkPair = chunkQueue.removeFirst();
            ChunkSnapshot snapshot = chunkPair[0].getChunkSnapshot(true, true, true);
            pasteSnapshotToChunk(chunkPair[1], snapshot);

        }, 0L, TICKS_BETWEEN_CHUNK);
        taskIdContainer.add(taskId.getTaskId());

    }

    /** Paste all the blocks from provided snapshot to the provided chunk. */
    public static void pasteSnapshotToChunk(Chunk chunk, ChunkSnapshot snapshot) {
        if (chunk == null || snapshot == null)
            throw new NullPointerException();
        boolean legacyBiomes = Bukkit.getBukkitVersion().contains("1.14");

        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++) {
                if (legacyBiomes) {
                    Block block = chunk.getBlock(x, 0, z);
                    block.setBiome(snapshot.getBiome(x, z));
                }
                for (int y = 0; y < 256; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    block.setBlockData(snapshot.getBlockData(x, y, z));
                    if (!legacyBiomes)
                        block.setBiome(snapshot.getBiome(x, y, z));
                }
            }
    }
}
