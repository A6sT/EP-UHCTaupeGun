package fr.a6st.epuhc.listeners;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import fr.a6st.epuhc.Main;
import fr.a6st.epuhc.State;

public class GlobalListeners implements Listener {

	
	//=============== Partie priv�e ===================
	
	
	private Main main; //La class principale;

	

	
	//=============== Partie publique ===================
	
	
	public GlobalListeners(Main main) { //Pour r�cup�rer le main de notre class principale;
		this.main = main;
	}
		
	@EventHandler
	public void onSpawn(EntitySpawnEvent event) { //Empecher le spawn durant la phase d'idle
		if(main.isState(State.IDLE)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) { //Desactiver la pluie
		if(event.toWeatherState()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBrew(BrewEvent event) { //Desactiver les potions de force
		if(event.getContents().getIngredient().getType() == Material.BLAZE_POWDER) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onRegen(EntityRegainHealthEvent event) { //Empecher le regain de vie
		if(event.getRegainReason() == RegainReason.SATIATED) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEnchant(EnchantItemEvent event) { //Empecher certains enchantements
		Map<Enchantment, Integer> enchantList = event.getEnchantsToAdd();
		if(enchantList.containsKey(Enchantment.FIRE_ASPECT)) {
			enchantList.remove(Enchantment.FIRE_ASPECT);
			event.getEnchanter().sendMessage("�aL'enchantement Fire Aspect a �t� supprim� de votre item");
		}
		
		//Pour les �p�e en diamants, sharp 3 au max
		if(event.getItem().getType() == Material.DIAMOND_SWORD && enchantList.containsKey(Enchantment.DAMAGE_ALL)) {
			if(enchantList.get(Enchantment.DAMAGE_ALL) > 3) { //Si on d�passe sharp 3
				enchantList.remove(Enchantment.DAMAGE_ALL);
				enchantList.put(Enchantment.DAMAGE_ALL, 3); //Set au lvl 3
				event.getEnchanter().sendMessage("�aL'�p�e en diamant est limit� � sharpness 3, l'enchantement s'est r�gul� au niveau maximal autoris�");
			}
		}
		
		//Pour les �p�e en fer, sharp 4 au max
		if(event.getItem().getType() == Material.IRON_SWORD && enchantList.containsKey(Enchantment.DAMAGE_ALL)) {
			if(enchantList.get(Enchantment.DAMAGE_ALL) > 4) { //Si on d�passe sharp 4
				enchantList.remove(Enchantment.DAMAGE_ALL);
				enchantList.put(Enchantment.DAMAGE_ALL, 4); //Set au lvl 4
				event.getEnchanter().sendMessage("�aL'�p�e en fer est limit� � sharpness 4, l'enchantement s'est r�gul� au niveau maximal autoris�");
			}
		}
		
		//Pour les arcs, power 4 au max
		if(event.getItem().getType() == Material.BOW && enchantList.containsKey(Enchantment.ARROW_DAMAGE)) {
			if(enchantList.get(Enchantment.ARROW_DAMAGE) > 4) { //Si on d�passe power 4
				enchantList.remove(Enchantment.ARROW_DAMAGE);
				enchantList.put(Enchantment.ARROW_DAMAGE, 4); //Set au lvl 4
				event.getEnchanter().sendMessage("�aL'arc est limit� � 4, l'enchantement s'est r�gul� au niveau maximal autoris�");
			}
		}
	}
}
