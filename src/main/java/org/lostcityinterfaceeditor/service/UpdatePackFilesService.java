package org.lostcityinterfaceeditor.service;

import org.lostcityinterfaceeditor.models.ApplicationState;
import org.lostcityinterfaceeditor.models.InterfaceComponent;
import org.lostcityinterfaceeditor.ui.widget.Widgets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UpdatePackFilesService {

    private final ApplicationState applicationState;

    public UpdatePackFilesService(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    public void updateInterfacePackFiles(File savedInterfaceFile, List<InterfaceComponent> componentsToRegister) {
        if (applicationState.getServerDirectoryPath() == null || applicationState.getServerDirectoryPath().isEmpty() || savedInterfaceFile == null || componentsToRegister == null) {
            System.out.println("Skipping pack file update: Missing server directory, saved file, or component list.");
            return;
        }

        Path packFilePath = Paths.get(applicationState.getServerDirectoryPath(), "pack", "interface.pack");
        Path orderFilePath = Paths.get(applicationState.getServerDirectoryPath(), "pack", "interface.order");
        Path interfaceDirectoryPath = savedInterfaceFile.getParentFile().toPath();
        Path expectedInterfaceBasePath = Paths.get(applicationState.getServerDirectoryPath());

        if (!interfaceDirectoryPath.startsWith(expectedInterfaceBasePath)) {
            System.out.println("Skipping pack file update: Saved file '" + savedInterfaceFile.getName() + "' is not within the expected server directory structure.");
            return;
        }

        String interfaceFileName = savedInterfaceFile.getName();
        if (interfaceFileName.toLowerCase().endsWith(".if")) {
            interfaceFileName = interfaceFileName.substring(0, interfaceFileName.length() - 3);
        }

        System.out.println("Checking and potentially updating pack files for interface: " + interfaceFileName);

        try {
            int nextNumber = 0;
            Set<String> existingEntries = new HashSet<>();
            if (Files.exists(packFilePath)) {
                List<String> packLines = Files.readAllLines(packFilePath);
                for (String line : packLines) {
                    line = line.trim();
                    if (line.isEmpty() || !line.contains("=")) continue;
                    try {
                        int number = Integer.parseInt(line.substring(0, line.indexOf('=')));
                        if (number >= nextNumber) {
                            nextNumber = number + 1;
                        }
                        String entry = line.substring(line.indexOf('=') + 1);
                        existingEntries.add(entry);
                    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                        System.err.println("Warning: Skipping malformed line in interface.pack: " + line);
                    }
                }
            } else {
                Files.createDirectories(packFilePath.getParent());
                Files.createFile(packFilePath);
            }

            if (!Files.exists(orderFilePath)) {
                Files.createDirectories(orderFilePath.getParent());
                Files.createFile(orderFilePath);
            }


            List<String> linesToAddPack = new ArrayList<>();
            List<String> linesToAddOrder = new ArrayList<>();
            boolean addedAny = false;

            for (InterfaceComponent component : componentsToRegister) {
                if (component == null || component.getName() == null || component.getName().trim().isEmpty()) {
                    continue;
                }

                String targetEntry = interfaceFileName + ":" + component.getName();

                if (!existingEntries.contains(targetEntry)) {
                    String newPackEntry = nextNumber + "=" + targetEntry;
                    linesToAddPack.add(newPackEntry);
                    linesToAddOrder.add(String.valueOf(nextNumber));

                    System.out.println("Adding to pack files: " + newPackEntry);
                    existingEntries.add(targetEntry);
                    nextNumber++;
                    addedAny = true;
                }
            }

            if (addedAny) {
                boolean packNeedsLeadingNewline = false;
                if (Files.exists(packFilePath) && Files.size(packFilePath) > 0) {
                    try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(packFilePath.toFile(), "r")) {
                        raf.seek(Files.size(packFilePath) - 1);
                        if (raf.readByte() != '\n') {
                            packNeedsLeadingNewline = true;
                        }
                    }
                } else if (!Files.exists(packFilePath) && !linesToAddPack.isEmpty()) {
                    packNeedsLeadingNewline = false;
                } else if (Files.exists(packFilePath) && Files.size(packFilePath) == 0 && !linesToAddPack.isEmpty()){
                    packNeedsLeadingNewline = false;
                }

                List<String> finalPackLines = new ArrayList<>();
                if (packNeedsLeadingNewline && !linesToAddPack.isEmpty()) {
                    finalPackLines.add("\n" + linesToAddPack.get(0));
                    finalPackLines.addAll(linesToAddPack.subList(1, linesToAddPack.size()));
                } else {
                    finalPackLines.addAll(linesToAddPack);
                }
                Files.write(packFilePath, finalPackLines, StandardOpenOption.APPEND, StandardOpenOption.CREATE);

                boolean orderNeedsLeadingNewline = false;
                if (Files.exists(orderFilePath) && Files.size(orderFilePath) > 0) {
                    try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(orderFilePath.toFile(), "r")) {
                        raf.seek(Files.size(orderFilePath) - 1);
                        if (raf.readByte() != '\n') {
                            orderNeedsLeadingNewline = true;
                        }
                    }
                } else if (!Files.exists(orderFilePath) && !linesToAddOrder.isEmpty()) {
                    orderNeedsLeadingNewline = false;
                } else if (Files.exists(orderFilePath) && Files.size(orderFilePath) == 0 && !linesToAddOrder.isEmpty()){
                    orderNeedsLeadingNewline = false;
                }

                List<String> finalOrderLines = new ArrayList<>();
                if (orderNeedsLeadingNewline && !linesToAddOrder.isEmpty()) {
                    finalOrderLines.add("\n" + linesToAddOrder.get(0));
                    finalOrderLines.addAll(linesToAddOrder.subList(1, linesToAddOrder.size()));
                } else {
                    finalOrderLines.addAll(linesToAddOrder);
                }

                Files.write(orderFilePath, finalOrderLines, StandardOpenOption.APPEND, StandardOpenOption.CREATE);


                System.out.println("Successfully updated interface pack files.");
            } else {
                System.out.println("No new component entries needed for pack files.");
            }
        } catch (IOException e) {
            System.err.println("Error updating interface pack files: " + e.getMessage());
            e.printStackTrace();
            Widgets.showAlert("Pack File Error", "Could not update interface.pack/interface.order: " + e.getMessage());
        }
    }
}
