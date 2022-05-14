package fr.a6st.epuhc.tasks;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.a6st.epuhc.Main;
import fr.a6st.epuhc.State;
import fr.a6st.epuhc.scoreboard.ScoreBoardCycle;
import fr.a6st.epuhc.team.Team;

public class GameCycle extends BukkitRunnable{

	
	//=============== Partie privée ===================
	
	
	private Main main; //La class principale;

	

	
	//=============== Partie publique ===================
	
	
	public GameCycle(Main main) { //Pour récupérer le main de notre class principale;
		this.main=main;
		this.startFarmingTimer=true;
		this.startGracePeriod = true;
		this.startPvPTimer = true;
		this.startBorderReduction = true;
	}
	
	public static int timer; //Timer
	public boolean startGracePeriod = true;
	public boolean startFarmingTimer = true;
	public boolean startPvPTimer = true;
	public boolean startBorderReduction = true;
	
	
	
	@Override
	public void run() { //Au lancement de la partie;
		if(main.isState(State.GRACE)) {
			if(startGracePeriod) { //Au debut de la grace period
				startGracePeriod = false;
				timer=60;
				Bukkit.getWorld("world").setFullTime(1000); //On met le jour
				for(Player player : main.getPlayers()) {
					player.getInventory().clear();
					player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*60, 255)); //Ajoute resistance 255 a chaque joueur pendant 60 secondes (60*20 ticks)
				}
			}
			//Rappel dans le chat avant la prochaine periode
			if(timer == 30 || timer == 15|| timer <=5 && timer > 0) {
				for(Player player : main.getPlayers()) {
					player.playSound(player.getLocation(), Sound.CLICK, 10, 1);
				}
				if(timer == 30) { //Envoies des messages dans le chat (pour eviter le spam)
					Bukkit.broadcastMessage(main.getConfig().getString("messages.GraceReminder").replace("&", "§").replace("{timer}", Integer.toString(timer)));
				}
			}
			//Changement de State: Plus d'invicibilité
			if(timer ==0) {
				Bukkit.broadcastMessage(main.getConfig().getString("messages.GraceEnd").replace("&", "§"));//Message pour la fin de la grace period
				main.setState(State.FARMING); //Passage en farming
			}
			timer --;
		}
		//Durant la periode de farming
		if(main.isState(State.FARMING)) {
			if(startFarmingTimer) { //si on doit lancer le pvp;
				startFarmingTimer=false;
				timer = main.getConfig().getInt("timers.pvp"); //Recupère le timer de la config
			}
			
			//Rappel dans le chat avant la prochaine periode 
			if(timer == 3600 || timer == 1800 || timer == 900 || timer == 300) {// En minutes
				Bukkit.broadcastMessage(main.getConfig().getString("messages.FarmReminder").replace("&", "§").replace("{timer}", Integer.toString(timer/60)));
			} 
			if(timer == 60 || (timer <= 10 && timer > 0)) { // En secondes
				for(Player player : main.getPlayers()) {
					player.playSound(player.getLocation(), Sound.CLICK, 10, 1);
				}
			}
			
			//Changement de State: Le pvp s'active et les taupes sont annoncés
			if(timer==0) { 
				//Annonce des taupes
				Team taupe = new Team("Taupe", "Taupe", 'c'); //Creation de la team des taupes
				main.getTeams().add(taupe); //Ajoute a la liste principale
				//Selection d'un joueur au hasard dans chaque team a ajouter dans la team des taupes
				Random rnd = new Random();
				int joueurChoisi;
				for(Team team : main.getTeams()) {
					if(team.getPlayers().size() > 0 && team.getTag() != "Taupe") { //Pour toutes les teams contenant des joueurs
						joueurChoisi = rnd.nextInt(team.getPlayers().size());
						System.out.println("Numero: " + joueurChoisi);
						Player gamer = team.getPlayers().get(joueurChoisi);
						main.titles.sendTitle(gamer, taupe.getColorCode()+ "Vous êtes la taupe", "§7Gagnez avec les taupes", 20);
						gamer.sendMessage("§cVous êtes la taupe. Votre objectif est de gagner avec la team des taupes. Vous allez devoir trahir votre équipe, des sacrifices seront probablement nécessaires... Bonne chance !");
						taupe.addPlayer(gamer);
					}
				}
				for(Player player : main.getPlayers()) {
					player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 10, 1);
				}
				Bukkit.broadcastMessage("§c[§6EP-UHC§c] - §bLes taupes ont été séléctionnées");
				ScoreBoardCycle.actualiseTeam(main);
				
				//Activation du pvp
				Bukkit.broadcastMessage(main.getConfig().getString("messages.pvp").replace("&", "§")); //message pvp on;
				Bukkit.getWorld("world").setPVP(true); //On start le pvp après le gamecycle;
				main.setState(State.PVP);
			}
			timer --; //update timer;
		}
		
		if(main.isState(State.PVP)) {
			if(startPvPTimer) {
				startPvPTimer=false;
				timer = main.getConfig().getInt("timers.timerBeforeBorder"); //Recupère le timer de la config
			}
			
			//Rappel dans le chat avant la prochaine periode 
			if(timer == 3600 || timer == 1800 || timer == 900 || timer == 300) {// En minutes
				Bukkit.broadcastMessage(main.getConfig().getString("messages.borderReminder").replace("&", "§").replace("{timer}", Integer.toString(timer/60)));
			} 
			if(timer == 60 || (timer <= 10 && timer > 0)) { // En secondes
				for(Player player : main.getPlayers()) {
					player.playSound(player.getLocation(), Sound.CLICK, 10, 1);
				}
			}
			
			if(timer==0) { 
				Bukkit.broadcastMessage(main.getConfig().getString("messages.borderReduction").replace("&", "§")); //message pvp on;
				Bukkit.getWorld("world").setPVP(true); //On start le pvp après le gamecycle;
				main.setState(State.BORDER);
			}
			timer --; //update timer;
		}
		
		//Lors de la phase de reduction de la bordure
		if(main.isState(State.BORDER)) {
			if(startBorderReduction) {
				startBorderReduction = false;
				main.getWorldBorder().setSize(100.0, main.getConfig().getInt("timers.borderReductionTime"));
				main.getNetherWorldBorder().setSize(10, (int)main.getConfig().getInt("timers.borderReductionTime"));
			}
		}
	}
}
