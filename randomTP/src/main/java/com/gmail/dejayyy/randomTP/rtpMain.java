package com.gmail.dejayyy.randomTP;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class rtpMain extends JavaPlugin {

	public ArrayList<String> locations = new ArrayList<String>();
	public HashMap<String, Long> players = new HashMap<String, Long>();
	
	public File configFile;
	public FileConfiguration ymlFile;
	
	
	public void onEnable(){
		
		this.saveDefaultConfig();
		
		this.loadLocationsYML();
		
		for(String blah : ymlFile.getStringList("Locations")){
			
			locations.add(blah);
			
		}

	}
	
	public void onDisable(){
		
		this.saveLocationsYML();
		
	}
	
	public boolean locationsEmpty(){
		
		if(locations.size() == 0){
			
			return true;
			
		}else{
			
			return false;
			
		}
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String cmdl, String[] args)  {
		
		int intNum = 0;
		int coolDown = 0;


		if(cmd.getName().equalsIgnoreCase("randomTPReload")){
			
			if(sender.hasPermission("randomtp.reload")){
				
				this.reloadConfig();
				
				locations.clear();
				
				for(String blah : ymlFile.getStringList("Locations")){
					
					locations.add(blah);
					
				}
				
				sender.sendMessage(ChatColor.GREEN + "Reload complete!");
				
				return true;
				
				
			}
		}
		
		if(cmdl.equalsIgnoreCase("addwildloc") || cmdl.equalsIgnoreCase("addwildlocation") || cmdl.equalsIgnoreCase("awl")){
			
			if(sender instanceof Player){
				
				Player player = (Player) sender;
				
				if(player.hasPermission("RandomTP.admin")){
					
					Location loc = player.getLocation();
					
					double x = loc.getX();
					double y = loc.getY();
					double z = loc.getZ();
					double yaw = loc.getYaw();
					double pitch = loc.getPitch();
					
					locations.add(x + ";" + y + ";" + z + ";" + yaw + ";" + pitch);
					
					this.ymlFile.set("Locations", locations);
							
					this.saveLocationsYML();
					
					player.sendMessage(ChatColor.AQUA + "Location added!");
					
				}else{
					
					player.sendMessage(ChatColor.RED + "You don't have permission to execute that command!");
					
					return true;
					
				}
			}
			
		}else if(cmdl.equalsIgnoreCase("wild") || cmdl.equalsIgnoreCase("tprandom") || cmdl.equalsIgnoreCase("randomtp")){
			
			if(sender instanceof Player){
				
				
				final Player player = (Player) sender;
				
				if(this.locationsEmpty()){
					
					player.sendMessage(ChatColor.AQUA + "Sorry, there have been no locations setup for this feature!");
					
					return true;
					
				}
				
				if(players.containsKey(player.getName())){
					
					long a = players.get(player.getName());
					long b = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());
					
					long TimeRemaining = (a - b);
					
					player.sendMessage(replaceColors(this.getConfig().getString("CooldownMSG")).replaceAll("<x>", Long.toString(TimeRemaining)));
					
					return true;
					
				}
				
				int intMaxNumber = locations.size();
				
				intNum = this.getRandomNumber(1, intMaxNumber) - 1;
				
				
				String loc[] = locations.get(intNum).split(";");
				
				Double x = Double.parseDouble(loc[0]);
				Double y = Double.parseDouble(loc[1]);
				Double z = Double.parseDouble(loc[2]);
				
				float pitch = Float.parseFloat(loc[3]);
				float yaw = Float.parseFloat(loc[4]);
				
				World world = player.getWorld();
				Location location = new Location(world, x, y, z, pitch, yaw);
				
				player.teleport(location);
				
				
				if(!(this.getConfig().getString("AlertMSG").equalsIgnoreCase("disable"))){
					
					player.sendMessage(replaceColors(this.getConfig().getString("AlertMSG")));
					
				}
				
				if(!(this.getConfig().getInt("TimeLimit") == 0)){
					
					coolDown = this.getConfig().getInt("TimeLimit");
					
					players.put(player.getName(), TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) + coolDown);

					coolDown = coolDown * 20 * 60;
					
		            this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
		            {
		                @Override
		                public void run() {

		                	players.remove(player.getName());
		                    
		                    }
		                
		            }, coolDown);
					
				}
				
				
			}else{
				
				sender.sendMessage("You goofball, you can't run that command from console!");
				
				return true;
				
			}
		
		}
		
		return true;
		
	}
	
	static String replaceColors(String msg){
		
		return ChatColor.translateAlternateColorCodes('&', msg);
		
	}
	
	public void loadLocationsYML(){
	    configFile = new File(getDataFolder(), "Locations.yml");
	    
	    ymlFile = new YamlConfiguration();
	    
		if(getDataFolder().exists() == false){
			
			try{
				getDataFolder().mkdir();
			}catch (Exception ex){
				//something went wrong.
			}
			
		}
	
	
		if(configFile.exists() == false){
			
			try{
				configFile.createNewFile();
			}catch (Exception ex){
				//something went wrong.
			}
		} 
		
		try{ //Load payers.yml
			ymlFile.load(configFile);
		}catch (Exception ex){
			//Something went wrong
		}
	}
	
	public void saveLocationsYML(){
		
		try {
			ymlFile.save(configFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public int getRandomNumber(int min, int max) {
        Random foo = new Random();
        int randomNumber = foo.nextInt((max + 1) - min) + min;

        return randomNumber;

    }
	
}
