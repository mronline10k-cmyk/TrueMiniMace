package com.minimace.mace;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MiniMaceCommand implements CommandExecutor {

    private final TrueMiniMace plugin;

    public MiniMaceCommand(TrueMiniMace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("minimace.give")) {
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }

        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cUsage: /minimace <player>");
                return true;
            }
            target = (Player) sender;
        } else {
            target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[0]);
                return true;
            }
        }

        target.getInventory().addItem(plugin.getItemManager().create());
        sender.sendMessage("§aGave MiniMace to §f" + target.getName());
        if (target != sender) {
            target.sendMessage("§aYou received a §4§lMini Mace§a!");
        }

        return true;
    }
}
