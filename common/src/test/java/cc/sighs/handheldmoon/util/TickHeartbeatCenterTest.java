package cc.sighs.handheldmoon.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TickHeartbeatCenterTest {
    @Test
    void keepsHeartbeatAliveForCurrentAndFollowingTick() {
        TickHeartbeatCenter<String, String> center = new TickHeartbeatCenter<>();

        center.report("overworld", 10L, "moon");

        assertTrue(center.isAlive("overworld", 10L, "moon"));
        assertTrue(center.isAlive("overworld", 11L, "moon"));
        assertFalse(center.isAlive("overworld", 12L, "moon"));
    }

    @Test
    void separatesScopesAndRejectsNullIds() {
        TickHeartbeatCenter<String, String> center = new TickHeartbeatCenter<>();

        center.report("overworld", 10L, "moon");

        assertFalse(center.isAlive("nether", 10L, "moon"));
        assertFalse(center.isAlive("overworld", 10L, null));
    }
}
