package fr.a6st.epuhc.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.a6st.epuhc.Main;
import fr.a6st.epuhc.State;

public class CommandSTOP implements CommandExecutor {
	
	
	private Main main;
	
	public CommandSTOP(Main main) {
		this.main=main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(main.isState(State.IDLE)) {
			sender.sendMessage(main.getConfig().getString("messages.stopped"));
			return false;
		} else {
			main.setState(State.IDLE);
			for(Player pls : Bukkit.getOnlinePlayers()) {
				Location spawn = new Location(Bukkit.getWorld("world"), 0.450, 253.5 , 0.450, 180, 0); //On créer une variable spawn pour pouvoir y tp le joueur;
				pls.teleport(spawn); //On téléporte le joueur au spawn;
			};
			Bukkit.getWorld("world").setPVP(false);
			return false;
		}
	}
}
