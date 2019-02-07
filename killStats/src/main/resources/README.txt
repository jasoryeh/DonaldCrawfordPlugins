These variables can be placed in any message within messages.yml and be replaced with the appropriate data where applicable.
%victim.name% - The victims name
%victim.kills% - The victims kill count
%victim.deaths% - The victims death count
%victim.streak% - The victims kill streak
%victim.ratio% - The victims k/d ratio
%killer.name% - The killers name
%killer.kills% - The killers kill count
%killer.deaths% - The killers death count
%killer.streak% - The killers kill streak
%killer.ratio% - The killers k/d ratio

These variables are only applicable within the "TS_X" messages.
Example "TS_Chat_Format"
%stats.playername% - Shows the playername
%stats.value% - Shows how many kills/deaths etc they player has
%stats.position% - Shows the rank of the player
%stats.currentpage% - Shows the page that is being viewed
%stats.maxpage% - Shows the max number of pages
%stats.lastupdate% - Shows how many minutes ago the top stats were updated

This variable is used in the '/ks rank' command and on signs.
Replaced with the players rank, or the rank you choose on signs.
%ranking%

"AntiBoost_Time_Limit" is measured in minutes.
"Top_Stats_Update" is measured in minutes. It is best not to make this a small time limit.
If you update stats on your server every 2 minutes, your putting alot of strain on the server, its best to wait a while.

This is the best explanation of the Anti-Boost feature of this plugin I can give.
The "Anti_Boost_Time_Limit" means that the player "bob" cannot kill the player "john" more than "AntiBoost_Max_Killing" within "AntiBoost_Time_Limit". 
If player "bob" does kill "john" more than he is suppose to, he is flagged for boosting and killing "john" again will not increase his kills or killstreak.
Nor will in increase the death count of "john".

The following messages can be disabled by simply putting "disable" as the message in messages.yml
Reward_Message
Full_Inventory_Message

Startover_Cooldown:
When a player issues command "/ks startover" they are asked in chat to confirm they want to start over.
If they do no answer within the given time limit, their request is cancelled and their stats are not reset.

Example of giving items with the rewards feature of this plugin.
Also an example of executing commands with the rewards feature of this plugin.
All commands are sent as OP. So you should be able to issue ANY command.

Format: ItemID[:DataValue] Amount [Enchantment:Level] [name:The_Name] [lore:The_Lore] <- example of all things possible, can put all enchantments is you want to.

Spaces in item names and lore must be an underscore("_")
";" <- = new line for lore items

Rewards:
  5:
    - 373:16273 2
    - 264 10
    - command;/xp give %killer.name% 825

  10:
    - 264 25
    - 276 1 sharpness:2 fireaspect:1 name:Killer_Sword

  15:
    - 264 25
    - 276 1 sharpness:4 fireaspect:2 name:%killer.name%'s_boss_sword lore:Lore_1;Lore_2;Lore_3
    
Permissions
killstats_admin.reload - Reload the killstats configuration
killstats_admin.enable_disable - Enable/Disable the plugin
killstats_admin.player_head - Allow the player to get head of other players
killstats_admin.reset_players - Allow the player to reset any players on the server
killstats.admin.createsign - Allow the player to create a leaderboard sign.

killstats.player.startover - Allows the player to reset their own stats