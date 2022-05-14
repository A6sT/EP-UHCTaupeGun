package fr.a6st.epuhc.scoreboard;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.a6st.epuhc.Main;
import fr.a6st.epuhc.State;
import fr.a6st.epuhc.tasks.GameCycle;
import fr.a6st.epuhc.team.Team;

public class ScoreBoardCycle extends BukkitRunnable{

	
	//=============== Partie privée ===================
	
	
	private Main main; //La class principale;

	

	
	//=============== Partie publique ===================
	
	
	public ScoreBoardCycle(Main main) { //Pour récupérer le main de notre class principale;
		this.main=main;
	}

	
	
	@Override
	public void run(){
		for(Entry<Player, ScoreboardSign> sign : main.boards.entrySet()) {
			if(main.isState(State.IDLE)) {
				sign.getValue().setLine(14, "§e§lEn ligne§e: §a " + Bukkit.getOnlinePlayers().size() + "§7/50"); //Joueurs connectés
			}
			//Permet d'actualiser la ligne des timers (ligne qui change le + souvent)
			int timer = GameCycle.timer; //On recupère la valeur du timer de gameCycle
			String tempsRestant = convertTimer(timer); //Convertit timer en une durée affichable dans le scoreboard
			
			//Scoreboard lors de la phase d'invincibilité
			if(main.isState(State.GRACE)) {
				sign.getValue().setLine(13, "§aInvincibilité: §b" + tempsRestant);
				sign.getValue().setLine(14, "§aJoueurs restant: §e" + main.getPlayers().size()); //Joueurs en vie
			}
			
			//Scoreboard lors de la phase d'avant pvp
			if(main.isState(State.FARMING)) { 
				sign.getValue().setLine(13, "§cPvP: §b" + tempsRestant);
				sign.getValue().setLine(14, "§aJoueurs restant: §e" + main.getPlayers().size()); //Joueurs en vie
			}
			
			//Scoreboard lors de la phase de pvp
			if(main.isState(State.PVP)) {
				sign.getValue().setLine(13, "§eBordure: §b" + tempsRestant);
				sign.getValue().setLine(14, "§aJoueurs restant: §e" + main.getPlayers().size()); //Joueurs en vie
			}
			
			//Scoreboard lors de la réduction de la bordure
			if(main.isState(State.BORDER)) {
				sign.getValue().setLine(13, "§eBordure: §b±" + (int)main.getWorldBorder().getSize()/2); //Affiche la vrai taille de la bordure
				sign.getValue().setLine(14, "§aJoueurs restant: §e" + main.getPlayers().size()); //Joueurs en vie
			}
		}
	}

	public static void createScoreBoard(Player player, Map<Player, ScoreboardSign> boards) {
		ScoreboardSign sb = new ScoreboardSign(player, "§6§lEP-UHC");
        sb.create();
        
        //Lignes par defaut (ne changent pas de la game)
        sb.setLine(0, "§a "); //Ligne d'aération prédefinit
        sb.setLine(1, "§6§lUHC§6: §bTaupeGun");
        sb.setLine(2, "§b "); //Ligne d'aération prédefinit
        
        /* Lignes pas par defaut
        //On alloue le plus de lignes possible pour les team (si ce sont les memes lignes, elles ne sont pas affiché) (autrement dit, si elles restent vides)
        sb.setLine(3, ""); //Premiere utilisation: le joueur rejoint une team : nom de la team
        sb.setLine(4, ""); //Premiere utilisation: le joueur rejoint une team : Leader de la team
        sb.setLine(5, ""); //Premiere utilisation: le joueur rejoint une team : 2eme joueur de la team
        sb.setLine(6, ""); //Premiere utilisation: le joueur rejoint une team : 3eme joueur de la team
        sb.setLine(7, ""); //Premiere utilisation: le joueur rejoint une team : 4eme joueur de la team (/!\ ne pas oublier de check lors d'un changement de team)
        sb.setLine(8, ""); //Premiere utilisation: le joueur rejoint une team : 5eme joueur de la team
        sb.setLine(9, ""); //Premiere utilisation: le joueur rejoint une team : 5eme joueur de la team
        sb.setLine(10, ""); //Premiere utilisation: le joueur rejoint une team : 6eme joueur de la team
        sb.setLine(11, ""); //Premiere utilisation: le joueur rejoint une team : 7eme joueur de la team
        
        sb.setLine(12, "§c "); //Premiere utilisation: le joueur rejoint une team : ligne d'aeration
        
        //Autres lignes (changent beaucoup en fonction de la periode du jeu)
        //Lignes par defaut = en State IDLE
        sb.setLine(13, ""); //Premiere utilisation: GRACE : temps restant
        */
        sb.setLine(14, "§e§lEn ligne§e: §a " + Bukkit.getOnlinePlayers().size() + "§7/30"); //Par defaut: joueurs connectés
        
        //On ajoute le scoreboard au joueur
        boards.put(player, sb);
		
	}

	private String convertTimer(int timer) {
		String tempsRestant = "";
		int h = timer / 3600;
		if(h != 0) {
			tempsRestant += h+"h";
			timer -= h*3600;
		}
		int m = timer / 60;
		if((m != 0) || (m == 0 && h>0)) { //Lorsqu'il reste 1h, on affichera quand meme les minutes (1h00m01 sec par exemple)
			if(m <10) {
				tempsRestant +="0";
			}
			tempsRestant += m+":";
		}
		int s = timer % 60;
		if(s < 10) {
			tempsRestant +="0";
		}
		tempsRestant += s+"s";
		return tempsRestant;
	}

	public static void actualiseTeam(Main main) { //Actualise la partie team du scoreboard
		for(Team team : main.getTeams()) {
			//Actualise les joueurs de la team
			for(Player playerboard : team.getPlayers()) {
				if(team.getTag() != "Taupe" || main.getTaupeReveal().contains(playerboard)) { //Empeche d'afficher la team des taupes a un joueur non reveal
					if(main.boards.containsKey(playerboard)) {
						//Supprime les joueurs
						for(int i = 4; i<12; i++) {
							main.boards.get(playerboard).setLine(i, "§t "); //Annule l'affichage de la ligne (car la ligne 1 contient déja cette valeur)
						}
						
						//Affiche les joueurs de la team du playerboard
						for(int i = 0; i<team.getPlayers().size(); i++) {
							String offline = "";
							Player gamer = team.getPlayers().get(i);
							if(main.getDeconnection().containsKey(gamer.getUniqueId()) || main.getMorts().contains(gamer)){
								offline = "§m";
							}
							main.boards.get(playerboard).setLine(4+i, " §7- "+ offline + gamer.getDisplayName());
						}
					}	
					//Actualise le nom de la team et ajoute l'aeration après les membres de la team
					for(Player joueur : team.getPlayers()) {
						if(main.boards.containsKey(joueur)) {
							main.boards.get(joueur).setLine(3, "§6Equipe: " + team.getColorCode()+team.getName() + " §7("+ team.getTeamKill() + ")"); //Nom de la team
							main.boards.get(joueur).setLine(12, "§t ");
						}
							
					}
				}
			}	
		}
	}
}

