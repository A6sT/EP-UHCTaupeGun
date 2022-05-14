package fr.a6st.epuhc.team;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;

public class Team {
	private String name; //A ne pas confondre avec le tag, name affiche un nom de team ("non officiel")
	private String tag; //Permet de reconnaitre la team par une chaine de caractère (peut etre un numéro ou un nom)
	private DyeColor color; //Associe une couleur a la team (pour les scoreboard)
	private String colorCode; //Color code de la team (pour l'affichage en couleur)
	private int teamKillCount; //Compteur de kill d'une team
	private List<Player> players = new ArrayList<>();
	
	public Team(String name, String tag, DyeColor color, char colorCode) //Constructeur de team classique
	{
		this.name = name;
		this.tag = tag;
		this.color = color;
		this.colorCode = "§" + colorCode;
		this.teamKillCount = 0;
	}
	
	public Team(String name, String tag, char colorCode) { //Constructeur de team (pour la team des taupes par exemple)
		this.name = name;
		this.tag = tag;
		this.colorCode = "§" + colorCode;
		this.teamKillCount = 0;
	}

	public String getName()
	{
		return this.name;
	}
	
	public void setName(String newName) //Changer le nom de la team
	{
		this.name = newName;
	}
	
	public String getTag()
	{
		return this.tag;
	}
	
	public DyeColor getColor()
	{
		return this.color;
	}
	
	public String getColorCode()
	{
		return this.colorCode;
	}
	
	public int getTeamKill() {
		return teamKillCount;
	}
	
	public List<Player> getPlayers()
	{
		return players;
	}
	
	public Player getFirstPlayer()
	{
		if(players.size() >0)
		{
			return players.get(0);
		}
		else
		{
			return null;
		}
	}
	
	public int getSize()
	{
		return players.size();
	}
	
	public void addPlayer(Player player)
	{
		players.add(player);
	}
	
	public void removePlayer(Player player)
	{
		players.remove(player);
	}
	
	public void addKill() {
		this.teamKillCount += 1;
	}
}
