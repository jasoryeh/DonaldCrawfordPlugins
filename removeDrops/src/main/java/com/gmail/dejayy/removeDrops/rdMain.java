package com.gmail.dejayy.removeDrops;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class rdMain extends JavaPlugin {
	
	int minutes;
	
	public void onEnable(){
		
		minutes = 0;
		
		long time = (60 * 20);
				
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, removal, time, time);
		
		this.saveDefaultConfig();
		
	}
	
	public void onDisable(){
		
		
	}
	
	static String replaceColors(String message) {
		
		return ChatColor.translateAlternateColorCodes('&', message);
		
	  }
	
	
	
	
    public Runnable removal  =  new Runnable(){ // create the Update Runnable. Register using Bukkit.getScheduler().scheduleSyncRepeatingTask(YOUR_PLUGIN, updateSign, 0L, time);

        @Override
        public void run() {
        	
        	int delay = getConfig().getInt("timeDelay");
        	
        	if(minutes == delay){
        		
        		minutes = 0;
        		
        		removeDrops();
        		
        		getServer().broadcastMessage(ChatColor.DARK_AQUA + "[Drop Removal] " + ChatColor.AQUA + "All items have been deleted.");
        	}
        	
        	minutes++;
        	
        	
        	if(minutes == (delay - 1)){
        		
        		String warnMSG = replaceColors(getConfig().getString("warnMSG"));
				
				if(!(warnMSG == "disable")){
					
					getServer().broadcastMessage(warnMSG);
					
				}
        		
        	}
        	
        }
        
    };
    
	public boolean onCommand(CommandSender sender, Command cmd, String cmdl, String[] args) {
		
		if(!(sender instanceof Player)){
			
			if(cmdl.equalsIgnoreCase("removedrops") || cmdl.equalsIgnoreCase( "rd")){
				
				this.removeDrops();
								
			}
			
		}else if(sender instanceof Player){
			
			Player p = (Player) sender;
			
			if(p.hasPermission("removedrops.admin")){
				
				this.removeDrops();
				
				
			}else{
				
				p.sendMessage(ChatColor.AQUA + "You don't have permission to run that command!");
				
				return true;
				
			}
		}
		
		return true;
		
	}
	
	public void removeDrops(){
		
		String doMobs = this.getConfig().getString("mobs");
		
		List<World> worlds = this.getServer().getWorlds();
		
		for(World w : worlds){
			
			if(!(doMobs.equalsIgnoreCase("none"))){
				
				this.removeMobs(w);
				
			}
			
			try{	
				
				List<Entity> e = w.getEntities();
											
				for(Entity ent : e){
					
					if(ent instanceof Item){
						
						ent.remove();
						
					}
					
					
				} // for loop
				
			}catch (Exception ex){
				
				this.getServer().getLogger().info("[removeDrops] Error: " + ex.getMessage());
				
			}
			
		} //world loop
		
	} //removeDrops
	
	public void removeMobs(World wrld){
		
		String what = this.getConfig().getString("mobs");
		
		if(what.equalsIgnoreCase("all")){
			
			for(LivingEntity e : wrld.getLivingEntities()){
				
				if(!(e instanceof Player)){
					
					e.setHealth(0);
					
				}
			}
			
		}else if(what.equalsIgnoreCase("passive")){
			
			for(LivingEntity ent : wrld.getLivingEntities()){
				
				if(!(ent instanceof Player) && ent instanceof Animals){
					ent.setHealth(0);
				}
				
			} //End for loop
		}else if(what.equalsIgnoreCase("aggressive")){			
			
			for(LivingEntity ent : wrld.getLivingEntities()){
				
				if(!(ent instanceof Player) && ent instanceof Monster){
					ent.setHealth(0);
				}
				
			} //End for loop


		}
		
	}

}
