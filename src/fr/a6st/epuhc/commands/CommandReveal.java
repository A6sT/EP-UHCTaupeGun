package fr.a6st.epuhc.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.a6st.epuhc.Main;
import fr.a6st.epuhc.scoreboard.ScoreBoardCycle;
import fr.a6st.epuhc.team.Team;

public class CommandReveal implements CommandExecutor {
	
private Main main;
	
	public CommandReveal(Main main) {
		this.main = main;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		for(Team team : main.getTeams()) {
			if(team.getTag() == "Taupe") { //Si la team analysé est celle des taupes
				if(team.getPlayers().contains(player)) { //Si le joueur appartient a cette team
					if(!main.getMorts().contains(player)) {
						for(Team equipe : main.getTeams()) { //On enleve le joueur de sa team initiale
							if(equipe.getPlayers().contains(player) && equipe.getTag() != "Taupe") { //On empeche aussi d'enlever le joueur de la team des taupes
								equipe.removePlayer(player);
								main.getTaupeReveal().add(player); //Ajoute le joueur a la liste des taupes reveals
								//Verifie si la team est encore en jeu
								if(main.isTeamAlive(equipe) == false && !main.getDeadTeams().contains(equipe)) { //Si tout les membres de la team sont éliminées / déconectés et que la team n'est pas déja éliminée
									Bukkit.broadcastMessage("§c[§6EP-UHC§c] - §bLa team "+ equipe.getColorCode() + equipe.getName() + " §best éliminé !");
									main.getDeadTeams().add(equipe); //On supprime la team des equipes participantes
									main.checkWin(); //Verifie si la game est terminée
								}
								
								for(Player gamer : main.getPlayers()) {
									gamer.playSound(gamer.getLocation(), Sound.WOLF_HOWL, 10, 1);
								}
								Bukkit.broadcastMessage("§c[§6EP-UHC§c] - §9"+ player.getDisplayName()+" §bétait en faite une taupe...");
								player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
								ScoreBoardCycle.actualiseTeam(main);
								return false; //Pas besoin de faire d'autres action, on economise donc du temps en arretant le processus ici
							}
						}
					} else {
						player.sendMessage("§cVous ne pouvez pas vous reveal en étant mort");
					}
				} else {
					player.sendMessage("§cVous n'êtes pas une taupe");
				}
					
			}
		}
		return false;
	}

}
