package io.github.mateusz00.MandelbrotSet.Dialogs;

import io.github.mateusz00.MandelbrotSet.Utilities.Utility;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.ArrayList;

public class ExtensionFilter extends FileFilter
{
    private ArrayList<String> allowedExtensions;
    private String description;
    private String enforcedSaveExtension;

    public ExtensionFilter(String description, String... allowExtensions) {
        this.description = description;
        allowedExtensions = new ArrayList<>(allowExtensions.length);

        for(String extension : allowExtensions)
            allowedExtensions.add(extension);
    }

    public String getEnforcedSaveExtension() {
        return enforcedSaveExtension;
    }

    public void setEnforcedSaveExtension(String enforcedSaveExtension) {
        this.enforcedSaveExtension = enforcedSaveExtension;
    }

    public String enforceExtension(String fileName) {
        // Get rid of invalid extensions and/or add correct one
        fileName = Utility.removeExtension(fileName) + "." + enforcedSaveExtension;

        return fileName;
    }

    @Override
    public boolean accept(File f) {
        if(f.isDirectory())
            return true;

        // Get extension
        String fileName = f.getName(), extension;
        int extensionIndex = fileName.lastIndexOf('.');

        if(extensionIndex != -1) {
            extension = fileName.substring(extensionIndex + 1).toLowerCase();

            for(String allowedExtension : allowedExtensions) {
                if(extension.equals(allowedExtension))
                    return true;
            }
        }

        return false;
    }

    @Override
    public String getDescription() {
        return description;
    }
}