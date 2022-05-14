package fr.a6st.epuhc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

import fr.a6st.epuhc.commands.CommandClaim;
import fr.a6st.epuhc.commands.CommandFHeal;
import fr.a6st.epuhc.commands.CommandMypos;
import fr.a6st.epuhc.commands.CommandReveal;
import fr.a6st.epuhc.commands.CommandRevive;
import fr.a6st.epuhc.commands.CommandSTOP;
import fr.a6st.epuhc.commands.CommandTN;
import fr.a6st.epuhc.commands.CommandToggleBoard;
import fr.a6st.epuhc.commands.CommandUHC;
import fr.a6st.epuhc.kits.TaupeKit;
import fr.a6st.epuhc.listeners.DamageListeners;
import fr.a6st.epuhc.listeners.GlobalListeners;
import fr.a6st.epuhc.listeners.PlayerListeners;
import fr.a6st.epuhc.scoreboard.ScoreBoardCycle;
import fr.a6st.epuhc.scoreboard.ScoreboardSign;
import fr.a6st.epuhc.tablist.ActionBarCycle;
import fr.a6st.epuhc.tablist.TabListCycle;
import fr.a6st.epuhc.team.Team;
import fr.a6st.epuhc.title.Title;

//import fr.a6st.epuhc.listeners.DamageListeners;
//import fr.a6st.epuhc.listeners.PlayerListeners;


@SuppressWarnings("deprecation")
public class Main extends JavaPlugin{
	
	
	//=============== Partie privée ===================
	
	//Partie gestion global des joueurs
	private List<Player> players = new ArrayList<>(); //Liste des joueurs;
	private List<Player> morts = new ArrayList<>(); //Liste des joueurs morts
	private Map<UUID, List<Team>> deconnection = new HashMap<>(); //Liste des joueurs déconnectés
	
	//Partie gestion des teams
	private List<Team> team = new ArrayList<>(); //Liste des teams en vie
	private List<Team> deadTeam = new ArrayList<>(); //Listes des teams éliminées
	
	//Partie gesetion des states
	private State state; //State de la partie : WAITING, STARTING, FARMING, PVP, FINISH;

	//Mise en place de la worldBorder
	private WorldBorder wb;
	private WorldBorder nwb;
	
	//=============== Partie publique ===================
	
	//Partie gestion des scoreboards
	public Map<Player, ScoreboardSign> boards = new HashMap<>();
	public ScoreboardManager manager = Bukkit.getScoreboardManager();
	public Scoreboard tabBoard = manager.getNewScoreboard();
	
	//Partie gestion des titles
	public Title titles = new Title();
	
	//Partie gestion des des taupes
	public List<List<ItemStack>> taupeKits = new ArrayList<>(); //Liste des kits de taupe
	private List<Player> taupeReveal = new ArrayList<>(); //Liste des taupes reveal
	public List<Player> claimedPlayer = new ArrayList<>(); //Liste des joueurs ayant recupéré le kit taupe
	
	@Override
	public void onEnable() { //Le plugin se lance;
		saveDefaultConfig(); //Sauvegarde la configuration par default;
		setState(State.IDLE);
		wb = Bukkit.getWorld("world").getWorldBorder();
		nwb = Bukkit.getWorld("world_nether").getWorldBorder();
		getCommand("uhc").setExecutor(new CommandUHC(this));
		getCommand("stopuhc").setExecutor(new CommandSTOP(this));
		getCommand("tn").setExecutor(new CommandTN(this));
		getCommand("revive").setExecutor(new CommandRevive(this));
		getCommand("reveal").setExecutor(new CommandReveal(this));
		getCommand("claim").setExecutor(new CommandClaim(this));
		getCommand("fheal").setExecutor(new CommandFHeal());
		getCommand("pos").setExecutor(new CommandMypos(this));
		getCommand("toggleboard").setExecutor(new CommandToggleBoard(this));
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new GlobalListeners(this), this);
		pm.registerEvents(new PlayerListeners(this), this);
		pm.registerEvents(new DamageListeners(this), this);
		
		
		Bukkit.getWorld("world").setDifficulty(Difficulty.NORMAL);
		
		//Gestion des teams
		DyeColor[] dyeList = DyeColor.values(); //Creation d'une liste contenant toutes les couleurs de colorants
		String colorCodeList = "f65bead8795162c0"; //Code couleur basé sur la generation des teams
		for(int i = 0;i < dyeList.length; i++){
			String name  = "Team " + (i+1); //Donne un nom par defaut a la team, ici, la couleur
			DyeColor color = dyeList[i];
			char colorCode = colorCodeList.charAt(i);
			String tag = Integer.toString(i);
			//Ajout a la liste global de team
			team.add(new Team(name, tag, color, colorCode));
		}
		
		//Actualisation du scoreboard
		
		ScoreBoardCycle sbCycle = new ScoreBoardCycle(this);
		sbCycle.runTaskTimer(this, 0, 20);
		
		//Actualisation de la tablist 
		Objective tablistObjective = tabBoard.registerNewObjective("tabhealth", "health");
		tablistObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		Objective belowNameObjective = tabBoard.registerNewObjective("belowhealth", "health");
		belowNameObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
		belowNameObjective.setDisplayName("§c❤");
		
		TabListCycle tabList = new TabListCycle(this);
		tabList.runTaskTimer(this, 0, 100);
		
		//Actualisation de l'action bar
		ActionBarCycle actionBar = new ActionBarCycle(this);
		actionBar.runTaskTimer(this, 0, 10);
		
		//Ajout de la schematic a demarrage du serveur
		try {
			loadSchematic("/cage.schematic");
		} catch (Exception e) {
			System.out.println("Impossible de load la schematic");
			e.printStackTrace();
		}
		
		//Ajout des kits de taupes
		TaupeKit.CreateTaupeKit(taupeKits);
	}
	
	public void loadSchematic(String name) throws Exception
	{		
        	File dir = new File(this.getDataFolder() + File.separator + name); //Schematic a paste
    		Vector pastePos = new Vector(0,229,0); // Position du paste
    		Location loc = new Location(Bukkit.getWorld("world"), 1, 1, 1); //Creer une loc pour en recuprer le monde dans la session
    		
    		WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
    		EditSession session = we.getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(loc.getWorld()), -1); //Creer la session ou l'on pastera la schematic
    		
    		CuboidClipboard clipboard = MCEditSchematicFormat.getFormat(dir).load(dir);
    		clipboard.paste(session, pastePos, false);
        
	}
	
	public void setState(State state) { //Méthode setState pour changer le state en dehors de la class principale;
		this.state=state;
	}
	
	public boolean isState(State state) { //Test State en dehors de la class principale;
		return this.state==state;
	}
	
	public List<Player> getPlayers(){ //getPlayers en dehors de la class principale;
		return players;
	}
	
	public List<Player> getMorts(){ //getMorts en dehors de la class principale
		return morts;
	}
	
	public Map<UUID, List<Team>> getDeconnection(){ //getDeconnection en dehors de la class principale
		return deconnection;
	}
	
	public void addPlayer(Player player, Team team) //addPlayer en dehors de la class principale;
	{
		if(team.getPlayers().contains(player)) //Le joueur est déja dans l'equipe
		{
			player.sendMessage("§cVous appartenez déjà à cette équipe");
			return; //Fin de la methode
		}	
		//Si la team est complete:
		/*if(team.getSize() >= tailleMax)
		  {
		      player.sendMessage("§cLa team est complète");
		      return;
		  }*/
		 
		team.addPlayer(player);
		player.sendMessage("§aVous avez rejoint l'équipe " + team.getColorCode() + team.getName());
		//Changement de la couleur de la bannière dans l'inventaire
		BannerMeta meta = (BannerMeta)player.getInventory().getItem(0).getItemMeta(); //Recupere la meta de la bannière dans l'inventaire
		meta.setBaseColor(team.getColor());
		meta.setDisplayName("§6Vous êtes dans la team "+ team.getColorCode() + team.getName());
		player.getInventory().getItem(0).setItemMeta(meta); //Applique la nouvelle couleur a la bannière dans l'inventaire
		player.updateInventory(); //Update l'inventaire du joueur (pour eviter les soucis de visibilité)
	}
	
	public void removePlayer(Player player) //removePlayer en dehors de la class principale;
	{
		for(Team Team : team) //Check chaque team pour voir si le joueur est dedans
		{
			if(Team.getPlayers().contains(player)) //Si la team contient le joueur
			{
				Team.removePlayer(player); //On supprime le joueur
			}
		}
	}
	
	public void removeUnusedTeam() { //Suppression des equipes sans joueurs
		List<Team> tempTeam = new ArrayList<>();
		for(Team equipe : team) {
			if(equipe.getPlayers().size() == 0) { //Si la team ne participe pas
				tempTeam.add(equipe); //on ajoute la team des equipes a supprimer a une liste
			}
		}
		
		for(Team equipe : tempTeam) { //Pour toute les teams sans joueurs
			team.remove(equipe); //On les supprimes de la liste des equipes participantes
		}
	}
	
	public List<Team> getTeams() //getTeams en dehors de la class principale;
	{
		return team;
	}
	
	public List<Team> getDeadTeams() { //getDeadTeams en dehors de la class principale
		return deadTeam;
	}
	
	public List<Team> getPlayerTeam(Player player) { //getPlayerTeam en dehors de la class principale
		List<Team> playTeam = new ArrayList<>();
		for(Team equipe : team) {
			if(equipe.getPlayers().contains(player)) {
				playTeam.add(equipe);
			}
		}
		return playTeam;
	}
	
	public Team getTaupeTeam() { //getTaupeTeam en dehors de la class principale
		for(Team equipe : team) {
			if(equipe.getTag() == "Taupe") {
				return equipe;
			}
		}
		return null;
	}
	
	public List<Player> getTaupeReveal() { //getTaupeReveal en dehors de la classe principale
		return taupeReveal;
	}
	
	public WorldBorder getWorldBorder() { //getworldBorder en dehors de la class principale
		return wb;
	}
	
	public WorldBorder getNetherWorldBorder() { //getworldBorder en dehors de la class principale
		return nwb;
	}
	
	public int occurenceTeam(Player player) { //Compte le nombre d'apparition d'un joueur dans les teams
		int occurence =0;
		for(Team team : team) {
			if(team.getPlayers().contains(player)) {
				occurence +=1;
			}
		}
		return occurence;
	}
	
	public List<List<ItemStack>> getTaupeKits(){ //Liste des kits en dehors de la class principale
		return taupeKits;
	}
	
	public List<Player> getClaimedPlayer() { //Liste des joueurs ayant récuperer leur kit de taupe en dehors de la class principale
		return claimedPlayer;
	}

	public void eliminate(Player player) { //Sur élimination d'un joueur à cause d'une déconnection ou d'une mort;
		if(players.contains(player)) { //test si l'array contient toujours le joueur, pour ensuite l'enlever;
			players.remove(player); 
			for(Player gamer : Bukkit.getOnlinePlayers()) {
				gamer.playSound(gamer.getLocation(), Sound.AMBIENCE_THUNDER, 10, 1);
			}
		}
		morts.add(player);
		player.setGameMode(GameMode.SPECTATOR); //passe le joueur en gamemode spectateur;
		player.sendMessage(getConfig().getString("messages.death").replace("&", "§")); //envoie un message au joueur;
		
		for(Team equipe : getPlayerTeam(player)) { //Parcours les teams du joueur
			if(isTeamAlive(equipe) == false && !getDeadTeams().contains(equipe)) { //Si tout les membres de cette team sont éliminées / déconectés et que la team n'est pas déja éliminée
				Bukkit.broadcastMessage("§c[§6EP-UHC§c] - §bLa team "+ equipe.getColorCode() + equipe.getName() + " §best éliminé !");
				deadTeam.add(equipe); //On ajoute la team a la liste des equipes mortes
				for(Player gamer : Bukkit.getOnlinePlayers()) {
					gamer.playSound(gamer.getLocation(), Sound.WITHER_SPAWN, 10, 1);
				}
			}
		}
		ScoreBoardCycle.actualiseTeam(this);
		checkWin(); //exécute la méthode checkWin;
	}

	public boolean isTeamAlive(Team team) {
		boolean isPlaying = true;
		int occurence = 0;
		for(Player gamer : team.getPlayers()) {
			if(getMorts().contains(gamer) || getDeconnection().containsKey(gamer.getUniqueId())) {
				occurence += 1; //On compte le nombre joueur éliminés ou déconnecté
			}
		}
		if(occurence >= team.getPlayers().size() || deadTeam.contains(team)) { //Si le nombre de joueur éliminé est le meme que le nombre de personne dans la team
			isPlaying = false;
		}
		return isPlaying;
	}
	
	public void checkWin() { //test si le joueur gagne la partie;
		if(team.size() == deadTeam.size()+1 && !isState(State.FINISH)) { //si il ne reste qu'une seul team et que la game n'est pas déja fini
			Team winner = team.get(0); //Récupère une team par defaut (sera modifié pour être la team gagnante juste après)
			
			//Récupère la team gagnante
			for(Team teamAlive : team) {
				if(!deadTeam.contains(teamAlive)) { //Si la liste des teams morte ne contient pas une team, c'est que c'est l'équipe gagnante
					winner = teamAlive;
				}
			}
			Bukkit.broadcastMessage(getConfig().getString("messages.win").replace("&", "§").replace("{team}", winner.getColorCode() + winner.getName())); //Envoie d'un message;
			
			String joueurGagnants = "§7";
			for(Player gagnant : winner.getPlayers()) {
				joueurGagnants += gagnant.getName() + " ";
			}
			for(Player player : Bukkit.getOnlinePlayers()) {
				player.sendTitle("§bVictoire de l'équipe " + winner.getColorCode() + winner.getName() + "§b!", joueurGagnants);
			}
			setState(State.FINISH);
		}
	}

}
