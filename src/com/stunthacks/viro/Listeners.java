package com.stunthacks.viro;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by StuntHacks on 22.06.2017.
 */
public class Listeners implements Listener {
    private Viro plugin;

    Listeners(Viro viro) {
        this.plugin = viro;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event)
    {
        if(event instanceof EntityDamageByEntityEvent)
        {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

            if(e.getDamager() instanceof Player && e.getEntity() instanceof Player)
            {
                int unixTime = (int) (System.currentTimeMillis() / 1000L);

                if(unixTime <= plugin.gameStart + (plugin.config.getPeaceDuration() * 60)) {
                    e.getDamager().sendMessage("§CThe peace period is still running!");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player p = event.getPlayer();

        if(plugin.gameRunning) {
            boolean teamFound = false;
            int team = -1;

            for(int i = 0; i  < plugin.teams.size() && !teamFound; i++) {

                if(plugin.teams.get(i).getMembers().contains(p.getName())) {
                    team = i;
                    teamFound = true;
                }
            }

            if(team != -1) {
                plugin.teams.get(team).removeLive();

                if(plugin.teams.get(team).getLives() > 0) {
                    int unixTime = (int) (System.currentTimeMillis() / 1000L);
                    plugin.banned.put(p.getName(), unixTime);

                    DecimalFormat df = new DecimalFormat("#.##");
                    String remainingTimeStr = df.format(plugin.config.getBanDuration());
                    remainingTimeStr = remainingTimeStr.replaceAll(",", ".");

                    if(plugin.teams.get(team).getLives() == 1)
                        p.kickPlayer("You were banned for §C" + remainingTimeStr + " §Rhours. Your team has §C" + plugin.teams.get(team).getLives() + " §Rlive remaining.");
                    else
                        p.kickPlayer("You were banned for §C" + remainingTimeStr + " §Rhours. Your team has §C" + plugin.teams.get(team).getLives() + " §Rlives remaining.");

                } else {
                    for(int i = 0; i < plugin.teams.get(team).getMembers().size(); i++) {
                        String name = plugin.teams.get(team).getMembers().get(i);

                        p = plugin.getServer().getPlayerExact(name);

                        if(p != null) {
                            p.setGameMode(GameMode.SPECTATOR);
                            p.sendMessage("§BYour team lost all it's lives and so you lost this round. You are now put into spectator mode.");
                        }
                    }

                    int living = 0;
                    int livingId = -1;

                    for(int i = 0; i < plugin.teams.size(); i++) {

                        if(plugin.teams.get(i).getLives() > 0) {
                            living++;
                            livingId = i;
                        }
                    }

                    if(living == 1) {
                        plugin.getServer().broadcastMessage("§BTeam " + plugin.teams.get(livingId).getName() + " has won this round!");
                        plugin.banned.clear();
                        plugin.lastWinner = plugin.teams.get(livingId).getName();
                        plugin.resetColors();
                        plugin.gameRunning = false;
                    } else if(living == 0) {
                        plugin.getServer().broadcastMessage("§BIt's a tie - No team has won this round!");
                        plugin.banned.clear();
                        plugin.lastWinner = "None";
                        plugin.resetColors();
                        plugin.gameRunning = false;
                    }
                }
            }
        }

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.saveConfig();
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        boolean done = false;

        if(plugin.banned.containsKey(event.getPlayer().getName())) {
            event.setQuitMessage(null);
            done = true;
        }

        if(!done) {
            boolean teamFound = false;
            int team = -1;

            for(int i = 0; i  < plugin.teams.size() && !teamFound; i++) {

                if(plugin.teams.get(i).getMembers().contains(p.getName())) {
                    team = i;
                    teamFound = true;
                }
            }

            if(team != -1) {

                if(plugin.teams.get(team).getLives() == 0) {
                    event.setQuitMessage(null);
                } else {
                    boolean inFight = false;
                    int radius = 15;
                    double radiusSquared = radius*radius;

                    if(p.getHealth() < 19) {
                        List<Entity> entities = p.getNearbyEntities(radius, radius, radius); // All entities withing a box

                        for (Entity entity : entities) {

                            if (entity.getLocation().distanceSquared(p.getLocation()) > radiusSquared)
                                continue; // All entities within a sphere

                            if (entity instanceof Player) {

                                if(!plugin.teams.get(team).getMembers().contains(entity.getName()))
                                    inFight = true;
                            }
                        }
                    }

                    if(plugin.joined.containsKey(p.getName())) {
                        int unixTime = (int) (System.currentTimeMillis() / 1000L);
                        int join = plugin.joined.get(p.getName());

                        if(unixTime < join + (plugin.config.getPlayDuration() * 60) || inFight) {
                            plugin.teams.get(team).removeLive();

                            if(plugin.teams.get(team).getLives() > 0) {
                                unixTime = (int) (System.currentTimeMillis() / 1000L);
                                plugin.banned.put(p.getName(), unixTime);
                            } else {

                                for(int i = 0; i < plugin.teams.get(team).getMembers().size(); i++) {
                                    String name = plugin.teams.get(team).getMembers().get(i);

                                    p = plugin.getServer().getPlayerExact(name);

                                    if(p != null) {
                                        p.setGameMode(GameMode.SPECTATOR);
                                        p.sendMessage("§BYour team lost all it's lives and so you lost this round. You are now put into spectator mode.");
                                    }
                                }

                                int living = 0;
                                int livingId = -1;

                                for(int i = 0; i < plugin.teams.size(); i++) {

                                    if(plugin.teams.get(i).getLives() > 0) {
                                        living++;
                                        livingId = i;
                                    }
                                }

                                if(living == 1) {
                                    plugin.getServer().broadcastMessage("§BTeam " + plugin.teams.get(livingId).getName() + " has won this round!");
                                    plugin.banned.clear();
                                    plugin.lastWinner = plugin.teams.get(livingId).getName();
                                    plugin.resetColors();
                                    plugin.gameRunning = false;
                                } else if(living == 0) {
                                    plugin.getServer().broadcastMessage("§BIt's a tie - No team has won this round!");
                                    plugin.banned.clear();
                                    plugin.lastWinner = "None";
                                    plugin.resetColors();
                                    plugin.gameRunning = false;
                                }
                            }
                        } else {

                            if(plugin.joined.containsKey(p.getName()))
                                plugin.joined.remove(p.getName());
                        }

                        plugin.teams.get(team).setLastLogin();
                    }
                }
            }
        }

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.saveConfig();
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(PlayerChatEvent event) {
        Player p = event.getPlayer();

        if(plugin.gameRunning) {
            boolean teamFound = false;
            int team = -1;

            for(int i = 0; i  < plugin.teams.size() && !teamFound; i++) {

                if(plugin.teams.get(i).getMembers().contains(p.getName())) {
                    team = i;
                    teamFound = true;
                }
            }

            if(team != -1) {
                event.setCancelled(true);

                if(event.getMessage().startsWith("@")) {
                    for(int i = 0; i < plugin.teams.get(team).getMembers().size(); i++) {
                        plugin.getServer().getPlayerExact(plugin.teams.get(team).getMembers().get(i)).sendMessage(
                                plugin.teams.get(team).getColor() + "[TEAM] " +
                                "§R<" + plugin.teams.get(team).getColor()
                                + event.getPlayer().getName() + "§R> " + event.getMessage().replace("@", ""));
                    }
                } else {
                    plugin.getServer().broadcastMessage("<" + plugin.teams.get(team).getColor()
                            + event.getPlayer().getName() + "§R> " + event.getMessage());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        if(!plugin.gameRunning) {
            // broadcast last winner
            if (!plugin.lastWinner.equalsIgnoreCase("None")) {
                p.sendMessage("§BTeam " + plugin.lastWinner + " has won the last round!");
            } else {
                p.sendMessage("§BNo team has won the last round");
            }
        } else {
            boolean teamFound = false, isBanned = false;
            int team = -1;

            for(int i = 0; i  < plugin.teams.size() && !teamFound; i++) {

                if(plugin.teams.get(i).getMembers().contains(p.getName())) {
                    team = i;
                    teamFound = true;
                }
            }

            if(team != -1) {
                // handle bans/loses
                if(plugin.teams.get(team).getLives() > 0) {

                    for (int i = 0; i < plugin.banned.size(); i++) {

                        if(plugin.banned.containsKey(p.getName())) {
                            int unixTime = (int) (System.currentTimeMillis() / 1000L);
                            int banned = plugin.banned.get(p.getName());

                            if(unixTime > banned + (plugin.config.getBanDuration() * 3600)) {
                                plugin.banned.remove(p.getName());
                                plugin.teams.get(team).setLastLogin();
                            } else {
                                double remainingTime = (((plugin.config.getBanDuration() * 3600) - (unixTime - banned)) / 3600);
                                DecimalFormat df = new DecimalFormat("#.###");
                                String remainingTimeStr = df.format(remainingTime);
                                remainingTimeStr = remainingTimeStr.replaceAll(",", ".");
                                final String out = remainingTimeStr;

                                event.setJoinMessage(null);

                                BukkitScheduler scheduler = plugin.getServer().getScheduler();
                                scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
                                    @Override
                                    public void run() {
                                        p.kickPlayer("You are still banned for §C" + out +  "§R hours.");
                                    }
                                }, 1L);

                                isBanned = true;
                            }
                        }
                    }
                } else {
                    p.setGameMode(GameMode.SPECTATOR);
                    p.sendMessage("§BYour team lost all it's lives and so you lost this round. You are now put into spectator mode.");
                    event.setJoinMessage(null);
                }
            }

            if(!isBanned) {
                int lastLogin = plugin.teams.get(team).getLastLogin();
                plugin.teams.get(team).setLastLogin();
                int currentTime = plugin.teams.get(team).getLastLogin();

                // one week check
                if(currentTime > lastLogin + 604800 && lastLogin != -1) {
                    plugin.teams.get(team).setLives(0);
                    p.setGameMode(GameMode.SPECTATOR);
                    p.sendMessage("§BYour team lost all it's lives and so you lost this round. You are now put into spectator mode.");
                    event.setJoinMessage(null);

                    int living = 0;
                    int livingId = -1;

                    for(int i = 0; i < plugin.teams.size(); i++) {

                        if(plugin.teams.get(i).getLives() > 0) {
                            living++;
                            livingId = i;
                        }
                    }

                    if(living == 1) {
                        plugin.getServer().broadcastMessage("§BTeam " + plugin.teams.get(livingId).getName() + " has won this round!");
                        plugin.banned.clear();
                        plugin.lastWinner = plugin.teams.get(livingId).getName();
                        plugin.resetColors();
                        plugin.gameRunning = false;
                    } else if(living == 0) {
                        plugin.getServer().broadcastMessage("§BIt's a tie - No team has won this round!");
                        plugin.banned.clear();
                        plugin.lastWinner = "None";
                        plugin.resetColors();
                        plugin.gameRunning = false;
                    }
                } else {
                    plugin.joined.put(p.getName(), currentTime);

                    // handle team colors
                    teamFound = false;

                    for(int i = 0; i  < plugin.teams.size() && !teamFound; i++) {

                        if(plugin.teams.get(i).getMembers().contains(p.getName())) {
                            final int t = i;

                            BukkitScheduler scheduler = plugin.getServer().getScheduler();
                            scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    p.setPlayerListName(plugin.teams.get(t).getColor() + p.getName());
                                }
                            }, 1L);

                            teamFound = true;
                        }
                    }
                }
            }
        }

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.saveConfig();
            }
        }, 1L);
    }
}
