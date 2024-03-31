package me.Bentipa.BungeeSignsFree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemUtils {
	
	private static HashMap<String, List<List<String>>> anLoreLores = new HashMap<String, List<List<String>>>();
	private static ArrayList<String> anIDs = new ArrayList<String>();
	private static HashMap<String, ItemStack> anItems  = new HashMap<String, ItemStack>();
	private static HashMap<String, Integer> anStep = new HashMap<String, Integer>();
	private static HashMap<String, Integer> anPos = new HashMap<String, Integer>();
	
	/**
	 * Sets the name of an ItemStack
	 * @param i the ItemStack to use
	 * @param s the Name to use
	 */
	public static void setName(ItemStack i, String s){
		ItemMeta m = i.getItemMeta();
		m.setDisplayName(s);
		i.setItemMeta(m);
	}
	
	/**
	 * Sets the Lore of an ItemStack
	 * @param i the ItemStack to use
	 * @param lore - the Lore to add
	 */
	public static void setLore(ItemStack i, List<String> lore){
		ItemMeta m = i.getItemMeta();
		m.setLore(lore);
		i.setItemMeta(m);
	}
		
	/**
	 * Sets a AnimatedLore of an ItemStack
	 * @param i the ItemStack to use
	 * @param id - the Lore to add
	 */
	public static void setAnimatedLore(final Inventory inv, JavaPlugin pl, ItemStack i, String id,  List<List<String>> steps, long time){
//		System.out.println("AnimLore: steps.size()=="+steps.size());
		if(!steps.isEmpty() && steps.size() == 1){
		setLore(i, steps.get(0));
		}else if(steps.size() > 1){
			anPos.put(id, getPos(inv, i));
//			System.out.println("Pos: " + getPos(inv, i));
			setLore(i, steps.get(0));
//			System.out.println("Lore1: ");
			//outList(steps.get(0));
			anIDs.add(id);
			anItems.put(id, i);
			anLoreLores.put(id, steps);
			anStep.put(id, 0);
		
//			System.out.println("Added all things!");
//			System.out.println(anIDs.get(anIDs.size()-1));
//			System.out.println(anLoreLores.get(i));
//			System.out.println(anStep.get(i));
		
		
		int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
		
			@Override
			public void run() {
				for(String si : anIDs){
					//System.out.println("Checking! "+ " Step: " + anStep.get(si) +" - " + anLoreLores.get(si).size());
					//outList(anLoreLores.get(si).get(anStep.get(si)));
					List<List<String>> lorelist = anLoreLores.get(si);
					if(lorelist!=null){
						//System.out.println("Lorelist: OK!");
						int currentstep = anStep.get(si);
						int step = currentstep;
						//System.out.println("Currentstep: " + currentstep);
						if(currentstep == lorelist.size()-1){
						//	System.out.println("Currenstep == " + (lorelist.size()-1));
							step = 0;
							anStep.put(si, step);
						}else if (currentstep < lorelist.size()){
						//	System.out.println("Currentstep < " + lorelist.size());
							step++;
							anStep.put(si, step);
						}
						ItemStack currentItem = anItems.get(si);						
						setLore(currentItem, lorelist.get(step));
						inv.setItem(anPos.get(si), currentItem);
					}
				}
			}
		}, 0, time);
		}
	}
	private static int getPos(Inventory i, ItemStack is){
		int d = 0;
		for(ItemStack iss: i.getContents()){
			if(iss!=null){
			if(iss.equals(is)){
				return d;
			}
			
			}
			d++;
			
		}
		
		return 0;
	}

	
	private static void outList(List<String> sl){
		for(String s: sl){
			Core.getInstance().getLogger().log(Level.INFO, s);
		}
	}
	
	/**
	 * Add an Enchantment to an ItemStack
	 * @param i the ItemStack to enchant
	 * @param e the Enchantment to use
	 * @param level the level to use
	 * @param forceench if the Level-Restriction should be ignored
	 */
	public static void addEnchantment(ItemStack i, Enchantment e, int level, boolean forceench){
		ItemMeta m = i.getItemMeta();
		m.addEnchant(e, level, forceench);
		i.setItemMeta(m);
	}
	
	public LoreGenerator newLoreGenerator(){	
	return new LoreGenerator();					
	}
	
	public AnimatedLoreLineGenerator newAnimatedLoreLineGenerator(int width, String... lines){	
		return new AnimatedLoreLineGenerator(width, lines);					
	}
	
	public class LoreGenerator{
		
		private List<String> lore;
		
		public LoreGenerator() {		
				lore = new ArrayList<String>();			
		}
		
		public LoreGenerator addLine(String s){
			lore.add(s);
			return this;
		}
		
		public List<String> getLore(){
			return this.lore;
		}
		
	}
		
	public class AnimatedLoreLineGenerator{
			
			private List<List<String>> lore = new ArrayList<List<String>>();
			
			
			public AnimatedLoreLineGenerator(int width, String... lines){
				
				ArrayList<List<String>> pre = new ArrayList<List<String>>(); // alle Animation Packete
				if(lines!=null)
				for(String s: lines){
//					System.out.println("For: "+ s + " Width: " + width + " Length: " + s.length() + " <" + lastChar(s));					
					pre.add(inCharSeqPieces(s, width));
				}
				
				int largest = getLargest(pre); // Anzahl an allen Animationsschritten
				for(int i = 0; i < largest; i++){
					lore.add(new ArrayList<String>());
					for(List<String> ansteps : pre){
//						System.out.println("AnStep: " + i); 
						if(i < ansteps.size()){
//						System.out.println("Line: " + ansteps.get(i) == null ? "" : trimprefandsub(ansteps.get(i)));
						lore.get(i).add(ansteps.get(i) == null ? "" : trimprefandsub(ansteps.get(i)));
						}else{
							lore.get(i).add("");
						}
					}
				}
				
			}
			
			private String trimprefandsub(String raw){
				while(raw.startsWith(" ")){
					raw = raw.substring(1, raw.length());
				}
				while(raw.endsWith(" ")){
					raw = raw.substring(0, raw.length()-1);
				}
				return raw;
			}
			
			private int lastChar(String s){
				int last = 0;
				for(char c: s.toCharArray()){
//					System.out.println(last +" - "+ c);
					last++;
				}
				return last;
			}
			
			private List<String> inList(String... strings){
				ArrayList<String> ret = new ArrayList<String>();
				for(String s: strings){
					ret.add(s);
				}
				return ret;
			}
		
			
			private int getLargest(ArrayList<List<String>> c){
				int curlarg = 0;
				for(List<String> s: c){
					if(s.size()> curlarg)	
						curlarg = s.size();
				}
				
				return curlarg;
			}
			
			public List<List<String>> getLoreAnimation(){
				return lore;
			}
			
			private List<String> inCharSeqPieces(String s, int length){
				ArrayList<String> ret = new ArrayList<String>();		
				int lastchars = 1;
				for(int i = 0; i < s.length(); i++){
					String c = s; 
					int lastto = length+i;
					if(lastto > s.length()){
						lastto = s.length();
					}
//					System.out.println("Check between " + i + " and " + lastto);
//					System.out.println(" Check: " + i + " length: " + length + " Sub:" + c.substring(i, lastto));
					String cur = c.substring(i, lastto);
					ret.add(cur);							
				}			
				return ret;
			}
		
	}
}
