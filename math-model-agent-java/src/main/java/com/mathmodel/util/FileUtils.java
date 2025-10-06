package com.mathmodel.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * File Utility Class
 */
@Slf4j
public class FileUtils {

    /**
     * Create directories recursively
     */
    public static void createDirectories(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.debug("Created directory: {}", path);
        }
    }

    /**
     * Write content to file
     */
    public static void writeFile(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
        log.debug("Wrote file: {}", path);
    }

    /**
     * Read file content
     */
    public static String readFile(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /**
     * List files in directory
     */
    public static List<Path> listFiles(Path directory) throws IOException {
        List<Path> files = new ArrayList<>();
        if (Files.exists(directory) && Files.isDirectory(directory)) {
            try (var stream = Files.list(directory)) {
                stream.filter(Files::isRegularFile).forEach(files::add);
            }
        }
        return files;
    }

    /**
     * List files with extension
     */
    public static List<Path> listFilesWithExtension(Path directory, String extension) throws IOException {
        List<Path> files = new ArrayList<>();
        if (Files.exists(directory) && Files.isDirectory(directory)) {
            try (var stream = Files.list(directory)) {
                stream.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(extension))
                        .forEach(files::add);
            }
        }
        return files;
    }

    /**
     * Delete directory recursively
     */
    public static void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            log.debug("Deleted directory: {}", directory);
        }
    }

    /**
     * Copy file
     */
    public static void copyFile(Path source, Path target) throws IOException {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Copied file from {} to {}", source, target);
    }

    /**
     * Get file extension
     */
    public static String getFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1) : "";
    }

    /**
     * Check if file exists
     */
    public static boolean exists(Path path) {
        return Files.exists(path);
    }

    /**
     * Get file size
     */
    public static long getFileSize(Path path) throws IOException {
        return Files.size(path);
    }
}
