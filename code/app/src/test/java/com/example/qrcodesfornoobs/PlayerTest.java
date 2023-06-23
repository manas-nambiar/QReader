package com.example.qrcodesfornoobs;

import static org.junit.Assert.*;

import com.example.qrcodesfornoobs.Models.Creature;
import com.example.qrcodesfornoobs.Models.Player;

import org.junit.Test;

public class PlayerTest {

    private Player mockPlayer() {
        return new Player("Test", "123456");
    }

    @Test
    public void testCreatureCollection() {
        Player testPlayer = mockPlayer();
        Creature testCreature = new Creature("test");

        testPlayer.addCreature(testCreature);
        assertEquals(1, testPlayer.getCreatures().size());
        assertTrue(testPlayer.containsCreature(testCreature));

        testPlayer.removeCreature(testCreature);
        assertEquals(0, testPlayer.getCreatures().size());
        assertFalse(testPlayer.containsCreature(testCreature));
    }

    @Test
    public void testEquals(){
        Player testPlayer1 = mockPlayer();
        Player testPlayer2 = mockPlayer();
        Player testPlayer3 = new Player("Fail", null);
        Player testPlayer4 = new Player("test", "somewhere");

        assertEquals(testPlayer1, testPlayer2);
        assertNotEquals(testPlayer1, testPlayer3);
        assertNotEquals(testPlayer1, testPlayer4);
    }
}
