package ru.mousecray.mouseth;

public class MouseTH {
    private static boolean sessionActive = false;
    public static void enableForSession() {
        sessionActive = true;
    }
    public static boolean isSessionActive() {
        return sessionActive;
    }
}