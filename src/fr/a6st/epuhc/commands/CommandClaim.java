package fr.a6st.epuhc.commands;

import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.a6st.epuhc.Main;
import fr.a6st.epuhc.team.Team;

public class CommandClaim implements CommandExecutor {

private Main main;
private Random rnd = new Random();

	public CommandClaim(Main main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		Team taupe = main.getTaupeTeam();
		if(taupe != null) { // Si les taupes ont �t� selectionn�es
			if(main.getTaupeTeam().getPlayers().contains(player)) { //Si le joueur est une taupe
				if(!main.getClaimedPlayer().contains(player)) { //Si il n'a pas claim son kit
					if(main.getTaupeKits().size() > 0) { 
						int posKit = rnd.nextInt(main.getTaupeKits().size()); //Recup�re la position dans la liste du prochain kit (de mani�re al�atoire)
						List<ItemStack> kit = main.getTaupeKits().get(posKit);
						for(int i=0; i< kit.size(); i++) { //On give tout les items du kit au joueur
							player.getInventory().addItem(kit.get(i)); //Si le joueur est full, le reste sera jet� au sol
						}
						player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1));
						main.getTaupeKits().remove(kit); //Supprime le kit pour empecher une autre taupe de le recevoir � nouveau
						main.claimedPlayer.add(player); //On empeche le joueur de reprendre un kit
						player.sendMessage("�aVous avez obtenu votre kit");
					} else {
						player.sendMessage("�cTout les kits ont �t� claims");
					}
				} else {
					player.sendMessage("�cVous avez d�j� claim votre kit");
				}
			} else {
				player.sendMessage("�cVous n'�tes pas une taupe");
			}
		} else {
			player.sendMessage("�cLes taupes n'ont pas encore �t� selectionn�es");
		}
		return false;
	}

}
