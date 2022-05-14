package fr.a6st.epuhc.tablist;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.a6st.epuhc.Main;
import fr.a6st.epuhc.State;
import fr.a6st.epuhc.team.Team;

public class ActionBarCycle extends BukkitRunnable {

	
	//=============== Partie priv�e ===================
	
	
	private Main main; //La class principale;
	
	//=============== Partie publique ===================
	
	public ActionBarCycle(Main main) { //Pour r�cup�rer le main de notre class principale;
		this.main = main;
	}
	

	@Override
	public void run() {
		if(!main.isState(State.IDLE)) {
			for(Team team : main.getTeams()) {
				int deadPlayerPerTeam = 0; //Reinitialise le compteur de mort / team a chaque nouvelle team
				
				//premiere boucle: on compte le nombre de mort / team
				for(Player player : team.getPlayers()) {
					if(main.getMorts().contains(player) || main.getDeconnection().containsKey(player.getUniqueId())) {
						deadPlayerPerTeam += 1;
					}
				}
				
				//Seconde boucle: on affiche l'action bar au joueur
				for(Player player : team.getPlayers()) {
					if(!main.getMorts().contains(player) && !main.getDeconnection().containsKey(player.getUniqueId())) { //Compte le nombre de joueur mort
						if(team.getTag() != "Taupe" || main.getTaupeReveal().contains(player)) { //Empeche de tracker le joueur en tant que membre des taupes tant qu'il ne s'est pas reveal
							if(team.getPlayers().size() - deadPlayerPerTeam > 1) { //Il reste au moin 2 joueurs dans la team
								if(getNearestTeamate(player, team) != null) { //Check si le tracker pointe vers un autre monde
									main.titles.sendActionBar(player, "�7Co�quipier le plus proche: �e" + getNearestTeamate(player, team) + "�7m");
								} else {
									main.titles.sendActionBar(player, "�cVotre co�quipier le plus proche est dans un autre monde");
								}	
							} else {
								main.titles.sendActionBar(player, "�cVous �tes le dernier survivant de votre team");
							}
						} 
					}
				}
			}
		}
	}


	private String getNearestTeamate(Player player, Team team) {
		Location playerLoc = player.getLocation();
		int lowestDistance = 1500; //On initialise la lowestDistance avec la plus grande valeur possible pour eviter des problemes
		String retour = "";
		
		for(Player gamer : team.getPlayers()) {
			if(!gamer.equals(player)) { //On ne doit pas tracker le joueur en question
				if(!(main.getMorts().contains(gamer) && main.getDeconnection().containsKey(gamer.getUniqueId()))) { //le joueur a track doit encore �tre en jeu
					if(team.getTag() != "Taupe" || main.getTaupeReveal().contains(player)) { //Le joueur est une taupe non reveal
						if(playerLoc.getWorld() == gamer.getLocation().getWorld()) {
							if(playerLoc.distance(gamer.getLocation()) < lowestDistance) {
								lowestDistance = (int) playerLoc.distance(gamer.getLocation());
							}
						} else {
							retour = "issue";
						}
						
					}
				}
			}
		}
		if(retour == "issue" && lowestDistance == 1500) { //Si le tracker n'as pas �t� modifi� (autrement dit: si la seul valeur trouv� provient d'un joueur d'une autre dimension
			return null;
		} else { //Cas par defaut
			return Integer.toString(lowestDistance);
		}
	}
}
