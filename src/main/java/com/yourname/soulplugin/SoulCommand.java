package com.yourname.soulplugin;

import com.yourname.soulplugin.enums.SoulType;
import com.yourname.soulplugin.managers.SoulManager;
import com.yourname.soulplugin.utils.SoulItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class SoulCommand implements CommandExecutor {
    
    private final SoulPlugin plugin;
    private final SoulManager soulManager;
    
    public SoulCommand(SoulPlugin plugin, SoulManager soulManager) {
        this.plugin = plugin;
        this.soulManager = soulManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("soulplugin.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage("§e/souls reload §7- Reload the plugin configuration");
            sender.sendMessage("§e/souls give <player> <soul> §7- Give a soul to a player");
            sender.sendMessage("§e/souls list §7- List all soul types");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reloadConfig();
                sender.sendMessage("§aConfiguration reloaded!");
                break;
                
            case "give":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /souls give <player> <soul>");
                    return true;
                }
                
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }
                
                try {
                    SoulType soulType = SoulType.valueOf(args[2].toUpperCase());
                    NamespacedKey soulKey = new NamespacedKey(plugin, "soul_type");
                    ItemStack soulItem = SoulItemCreator.createSoulItem(soulType, soulKey);
                    
                    target.getInventory().addItem(soulItem);
                    sender.sendMessage("§aGave " + soulType.getDisplayName() + " §ato " + target.getName());
                    target.sendMessage("§aYou received a " + soulType.getDisplayName() + "§a!");
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cInvalid soul type! Use /souls list to see all types.");
                }
                break;
                
            case "list":
                sender.sendMessage("§eAvailable Soul Types:");
                for (SoulType soulType : SoulType.values()) {
                    sender.sendMessage("§7- " + soulType.getDisplayName() + " §7(" + 
                                     soulType.getRarity().getDisplayName() + "§7)");
                }
                break;
                
            default:
                sender.sendMessage("§cUnknown subcommand! Use /souls for help.");
                break;
        }
        
        return true;
    }
}