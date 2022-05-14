package fr.a6st.epuhc.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public class TaupeKit {

	@SuppressWarnings("deprecation")
	public static void CreateTaupeKit(List<List<ItemStack>> kits) {
		
		List<ItemStack> TNT = new ArrayList<>();
		List<ItemStack> Blaze = new ArrayList<>();
		List<ItemStack> Aerien = new ArrayList<>();
		List<ItemStack> Potion = new ArrayList<>();
		
		//Creation des kits
	    //Kit TNT :
		TNT.add(new ItemStack(Material.TNT, 5));
		TNT.add(new ItemStack(Material.FLINT_AND_STEEL, 1));
		TNT.add(new ItemStack(Material.MONSTER_EGG, 1, EntityType.CREEPER.getTypeId()));
	    kits.add(TNT);
	    
		//Kit Blaze :
	    Blaze.add(new ItemStack(Material.MONSTER_EGG, 3, EntityType.BLAZE.getTypeId()));
	    
	    //Book flame
	    ItemStack fireBook = new ItemStack(Material.ENCHANTED_BOOK, 1);
	    ItemMeta metaFire = fireBook.getItemMeta();
	    metaFire.addEnchant(Enchantment.ARROW_FIRE, 1, false);
	    fireBook.setItemMeta(metaFire);
	    Blaze.add(fireBook);
	    
	    kits.add(Blaze);
	    
		//Kit aérien :
	    //Epée en fer kb 2
	    ItemStack kb = new ItemStack(Material.IRON_SWORD, 1);
	    ItemMeta kbMeta = kb.getItemMeta();
	    kbMeta.addEnchant(Enchantment.KNOCKBACK, 2, false);
	    kb.setItemMeta(kbMeta);
	    Aerien.add(kb);
	    
	    //Livre feather falling 4
	    ItemStack featherBook = new ItemStack(Material.ENCHANTED_BOOK, 1);
	    ItemMeta metaFeather = featherBook.getItemMeta();
	    metaFeather.addEnchant(Enchantment.PROTECTION_FALL, 4, false);
	    featherBook.setItemMeta(metaFeather);
	    Aerien.add(featherBook);
	    
	    Aerien.add(new ItemStack(Material.ENDER_PEARL, 3));
	    
	    kits.add(Aerien);
	    
		//Kit potion :
	    Potion.add(new Potion(PotionType.POISON, 1, true ,false).toItemStack(1));
	    Potion.add(new Potion(PotionType.SLOWNESS, 1, true ,false).toItemStack(1));
	    Potion.add(new Potion(PotionType.WEAKNESS, 1, true ,false).toItemStack(1));
	    Potion.add(new Potion(PotionType.INSTANT_DAMAGE, 1, true ,false).toItemStack(1));
	    
	    kits.add(Potion);
	}
	
}
