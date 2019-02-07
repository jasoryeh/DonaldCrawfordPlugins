package com.gmail.dejayyy.coloredTab;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;
 
public class VaultAdapter {
    private static final String VAULT = "Vault";
    //economy
    private static Economy ecoVault = null;
    private static Permission permVault = null;
    private static Chat chatVault = null;
    private static boolean vaultLoaded = false;
    
    public static Economy getEconomy(){
        if(!vaultLoaded){
            vaultLoaded = true;
            Server theServer = Bukkit.getServer();
            if (theServer.getPluginManager().getPlugin(VAULT) != null){
                RegisteredServiceProvider<Economy> rsp = theServer.getServicesManager().getRegistration(Economy.class);
                if(rsp!=null){
                   ecoVault = rsp.getProvider();
                }
            }
        }
               
        return ecoVault;
    }    
    
    
    
    public static Permission getPermissions(){
        if(!vaultLoaded){
            vaultLoaded = true;
            Server theServer = Bukkit.getServer();
            if (theServer.getPluginManager().getPlugin(VAULT) != null){
                RegisteredServiceProvider<Permission> rsp = theServer.getServicesManager().getRegistration(Permission.class);
                if(rsp!=null){
                    permVault = rsp.getProvider();
                }
            }
        }
               
        return permVault;
    }
    
    
    public static Chat getChat(){
        if(!vaultLoaded){
            vaultLoaded = true;
            Server theServer = Bukkit.getServer();
            if (theServer.getPluginManager().getPlugin(VAULT) != null){
                RegisteredServiceProvider<Chat> rsp = theServer.getServicesManager().getRegistration(Chat.class);
                if(rsp!=null){
                    chatVault = rsp.getProvider();
                }
            }
        }
               
        return chatVault;
    }
    
    
    
}