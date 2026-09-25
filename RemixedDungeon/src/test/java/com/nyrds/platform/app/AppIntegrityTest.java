package com.nyrds.platform.app;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AppIntegrityTest {

    @Test
    public void testSha256Hex() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                AppIntegrity.sha256Hex(new byte[0]));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                AppIntegrity.sha256Hex("abc".getBytes()));
    }

    @Test
    public void testDeriveRepacked() {
        // legit install, cert configured and trusted
        assertFalse(AppIntegrity.deriveRepacked(AppIntegrity.PACKER_NONE, true, true));
        // dev build without configured cert - signature verdict must stay quiet
        assertFalse(AppIntegrity.deriveRepacked(AppIntegrity.PACKER_NONE, false, false));
        assertFalse(AppIntegrity.deriveRepacked(AppIntegrity.PACKER_NONE, false, true));
        // re-signed without a packer - only the cert check catches it
        assertTrue(AppIntegrity.deriveRepacked(AppIntegrity.PACKER_NONE, true, false));
        // any packer fingerprint is unconditional
        assertTrue(AppIntegrity.deriveRepacked("stubapp", true, true));
        assertTrue(AppIntegrity.deriveRepacked("jiagu:maps", false, false));
        assertTrue(AppIntegrity.deriveRepacked("jiagu:datadir", true, false));
    }

    @Test
    public void testReportDefaults() {
        AppIntegrity.Report report = new AppIntegrity.Report(
                "unknown", false, false, "unknown", AppIntegrity.PACKER_NONE);
        assertFalse(report.repacked);
        assertEquals(AppIntegrity.PACKER_NONE, report.packer);
    }
}
