package com.stunthacks.viro;

import org.bukkit.ChatColor;

import java.util.ArrayList;

/**
 * Created by StuntHacks on 22.06.2017.
 */
public class ViroTeam {
    private ChatColor color;
    private String name;
    private int lives, lastLogin;
    private ArrayList<String> members = new ArrayList<>();

    ViroTeam(ChatColor color, String name, int lives) {
        this.color = color;
        this.name = name;
        this.lives = lives;
        this.lastLogin = -1;
    }

    ChatColor getColor() {
        return color;
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    int getLives() {
        return lives;
    }

    void setLives(int lives) {
        this.lives = lives;
    }

    public void addLive() { this.lives++; }

    void removeLive() { this.lives--; }

    ArrayList<String> getMembers() {
        return members;
    }

    void addMember(String p) {
        members.add(p);
    }

    void removeMember(String p) {
        members.remove(p);
    }

    void setLastLogin() { this.lastLogin = (int) (System.currentTimeMillis() / 1000L); }

    void setLastLogin(int time) { this.lastLogin = time; }

    int getLastLogin() { return lastLogin; }
}
