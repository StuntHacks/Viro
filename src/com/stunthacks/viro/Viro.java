package com.stunthacks.viro;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by StuntHacks on 22.06.2017.
 */
public class Viro extends JavaPlugin {
    private static final String PRE_PLUGIN = "[Viro] ";
    private static final String PRE_SUCC = ChatColor.GREEN + "[SUCCESS] ";

    Config config = new Config();
    private ArrayList<String> op = new ArrayList<>();
    private ArrayList<String> spectators = new ArrayList<>();
    HashMap<String, Integer> banned = new HashMap<>();
    HashMap<String, Integer> joined = new HashMap<>();
    ArrayList<ViroTeam> teams = new ArrayList<>();
    boolean gameRunning;
    private ChatColor[] colors = {
            ChatColor.AQUA,
            ChatColor.BLUE,
            ChatColor.DARK_AQUA,
            ChatColor.DARK_BLUE,
            ChatColor.DARK_GREEN,
            ChatColor.DARK_PURPLE,
            ChatColor.DARK_RED,
            ChatColor.GOLD,
            ChatColor.GREEN,
            ChatColor.LIGHT_PURPLE,
            ChatColor.RED,
            ChatColor.YELLOW
    };
    String lastWinner = "None";
    int gameStart = -1;
    private boolean[] colorsUsed = new boolean[12];

    void resetColors() {
        for(Player players : getServer().getOnlinePlayers())
        {
            boolean teamFound = false;

            for(int i = 0; i  < teams.size() && !teamFound; i++) {

                if(teams.get(i).getMembers().contains(players.getName())) {
                    players.setPlayerListName(teams.get(i).getColor() + players.getName());
                    teamFound = true;
                }
            }
        }
    }

    private void loadData() throws FileNotFoundException {
        String content = new Scanner(new File(getDataFolder() + "\\config.json")).useDelimiter("\\Z").next();
        JsonElement jelement = new JsonParser().parse(content);
        JsonObject  jobject = jelement.getAsJsonObject();

        config.setLives(jobject.get("lives").getAsInt());
        config.setBanDuration(jobject.get("banDuration").getAsDouble());
        config.setPlayDuration(jobject.get("playDuration").getAsDouble());
        config.setWorldborder(jobject.get("worldborder").getAsInt());
        gameRunning = jobject.get("gameRunning").getAsBoolean();

        content = new Scanner(new File(getDataFolder() + "\\ops.json")).useDelimiter("\\Z").next();
        jelement = new JsonParser().parse(content);

        for(int i = 0; i < jelement.getAsJsonArray().size(); i++) {
            jobject = jelement.getAsJsonArray().get(i).getAsJsonObject();
            op.add(jobject.get("name").getAsString());
        }

        content = new Scanner(new File(getDataFolder() + "\\spectators.json")).useDelimiter("\\Z").next();
        jelement = new JsonParser().parse(content);

        for(int i = 0; i < jelement.getAsJsonArray().size(); i++) {
            jobject = jelement.getAsJsonArray().get(i).getAsJsonObject();
            spectators.add(jobject.get("name").getAsString());
        }

        content = new Scanner(new File(getDataFolder() + "\\banned.json")).useDelimiter("\\Z").next();
        jelement = new JsonParser().parse(content);

        for(int i = 0; i < jelement.getAsJsonArray().size(); i++) {
            jobject = jelement.getAsJsonArray().get(i).getAsJsonObject();
            banned.put(jobject.get("name").getAsString(), jobject.get("time").getAsInt());
        }

        content = new Scanner(new File(getDataFolder() + "\\teams.json")).useDelimiter("\\Z").next();
        jelement = new JsonParser().parse(content);

        for(int i = 0; i < jelement.getAsJsonArray().size(); i++) {
            jobject = jelement.getAsJsonArray().get(i).getAsJsonObject();

            String name = jobject.get("name").getAsString();
            int lives = jobject.get("lives").getAsInt();
            int lastLogin = jobject.get("lastlogin").getAsInt();

            ChatColor color = ChatColor.BLACK;
            boolean colorFound = false;

            for(int j = 0; j < colors.length && !colorFound; j++) {

                switch (jobject.get("color").getAsString().toLowerCase()) {
                    case "aqua":
                        color = ChatColor.AQUA;
                        colorFound = true;
                        break;
                    case "blue":
                        color = ChatColor.BLUE;
                        colorFound = true;
                        break;
                    case "dark_aqua":
                        color = ChatColor.DARK_AQUA;
                        colorFound = true;
                        break;
                    case "dark_blue":
                        color = ChatColor.DARK_BLUE;
                        colorFound = true;
                        break;
                    case "dark_green":
                        color = ChatColor.DARK_GREEN;
                        colorFound = true;
                        break;
                    case "dark_purple":
                        color = ChatColor.DARK_PURPLE;
                        colorFound = true;
                        break;
                    case "dark_red":
                        color = ChatColor.DARK_RED;
                        colorFound = true;
                        break;
                    case "gold":
                        color = ChatColor.GOLD;
                        colorFound = true;
                        break;
                    case "green":
                        color = ChatColor.GREEN;
                        colorFound = true;
                        break;
                    case "light_purple":
                        color = ChatColor.LIGHT_PURPLE;
                        colorFound = true;
                        break;
                    case "red":
                        color = ChatColor.RED;
                        colorFound = true;
                        break;
                    case "yellow":
                        color = ChatColor.YELLOW;
                        colorFound = true;
                        break;
                    default:
                        color = ChatColor.BLACK;
                }
            }

            ViroTeam team = new ViroTeam(color, name, lives);
            team.setLastLogin(lastLogin);

            JsonObject members;

            for(int j = 0; j < jobject.get("members").getAsJsonArray().size(); j++) {
                members = jobject.get("members").getAsJsonArray().get(j).getAsJsonObject();
                team.addMember(members.get("name").getAsString());
            }

            teams.add(team);
        }
    }

    private void logToFile(String message, String filename)
    {
        try
        {
            File dataFolder = getDataFolder();

            if(!dataFolder.exists())
            {
                dataFolder.mkdir();
            }

            File saveTo = new File(getDataFolder(), filename);

            if (!saveTo.exists())
            {
                saveTo.createNewFile();
            }

            FileWriter fw = new FileWriter(saveTo, false);
            fw.write(message);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        // save config
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();

        obj.put("lives", config.getLives());
        obj.put("banDuration", config.getBanDuration());
        obj.put("playDuration", config.getPlayDuration());
        obj.put("worldborder", config.getWorldborder());
        obj.put("gameRunning", gameRunning);

        arr.add(obj);

        logToFile(arr.toJSONString().replace("[", "").replace("]", ""), "config.json");

        // save teams
        obj = new JSONObject();
        arr = new JSONArray();

        for (ViroTeam t : teams) {
            obj.put("name", t.getName());
            obj.put("color", t.getColor().name());
            obj.put("lives", t.getLives());
            obj.put("lastlogin", t.getLastLogin());

            JSONObject members = new JSONObject();
            JSONArray membersArr = new JSONArray();

            for (int j = 0; j < t.getMembers().size(); j++) {
                members = new JSONObject();
                members.put("name", t.getMembers().get(j));
                membersArr.add(members);
            }

            obj.put("members", membersArr);

            arr.add(obj);

            obj = new JSONObject();
        }

        logToFile(arr.toJSONString(), "teams.json");

        // save ops
        obj = new JSONObject();
        arr = new JSONArray();

        for (String anOp : op) {
            obj.put("name", anOp);
            arr.add(obj);
            obj = new JSONObject();
        }

        logToFile(arr.toJSONString(), "ops.json");

        // save banned players
        obj = new JSONObject();
        arr = new JSONArray();

        for (Map.Entry<String, Integer> entry : banned.entrySet()) {
            obj.put("name", entry.getKey());
            obj.put("time", entry.getValue());
            arr.add(obj);
            obj = new JSONObject();
        }

        logToFile(arr.toJSONString(), "banned.json");
    }

    @Override
    public void onEnable() {

        try {
            loadData();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < 12; i++) {
            colorsUsed[i] = false;
        }

        new Listeners(this);
        getServer().getConsoleSender().sendMessage(PRE_PLUGIN + PRE_SUCC + "Plugin enabled successfully");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getServer().getConsoleSender().sendMessage(PRE_PLUGIN + PRE_SUCC + "Plugin disabled successfully");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {

        if(sender instanceof Player) {
            Player p = (Player) sender;
            String cmdString = cmd.getName();

            if(cmdString.equalsIgnoreCase("info")) {
                p.sendMessage("§6 == §BViro PvP plugin §6==");
                p.sendMessage("§6Version: §F0.1");
                p.sendMessage("§6Author: §FStuntHacks");
                p.sendMessage("Visit §Ngithub.com/stunthacks/viro§R for more information.");

                saveConfig();
                return true;
            }
            else if(cmdString.equalsIgnoreCase("lastwinner")) {
                if (!lastWinner.equalsIgnoreCase("None")) {
                    getServer().broadcastMessage("§BTeam " + lastWinner + " has won the last round!");
                } else {
                    getServer().broadcastMessage("§BNo team has won the last round");
                }

                saveConfig();
                return true;
            }
            else if(cmdString.equalsIgnoreCase("start")) {

                if(p.hasPermission("viro.start")) {

                    if(!gameRunning) {
                        getServer().broadcastMessage("§BViro game starts in 10...");

                        BukkitScheduler countdown9 = getServer().getScheduler();
                        countdown9.scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                getServer().broadcastMessage("§B9...");
                            }
                        }, 20L);

                        BukkitScheduler countdown8 = getServer().getScheduler();
                        countdown8.scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                getServer().broadcastMessage("§B8...");
                            }
                        }, 40L);

                        BukkitScheduler countdown7 = getServer().getScheduler();
                        countdown7.scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                getServer().broadcastMessage("§B7...");
                            }
                        }, 60L);

                        BukkitScheduler countdown6 = getServer().getScheduler();
                        countdown6.scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                getServer().broadcastMessage("§B6...");
                            }
                        }, 80L);

                        BukkitScheduler countdown5 = getServer().getScheduler();
                        countdown5.scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                getServer().broadcastMessage("§B5...");
                            }
                        }, 100L);

                        BukkitScheduler countdown4 = getServer().getScheduler();
                        countdown4.scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                getServer().broadcastMessage("§B4...");
                            }
                        }, 120L);

                        BukkitScheduler countdown3 = getServer().getScheduler();
                        countdown3.scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                getServer().broadcastMessage("§B3...");
                            }
                        }, 140L);

                        BukkitScheduler countdown2 = getServer().getScheduler();
                        countdown2.scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                getServer().broadcastMessage("§B2...");
                            }
                        }, 160L);

                        BukkitScheduler countdown1 = getServer().getScheduler();
                        countdown1.scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                getServer().broadcastMessage("§B1...");
                            }
                        }, 180L);

                        BukkitScheduler start = getServer().getScheduler();
                        start.scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                PlayerInventory inv;

                                for(Player players : getServer().getOnlinePlayers())
                                {
                                    // reset inventory
                                    inv = players.getInventory();
                                    inv.clear();
                                    inv.setHelmet(new ItemStack(Material.AIR));
                                    inv.setChestplate(new ItemStack(Material.AIR));
                                    inv.setLeggings(new ItemStack(Material.AIR));
                                    inv.setBoots(new ItemStack(Material.AIR));
                                    players.updateInventory();

                                    // set worldspawn
                                    World world = getServer().getWorld(p.getWorld().getName());
                                    world.setSpawnLocation(p.getLocation().getBlockX(),
                                            p.getLocation().getBlockY(),
                                            p.getLocation().getBlockZ());

                                    // set worldborder
                                    WorldBorder border = world.getWorldBorder();
                                    border.setSize(config.getWorldborder());
                                    border.setCenter(p.getLocation().getBlockX(), p.getLocation().getBlockZ());

                                    if(op.contains(players.getName())) {
                                        players.setGameMode(GameMode.CREATIVE);
                                    } else if(spectators.contains(players.getName())) {
                                        players.setGameMode(GameMode.SPECTATOR);
                                    } else {
                                        players.setGameMode(GameMode.SURVIVAL);
                                        players.setExp(0);
                                        players.setHealth(20);
                                        players.setFoodLevel(20);
                                    }

                                    players.sendMessage("§BViro game started! Play fair and have fun!");
                                    players.sendMessage("§AYou have a " + config.getPeaceDuration() + " minute long peace period.");

                                    for (ViroTeam team : teams) {
                                        team.setLives(config.getLives());
                                    }

                                    boolean teamFound = false;

                                    for(int i = 0; i  < teams.size() && !teamFound; i++) {

                                        if(teams.get(i).getMembers().contains(players.getName())) {
                                            players.setPlayerListName(teams.get(i).getColor() + players.getName());
                                            teamFound = true;
                                        }
                                    }
                                }

                                for (ViroTeam team : teams) {
                                    team.setLastLogin();
                                }

                                gameStart = (int) (System.currentTimeMillis() / 1000L);
                                gameRunning = true;
                            }
                        }, 200L);
                    } else {
                        p.sendMessage("§CThe game is already running!");
                    }

                    saveConfig();
                    return true;
                }
            }
            else if(cmdString.equalsIgnoreCase("vstop")) {

                if(p.hasPermission("viro.stop")) {

                    if(gameRunning) {
                        banned.clear();
                        lastWinner = "None";
                        gameRunning = false;
                        resetColors();
                        getServer().broadcastMessage("§AThe Viro game was stopped!");
                    } else {
                        p.sendMessage("§CThe game isn't running!");
                    }

                    saveConfig();
                    return true;
                }
            }
            else if(cmdString.equalsIgnoreCase("configuration")) {

                if(p.hasPermission("viro.config")) {

                    if(args.length > 0) {

                        if(!gameRunning) {

                            if (args[0].equalsIgnoreCase("settings")) {
                                p.sendMessage("worldborder, lives, banDuration, playDuration, peaceDuration");

                                saveConfig();
                                return true;
                            } else if (args[0].equalsIgnoreCase("init")) {
                                config.init();
                                p.sendMessage("§AConfig initialized!");

                                saveConfig();
                                return true;
                            } else {

                                if (args.length > 1) {

                                    if (args[0].equalsIgnoreCase("worldborder")) {

                                        try {
                                            int value = Integer.parseInt(args[1]);
                                            config.setWorldborder(value);
                                            p.sendMessage("§AWorldborder size set successfully!");

                                        } catch (Exception e) {
                                            p.sendMessage("§CError: 'worldborder' has to be an Integer");

                                        }
                                    } else if (args[0].equalsIgnoreCase("lives")) {

                                        try {
                                            int value = Integer.parseInt(args[1]);
                                            config.setLives(value);
                                            p.sendMessage("§ALives set successfully!");

                                        } catch (Exception e) {
                                            p.sendMessage("§CError: 'lives' has to be an Integer");

                                        }
                                    } else if (args[0].equalsIgnoreCase("banDuration")) {

                                        try {
                                            double value = Double.parseDouble(args[1]);
                                            config.setBanDuration(value);
                                            p.sendMessage("§ABan duration set successfully!");

                                        } catch (Exception e) {
                                            p.sendMessage("§CError: 'banDuration' has to be a double");

                                        }
                                    } else if (args[0].equalsIgnoreCase("playDuration")) {

                                        try {
                                            double value = Double.parseDouble(args[1]);
                                            config.setPlayDuration(value);
                                            p.sendMessage("§APlay duration set successfully!");

                                        } catch (Exception e) {
                                            p.sendMessage("§CError: 'playDuration' has to be a double");

                                        }
                                    } else if (args[0].equalsIgnoreCase("peaceDuration")) {

                                        try {
                                            double value = Double.parseDouble(args[1]);
                                            config.setPeaceDuration(value);
                                            p.sendMessage("§APeace duration set successfully!");

                                        } catch (Exception e) {
                                            p.sendMessage("§CError: 'peaceDuration' has to be a double");

                                        }
                                    } else {
                                        p.sendMessage("§CUnknown setting. Use '/vconfig settings' for a list of all settings.");
                                    }

                                    saveConfig();
                                    return true;
                                } else {

                                    if (args[0].equalsIgnoreCase("worldborder")) {
                                        p.sendMessage(Integer.toString(config.getWorldborder()));
                                        saveConfig();
                                        return true;

                                    } else if (args[0].equalsIgnoreCase("lives")) {
                                        p.sendMessage(Integer.toString(config.getLives()));
                                        saveConfig();
                                        return true;

                                    } else if (args[0].equalsIgnoreCase("banDuration")) {
                                        p.sendMessage(Double.toString(config.getBanDuration()));
                                        saveConfig();
                                        return true;
                                    } else if (args[0].equalsIgnoreCase("playDuration")) {
                                        p.sendMessage(Double.toString(config.getPlayDuration()));
                                        saveConfig();
                                        return true;
                                    } else if (args[0].equalsIgnoreCase("peaceDuration")) {
                                        p.sendMessage(Double.toString(config.getPeaceDuration()));
                                        saveConfig();
                                        return true;
                                    }
                                }
                            }
                        } else {
                            p.sendMessage("§CThe game is already running");
                            return true;
                        }
                    }
                }
            }
            else if(cmdString.equalsIgnoreCase("operators")) {

                if(p.hasPermission("viro.operators")) {

                    if (args.length > 0) {

                        if(!gameRunning) {

                            if (args[0].equalsIgnoreCase("list")) {
                                String out = "";

                                for (int i = 0; i < op.size(); i++) {
                                    out += op.get(i);

                                    if (i != op.size() - 1)
                                        out += ", ";
                                }

                                p.sendMessage(out);

                                saveConfig();
                                return true;
                            } else if (args[0].equalsIgnoreCase("add")) {

                                if (args.length > 1) {

                                    if (getServer().getPlayer(args[1]) != null) {
                                        boolean found = false;

                                        for (String anOp : op) {
                                            if (anOp.equalsIgnoreCase(args[1])) {
                                                found = true;
                                            }
                                        }

                                        if (!found) {
                                            op.add(args[1]);
                                            p.sendMessage("§APlayer '" + args[1] + "' added to Viro operators");
                                        } else {
                                            p.sendMessage("§APlayer '" + args[1] + "' already is a Viro operator");
                                        }
                                    } else {
                                        p.sendMessage("§CPlayer could not be found");
                                    }

                                    saveConfig();
                                    return true;
                                }
                            } else if (args[0].equalsIgnoreCase("remove")) {

                                if (args.length > 1) {

                                    boolean successful = false;

                                    for (int i = 0; i < op.size(); i++) {
                                        if (op.get(i).equalsIgnoreCase(args[1])) {
                                            op.remove(i);
                                            successful = true;
                                        }
                                    }

                                    if (successful)
                                        p.sendMessage("§APlayer '" + args[1] + "' removed from Viro operators");
                                    else
                                        p.sendMessage("§APlayer '" + args[1] + "' isn't a Viro operator");

                                    saveConfig();
                                    return true;
                                }
                            }
                        } else {
                            p.sendMessage("§CThe game is already running");
                        }
                    }
                }
            }
            else if(cmdString.equalsIgnoreCase("spectators")) {

                if(p.hasPermission("viro.spectators")) {

                    if (args.length > 0) {

                        if(!gameRunning) {

                            if (args[0].equalsIgnoreCase("list")) {
                                String out = "";

                                for (int i = 0; i < spectators.size(); i++) {
                                    out += spectators.get(i);

                                    if (i != spectators.size() - 1)
                                        out += ", ";
                                }

                                p.sendMessage(out);

                                return true;
                            } else if (args[0].equalsIgnoreCase("add")) {

                                if (args.length > 1) {

                                    if (getServer().getPlayer(args[1]) != null) {
                                        boolean found = false;

                                        for (String spectator : spectators) {
                                            if (spectator.equalsIgnoreCase(args[1])) {
                                                found = true;
                                            }
                                        }

                                        if (!found) {
                                            spectators.add(args[1]);
                                            p.sendMessage("§APlayer '" + args[1] + "' added to Viro spectators");
                                        } else {
                                            p.sendMessage("§APlayer '" + args[1] + "' already is a Viro spectator");
                                        }
                                    } else {
                                        p.sendMessage("§CPlayer could not be found");
                                    }

                                    saveConfig();
                                    return true;
                                }
                            } else if (args[0].equalsIgnoreCase("remove")) {

                                if (args.length > 1) {

                                    boolean successful = false;

                                    for (int i = 0; i < spectators.size(); i++) {
                                        if (spectators.get(i).equalsIgnoreCase(args[1])) {
                                            spectators.remove(i);
                                            successful = true;
                                        }
                                    }

                                    if (successful)
                                        p.sendMessage("§APlayer '" + args[1] + "' removed from Viro spectators");
                                    else
                                        p.sendMessage("§APlayer '" + args[1] + "' isn't a Viro spectator");

                                    saveConfig();
                                    return true;
                                }
                            }
                        } else {
                            p.sendMessage("§CThe game is already running");
                        }
                    }
                }
            }
            else if(cmdString.equalsIgnoreCase("teams")) {

                if(p.hasPermission("viro.teams")) {

                    if (args.length > 0) {

                        if(!gameRunning) {

                            if (args[0].equalsIgnoreCase("list")) {

                                if(args.length > 1) {
                                    boolean found = false;

                                    for(int i = 0; i < teams.size() && !found; i++) {

                                        if(teams.get(i).getName().equalsIgnoreCase(args[1])) {
                                            found = true;
                                            String out = "";

                                            for (int j = 0; j < teams.get(i).getMembers().size(); j++) {
                                                out += teams.get(i).getMembers().get(j);

                                                if (j != teams.get(i).getMembers().size() - 1)
                                                    out += ", ";
                                            }

                                            p.sendMessage(out);
                                        }
                                    }

                                    if(!found) {
                                        p.sendMessage("§CTeam not found!");
                                    }
                                } else {
                                    String out = "";

                                    for (int i = 0; i < teams.size(); i++) {
                                        out += teams.get(i).getColor() + teams.get(i).getName() + "§R";

                                        if (i != teams.size() - 1)
                                            out += ", ";
                                    }

                                    p.sendMessage(out);
                                }

                                saveConfig();
                                return true;
                            }
                            else if (args[0].equalsIgnoreCase("create")) {

                                if (args.length > 1) {

                                    if (teams.size() < 12) {
                                        boolean colorFound = false, found = false;
                                        ChatColor color = ChatColor.AQUA;

                                        for (ViroTeam team : teams) {

                                            if (team.getName().equalsIgnoreCase(args[1]))
                                                found = true;
                                        }

                                        if (!found) {

                                            if (args.length > 2) {
                                                switch (args[2].toLowerCase()) {
                                                    case "aqua":
                                                        color = ChatColor.AQUA;
                                                        colorFound = true;
                                                        break;
                                                    case "blue":
                                                        color = ChatColor.BLUE;
                                                        colorFound = true;
                                                        break;
                                                    case "dark_aqua":
                                                        color = ChatColor.DARK_AQUA;
                                                        colorFound = true;
                                                        break;
                                                    case "dark_blue":
                                                        color = ChatColor.DARK_BLUE;
                                                        colorFound = true;
                                                        break;
                                                    case "dark_green":
                                                        color = ChatColor.DARK_GREEN;
                                                        colorFound = true;
                                                        break;
                                                    case "dark_purple":
                                                        color = ChatColor.DARK_PURPLE;
                                                        colorFound = true;
                                                        break;
                                                    case "dark_red":
                                                        color = ChatColor.DARK_RED;
                                                        colorFound = true;
                                                        break;
                                                    case "gold":
                                                        color = ChatColor.GOLD;
                                                        colorFound = true;
                                                        break;
                                                    case "green":
                                                        color = ChatColor.GREEN;
                                                        colorFound = true;
                                                        break;
                                                    case "light_purple":
                                                        color = ChatColor.LIGHT_PURPLE;
                                                        colorFound = true;
                                                        break;
                                                    case "red":
                                                        color = ChatColor.RED;
                                                        colorFound = true;
                                                        break;
                                                    case "yellow":
                                                        color = ChatColor.YELLOW;
                                                        colorFound = true;
                                                        break;
                                                    default:
                                                        p.sendMessage("§CUnknown color '" + args[2] + "'. Will auto-choose one.");
                                                }

                                                if (colorFound) {

                                                    for (int i = 0; i < colors.length; i++) {

                                                        if (colors[i].equals(color)) {

                                                            if (colorsUsed[i]) {
                                                                p.sendMessage("§CColor '" + args[2] + "' already in use. Will auto-choose one.");
                                                                colorFound = false;
                                                            } else {
                                                                colorsUsed[i] = true;
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            if (!colorFound) {

                                                for (int i = 0; i < colorsUsed.length && !colorFound; i++) {

                                                    if (!colorsUsed[i]) {
                                                        color = colors[i];
                                                        colorsUsed[i] = true;
                                                        colorFound = true;
                                                    }
                                                }
                                            }

                                            ViroTeam t = new ViroTeam(color, args[1], config.getLives());
                                            teams.add(t);

                                            p.sendMessage("§ATeam '" + t.getColor() + t.getName() + "§A' created successfully!");
                                        } else {
                                            p.sendMessage("§CTeam '" + args[1] + "' already exists");
                                        }

                                    } else {
                                        p.sendMessage("§CThe maximum amount of teams has already been reached!");
                                    }

                                    saveConfig();
                                    return true;
                                }
                            } else if (args[0].equalsIgnoreCase("delete")) {

                                if (args.length > 1) {
                                    boolean found = false;

                                    for (int i = 0; i < teams.size(); i++) {

                                        if (teams.get(i).getName().equalsIgnoreCase(args[1])) {
                                            found = true;
                                            ChatColor c = teams.get(i).getColor();
                                            String name = teams.get(i).getName();

                                            for (int j = 0; j < colors.length; j++) {

                                                if (colors[j].equals(c)) {
                                                    colorsUsed[j] = false;
                                                }
                                            }

                                            teams.remove(i);

                                            p.sendMessage("§ATeam '" + c + name + "§A' deleted successfully!");
                                        }
                                    }

                                    if (!found) {
                                        p.sendMessage("§CTeam '" + args[1] + "' doesn't exist");
                                    }

                                    return true;
                                }
                            }
                            else if (args[0].equalsIgnoreCase("add")) {

                                if (args.length > 2) {
                                    boolean found = false;
                                    int teamId = 0;

                                    for (int i = 0; i < teams.size() && !found; i++) {

                                        if (teams.get(i).getName().equalsIgnoreCase(args[1])) {
                                            found = true;
                                            teamId = i;
                                        }
                                    }

                                    if (!found) {
                                        p.sendMessage("§CTeam '" + args[1] + "' doesn't exist");
                                    } else {
                                        boolean alreadyInTeam = false;

                                        for (int i = 0; i < teams.size() && !alreadyInTeam; i++) {

                                            if (teams.get(i).getMembers().contains(args[2])) {
                                                alreadyInTeam = true;
                                            }
                                        }

                                        if (!alreadyInTeam) {
                                            teams.get(teamId).addMember(args[2]);

                                            p.sendMessage("§APlayer '" + args[2] + "' added to team '"
                                                    + teams.get(teamId).getColor() + teams.get(teamId).getName() + "§A' successfully!");
                                        } else {
                                            p.sendMessage("§CPlayer '" + args[2] + "' already is in a team!");
                                        }
                                    }

                                    saveConfig();
                                    return true;
                                }
                            }
                            else if (args[0].equalsIgnoreCase("remove")) {

                                if (args.length > 2) {
                                    boolean found = false;
                                    int teamId = 0;

                                    for (int i = 0; i < teams.size() && !found; i++) {

                                        if (teams.get(i).getName().equalsIgnoreCase(args[1])) {
                                            found = true;
                                            teamId = i;
                                        }
                                    }

                                    if (!found) {
                                        p.sendMessage("§CTeam '" + args[1] + "' doesn't exist");
                                    } else {

                                        if (teams.get(teamId).getMembers().contains(args[2])) {
                                            teams.get(teamId).removeMember(args[2]);
                                            p.sendMessage("§APlayer '" + args[2] + "' removed from team '"
                                                    + teams.get(teamId).getColor() + teams.get(teamId).getName() + "§A' successfully!");
                                        } else {
                                            p.sendMessage("§CPlayer '" + args[2] + "' is not in team '"
                                                    + teams.get(teamId).getColor() + teams.get(teamId).getName() + "§C'!");
                                        }
                                    }

                                    saveConfig();
                                    return true;
                                }
                            }
                        } else {
                            p.sendMessage("§CThe game is already running");
                            return true;
                        }
                    }
                }
            }
        }

        saveConfig();
        return false;
    }
}
