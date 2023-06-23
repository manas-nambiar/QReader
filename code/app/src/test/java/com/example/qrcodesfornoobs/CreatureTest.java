package com.example.qrcodesfornoobs;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.qrcodesfornoobs.Models.Creature;

import java.security.NoSuchAlgorithmException;

public class CreatureTest {

    private Creature mockHash() throws NoSuchAlgorithmException {
        return new Creature("test");
    }

    @Test
    public void testName() throws NoSuchAlgorithmException {
        Creature test = mockHash();

        test.genName("0000");
        assertEquals("HaHaHaHa", test.getName());
    }

    @Test
    public void testScore() throws NoSuchAlgorithmException {
        Creature test = mockHash();

        test.calcScore("12345678900");
        assertEquals(29, test.getScore());
    }

    @Test
    public void testCreate() throws NoSuchAlgorithmException {
        Creature test = mockHash();
        assertEquals("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
                        test.getHash());

        assertEquals(99, test.getScore());
        assertEquals("KalYaPenTri", test.getName());
    }

    @Test
    public void testScanCount() throws NoSuchAlgorithmException {
        Creature test = mockHash();

        assertEquals(1, test.getNumOfScans());
        test.incrementScan();
        assertEquals(2, test.getNumOfScans());
    }

    @Test
    public void testComments() throws NoSuchAlgorithmException {
        Creature test = mockHash();

        assertEquals(0, test.getComments().size());
        test.addComment("This is a comment.");
        assertEquals(1, test.getComments().size());
        test.removeComment("This is a comment.");
        assertEquals(0, test.getComments().size());
    }
}
