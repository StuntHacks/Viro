package com.stunthacks.viro;

/**
 * Created by StuntHacks on 22.06.2017.
 */
class Config {
    private int worldborder, lives;
    private double banDuration, playDuration, peaceDuration;

    Config() {
        worldborder = 200;
        lives = 15;
        banDuration = 12;
        playDuration = 15;
        peaceDuration = 15;
    }

    void init() {
        worldborder = 200;
        lives = 15;
        banDuration = 12;
        playDuration = 15;
        peaceDuration = 15;
    }

    int getWorldborder() {
        return worldborder;
    }

    void setWorldborder(int worldborder) {
        this.worldborder = worldborder;
    }

    int getLives() {
        return lives;
    }

    void setLives(int lives) {
        this.lives = lives;
    }

    double getBanDuration() {
        return banDuration;
    }

    void setBanDuration(double banDuration) {
        this.banDuration = banDuration;
    }

    double getPlayDuration() {
        return playDuration;
    }

    void setPlayDuration(double playDuration) {
        this.playDuration = playDuration;
    }

    double getPeaceDuration() {
        return peaceDuration;
    }

    void setPeaceDuration(double peaceDuration) {
        this.peaceDuration = peaceDuration;
    }
}
