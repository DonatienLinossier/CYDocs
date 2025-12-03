package com.cyFramework.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    @Test
    void testCreationLogger() {
        Logger log = new Logger("TestLogger");

        assertNotNull(log);
        assertEquals("TestLogger", log.getNom());
    }
}
