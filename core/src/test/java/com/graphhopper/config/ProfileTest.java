package com.graphhopper.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProfileTest {
    Profile instance = new Profile("name");

    @Test
    public void testValidateProfileName() {

        assertThrows(IllegalArgumentException.class, () -> {
            Profile.validateProfileName("#not_ok?");
        });

        assertDoesNotThrow(()-> {Profile.validateProfileName("ok_name");});
    }

    @Test
    public void testPutHint() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance.putHint("u_turn_costs", "whatever");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            instance.putHint("vehicle", "whatever");
        });
    }

    @Test
    public void testEquals() {
        String otherClass = "not null, just another class";
        String nullObject = null;
        Profile sameRef = instance;
        Object sameName = new Profile("name");

        assertNotEquals(otherClass, instance.getName());
        assertNotEquals(nullObject, instance.getName());
        assertEquals(instance, sameRef);
        assertEquals(instance, sameName);
    }
}
