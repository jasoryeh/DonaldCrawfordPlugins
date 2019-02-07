package com.gmail.dejayyy;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class killMobs extends JavaPlugin{

public final Logger log = Logger.getLogger("Minecraft");

	@Override
	public void onEnable(){
		
		PluginDescriptionFile pdffile = this.getDescription();
				
		this.log.info("[" + pdffile.getName() + "] has been enabled.");
		
	} //End onEnable
	
	@Override
	public void onDisable(){
		
		PluginDescriptionFile pdffile = this.getDescription();
		
		this.log.info("[" + pdffile.getName() + "] has been disabled.");
		
	} //End ondisable
	
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if(sender instanceof Player){
			
			Player player = (Player) sender;
			Location loc = player.getLocation();
			World wrld = loc.getWorld();
			List<LivingEntity> mobs = wrld.getLivingEntities();
			
			if(commandLabel.equalsIgnoreCase("killmobs") || commandLabel.equalsIgnoreCase("km")) {
				
				if(args.length == 1){
					
					if(args[0].equalsIgnoreCase("passive")){ //Kill passive mobs 
						
						if(player.hasPermission("killmobs.kill.passive")){ //perm check
							
							for(LivingEntity ent : mobs){
								
								if(!(ent instanceof Player)){
									ent.setHealth(0);
								}
								
							} //for loop
							
							player.sendMessage("All passive mobs have been removed!");
							
						} //perm check
					}else if(args[0].equalsIgnoreCase("hostile")){
						
						if(player.hasPermission("killmobs.kill.hostile")){ //Perm check
							
							for(LivingEntity ent : mobs){
								
								if(!(ent instanceof Player)){
									ent.setHealth(0);
								}
								
							} //for loop
							
							player.sendMessage("All hostile mobs have been removed!");
							
						} // End perm check
					}else if(args[0].equalsIgnoreCase("all")){
						
						if(player.hasPermission("killmobs.kill.all")){
							
							for(LivingEntity ent : mobs){
								
								if(!(ent instanceof Player)){
									ent.setHealth(0);
								}
							}
						
							player.sendMessage("All mobs have been removed!");
							
						} //End perm check
						
					}else{
						player.sendMessage("Invalid argument! Must choose hostile, passive, or all");
					}
					
				}else if(args.length > 1){
					
					player.sendMessage("Too many arguments!");
					
				}else if(args.length < 1){
					
					player.sendMessage("Not enough arguments!");
					
				} //End argument check
				
				
			} //End check for cmd
	
		}else{
			sender.sendMessage("You goofball, you cant use this command from console!");
		}
		return true;
		
	} //End onCommand

	
	
	
} //End plugin
