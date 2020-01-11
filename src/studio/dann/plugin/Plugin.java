package studio.dann.plugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import studio.dann.chattools.Chat;

import java.util.logging.Level;

/**
 * This is the main class of this plugin.
 * This class is responsible for instantiating and tracking all the core instances.
 *
 * @author Dan Negura (contact.dann@icloud.com, https://spigot.dann.studio/)
 */
public class Plugin extends JavaPlugin {

    /** This method initializes this plugin's main instances. */
    @Override
    public void onEnable() {
        Chat chat = new Chat("CT", ChatColor.RED, ChatColor.GRAY);

        // Register commands.
        CommandExecutor executor = new TransferCommand(this, chat);
        PluginCommand selectCommand = getServer().getPluginCommand("select");
        PluginCommand transferCommand = getServer().getPluginCommand("transferfrom");
        if (selectCommand == null || transferCommand == null) {
            getLogger().log(Level.SEVERE, "Couldn't retrieve the plugin's commands.");
            return;
        }
        selectCommand.setExecutor(executor);
        transferCommand.setExecutor(executor);
    }

}
