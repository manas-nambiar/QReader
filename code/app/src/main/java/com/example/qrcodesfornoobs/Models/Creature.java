package com.example.qrcodesfornoobs.Models;

import android.location.Location;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Represents a creature derived from a string
 */
public class Creature {
    private String name; // const
    private String hash; // const
    private int score; // const
    private int numOfScans = 1; // update every scan
    private String photoCreatureUrl; // const
    private Double latitude;
    private Double longitude;
    private String locationName;
    private String geoHash;
    private String photoLocationUrl; // update every scan
    private ArrayList<String> comments = new ArrayList<>(); // update every comment

    /**
     * Constructor for a Creature when it is scanned for the first time. Will generate score, name and
     * hash.
     * @param code code received from the QRCode scan.
     */
    public Creature (String code) {
        //this will be used when we scan a code
        //set hash
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Change this to UTF-16 if needed
            md.update(code.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            hash = String.format("%064x", new BigInteger(1, digest));
            // Calculating the score
            calcScore(hash);
            // Generate a name
            genName(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    /**
     * Constructor for a Creature when it has been already scanned, all Creature attributes will
     * already exists. No need to regenerate name, score, visual representation.
     * **location to be implemented in part4
     * @param name String Value
     * @param latitude Latitude double
     * @param longitude Longitude double
     * @param locationName String of location's name
     * @param comments list of String
     * @param hash String
     * @param geoHash String of geolocation hash
     * @param score int
     * @param numOfScans int that represent how many players have scanned a Creature
     */
    public Creature(String name, String hash, int score, int numOfScans, Double latitude, Double longitude, String locationName, String geoHash, ArrayList<String> comments){
        //this will be used when creature is already in database
        this.name = name;
        this.hash = hash;
        this.score = score;
        this.numOfScans = numOfScans;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.geoHash = geoHash;
        this.comments = comments;
    }

    // NO ARGUMENT CONSTRUCTOR -> USED SO THAT SOMETHING DOESNT BREAK I GUESS IDK
    // Was running into this issue:
    // https://stackoverflow.com/questions/60389906/could-not-deserialize-object-does-not-define-a-no-argument-constructor-if-you
    public Creature(){}

    /**
     * Function to generate a name with a given hash.
     * @param hash
     * @see Creature
     */
    public void genName(String hash){
        name = "";
        for(int i = 0; i < 4; i++){
            int b = Integer.decode("0x"+hash.charAt(i));
            switch(b){
                case 0: name = name.concat("Ha"); break;
                case 1: name = name.concat("Mo"); break;
                case 2: name = name.concat("Chi"); break;
                case 3: name = name.concat("Go"); break;
                case 4: name = name.concat("Na"); break;
                case 5: name = name.concat("Li"); break;
                case 6: name = name.concat("Tri"); break;
                case 7: name = name.concat("Ca"); break;
                case 8: name = name.concat("Pen"); break;
                case 9: name = name.concat("Kal"); break;
                case 10: name = name.concat("Vee"); break;
                case 11: name = name.concat("Ri"); break;
                case 12: name = name.concat("Tee"); break;
                case 13: name = name.concat("Wa"); break;
                case 14: name = name.concat("Sa"); break;
                case 15: name = name.concat("Ya"); break;
            }
        }

        // TODO: Image functionality
    }
    /**
     * Function to generate the score with a given hash.
     * @param hash
     * @see Creature
     */
    public void calcScore(String hash){
        int count = 0;
        int prev = -1;
        score = 0;
        for (int i = 0; i < hash.length(); i++){
            int b = Integer.decode("0x"+hash.charAt(i));
            if (b == 0) {b = 20;} // special case of 0 which scales by 20 points.
            if (prev == b){
                count *= b; // multiply when current digit is same and the previous digit
            } else {
                score += count; // Add the current total to the current score
                count = 1;  //reset count value to 1
                prev = b;
            }
        }
        score += count; // Final addition for the end of the loop.
    }
    /**
     * Getter for a Creature's hash value.
     * @return String, assigned hash of a Creature.
     * @see Creature
     */
    public String getHash() {
        return hash;
    }
    /**
     * Getter for a Creature's name value.
     * @return String, assigned name of a Creature.
     * @see Creature
     */
    public String getName() {
        return name;
    }
    /**
     * Getter for a Creature's score value.
     * @return int, assigned score of a Creature.
     * @see Creature
     */
    public int getScore() {
        return score;
    }
    /**
     * Getter for a Creature's photoCreatureUrl value.
     * @return String, assigned photoCreatureUrl of a Creature.
     * @see Creature
     */
    public String getPhotoCreatureUrl() {
        return photoCreatureUrl;
    }
    /**
     * Getter for a Creature's photoLocationUrl value. **implemented in part4
     * @return String, assigned photoLocationUrl of a Creature.
     * @see Creature
     */
    public String getPhotoLocationUrl() {
        return photoLocationUrl;
    }
    /**
     * Getter for a Creature's latitude value. **implemented in part4
     * @return Location, assigned location of a Creature.
     * @see Creature
     */
    public Double getLatitude() {
        return latitude;
    }
    /**
     * Getter for a Creature's latitude value. **implemented in part4
     * @return Location, assigned location of a Creature.
     * @see Creature
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Getter for a Creature's location name.
     * @return String, location name.
     * @see Creature
     */
    public String getLocationName(){
        return locationName;
    }
    /**
     * Getter for Creature's geolocation hash.
     * @return String, geolocation hash.
     * @see Creature
     */
    public String getGeoHash(){return geoHash;}
    /**
     * Getter for a Creature's location value. **implemented in part4
     * @return List of Creature's comments.
     * @see Creature
     */
    public ArrayList<String> getComments() {return comments;}
    /**
     * Getter for a Creature's numOfScans value.
     * @return int, assigned numOfScans of a Creature.
     * @see Creature
     */
    public int getNumOfScans() {
        return numOfScans;
    }

    /**
     * Setter for a Creature's coordinates.
     * Takes the latitude and longitude of the location said creature was found.
     * @param latitude latitude value
     * @see Creature
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    /**
     * Setter for a Creature's coordinates.
     * Takes the latitude and longitude of the location said creature was found.
     * @param longitude longitude value
     * @see Creature
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    /**
     * Setter for a Creature's location name.
     * @param locationName, String value
     * @see Creature
     */
    public void setLocationName(String locationName){
        this.locationName = locationName;
    }
    /**
     * Setter for a Creature's geolocation hash.
     * @param geoHash, String
     * @see Creature
     */
    public void setGeoHash(String geoHash){this.geoHash = geoHash;}
    /**
     * Setter for a Creature's photoCreatureUrl value.
     * @param photoCreatureUrl, String value
     * @see Creature
     */
    public void setPhotoCreatureUrl(String photoCreatureUrl) {
        this.photoCreatureUrl = photoCreatureUrl;
    }
    /**
     * Setter for a Creature's photoLocationUrl value.
     * @param photoLocationUrl, String value
     * @see Creature
     */
    public void setPhotoLocationUrl(String photoLocationUrl) {
        this.photoLocationUrl = photoLocationUrl;
    }
    /**
     * Function to increment a Creature's numOfScans value by 1.
     * @see Creature
     */
    public void incrementScan() {
        this.numOfScans++;
    }
    /**
     * Function to add a comment to Creature's comments array.
     * @param comment, String value
     * @see Creature
     */
    public void addComment(String comment){
        comments.add(comment);
    }
    /**
     * Function to remove a comment from Creature's comments array.
     * @param comment, String value
     * @see Creature
     */
    public void removeComment(String comment){
        comments.remove(comment);
    }

}