package com.creeperpastel.client;

/** Estado del "agarre" del dron en el cliente. */
public class DroneClientState {
    public static boolean active = false;
    public static int droneId = -1;
    public static float progress = 0f;
    public static long lastPressMs = 0;
    public static int missingTicks = 0;

    public static boolean isLocked() {
        return active;
    }

    public static void start(int id) {
        if (!active) progress = 0f;
        active = true;
        droneId = id;
        missingTicks = 0;
    }

    public static void stop() {
        active = false;
        progress = 0f;
        droneId = -1;
    }
}
