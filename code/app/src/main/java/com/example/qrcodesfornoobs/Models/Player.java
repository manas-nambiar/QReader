package com.example.qrcodesfornoobs.Models;

import java.util.ArrayList;
import java.util.Objects;

/**
 * The Player class represents a player in the game with a unique username and device.
 * It also maintains a score, a list of creatures, and a contact for the player.
 */
public class Player {
    public static String LOCAL_USERNAME = null; // only set if user has signed in before
    //TODO: Call to db for Creatures
    private String username;
    private String device;
    private int score = 0;
    private ArrayList<String> creatures = new ArrayList<>();
    private String contact;

    /**
     * Constructs a new player with the specified username and device.
     * @param username the username of the player.
     * @param device the device associated with the player.
     */
    public Player(String username, String device) {
        this.username = username;
        this.device = device;
    }

    /**
     * Constructs a new player with default values.
     */
    // addressing https://stackoverflow.com/questions/60389906/could-not-deserialize-object-does-not-define-a-no-argument-constructor-if-you
    public Player() {

    }

    /**
     * 2 Player are considered equal if they have the same username and device
     * @param o player to be compared with
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return username.equals(player.username) && device.equals(player.device);
    }

    /**
     * Returns a hash code for this player.
     * @return a hash code for this player.
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, device);
    }

    /**
     * Adds a creature to the player's list of creatures.
     * @param creature the creature to add.
     */
    public void addCreature(Creature creature) {
        creatures.add(creature.getHash());
    }

    /**
     * Removes a creature from the player's list of creatures.
     * @param creature the creature to remove.
     */
    public void removeCreature(Creature creature) {
        creatures.remove(creature.getHash());
    }

    /**
     * Removes the creature at the specified index from the player's list of creatures.
     * @param i the index of the creature to remove.
     */
    public void removeCreature(int i) {
        creatures.remove(i);
    }

    /**
     * Returns the username of the player.
     * @return the username of the player.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the device associated with the player.
     * @return the device associated with the player.
     */
    public String getDevice(){
        return device;
    }

    /**
     * Returns the contact information for the player.
     * @return the contact information for the player.
     */
    public String getContact() {
        return contact;
    }

    public int getScore() {
        return score;
    }
    /**
     * Sets the contact information for the player.
     * @param contact The contact to be set for the player.
     */
    public void setContact(String contact){this.contact = contact;}
    /**
     * Returns the list of creatures associated with the player.
     * @return the list of creatures associated with the player.
     */
        public ArrayList<String> getCreatures() {
        return creatures;
    }

    /**
     * Sets the score of the player.
     *
     * @param score The score to be set for the player.
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Checks if the player has a certain creature.
     *
     * @param creature The Creature object to check.
     * @return true if the player has the creature, false otherwise.
     */
    public boolean containsCreature(Creature creature) {
        return creatures.contains(creature.getHash());
    }

}


