package ru.mousecray.mouseth.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportBackupHelper {
    private static final DateTimeFormatter BACKUP_FORMATTER  = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static boolean hasExistingData(Path dir) {
        if (!Files.isDirectory(dir)) return false;
        try (Stream<Path> stream = Files.list(dir)) { return stream.findAny().isPresent(); } catch (IOException e) { return false; }
    }

    public static boolean hasExistingDatapackData(Path worldDir, Path rootMouseTH) {
        Path dpDir = worldDir.resolve("datapacks");
        if (!Files.isDirectory(dpDir) || !Files.isDirectory(rootMouseTH)) return false;

        try (Stream<Path> stream = Files.list(dpDir)) {
            return stream.anyMatch(packPath -> {
                String packName      = packPath.getFileName().toString();
                String packId        = packName.endsWith(".zip") ? packName.substring(0, packName.length() - 4) : packName;
                Path   packTargetDir = rootMouseTH.resolve(packId);
                return hasExistingData(packTargetDir);
            });
        } catch (IOException e) { return false; }
    }

    public static void createBackup(Path sourceDir, Path mdataDir) {
        if (!hasExistingData(sourceDir)) return;

        Path   backupsDir = mdataDir.resolve("backups");
        String timestamp  = LocalDateTime.now().format(BACKUP_FORMATTER);
        Path   zipFile    = backupsDir.resolve("backup_" + timestamp + ".zip");

        try {
            Files.createDirectories(backupsDir);
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile));
                 Stream<Path> walk = Files.walk(sourceDir)) {
                walk.filter(p -> !Files.isDirectory(p)).forEach(path -> {
                    String entryName = sourceDir.relativize(path).toString().replace('\\', '/');
                    try {
                        zos.putNextEntry(new ZipEntry(entryName));
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException ignored) { }
                });
            }
        } catch (IOException ignored) { }
    }

    public static void saveLastExportTime(Path mdataDir, String fileName) {
        try {
            Files.createDirectories(mdataDir);
            Path   file      = mdataDir.resolve(fileName);
            String timestamp = LocalDateTime.now().format(DISPLAY_FORMATTER);
            Files.writeString(file, timestamp, StandardCharsets.UTF_8);
        } catch (IOException ignored) { }
    }

    public static String getLastExportTime(Path mdataDir, String fileName) {
        Path file = mdataDir.resolve(fileName);
        if (Files.exists(file)) {
            try { return Files.readString(file, StandardCharsets.UTF_8).trim(); } catch (IOException ignored) { }
        }
        return null;
    }
}