package edu.uclm.reper.services;

public interface VirusScanner {
    boolean isEnabled();

    ScanResult scan(byte[] data);

    record ScanResult(boolean clean, String signature) {}
}
