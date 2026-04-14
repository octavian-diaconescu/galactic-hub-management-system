package com.octavian.galactic.support;

import org.testcontainers.DockerClientFactory;

/**
 * Conditions for optional test prerequisites (used by JUnit {@code @EnabledIf}).
 */
public final class TestEnvironment {

    private TestEnvironment() {
    }

    /**
     * @return true if Testcontainers can reach a Docker daemon
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean dockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
