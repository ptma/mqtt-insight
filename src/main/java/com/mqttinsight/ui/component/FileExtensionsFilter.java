package com.mqttinsight.ui.component;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * Copy from FileNameExtensionFilter
 *
 * @author ptma
 * @see FileNameExtensionFilter
 * @see javax.swing.JFileChooser#setFileFilter
 * @see javax.swing.JFileChooser#addChoosableFileFilter
 * @see javax.swing.JFileChooser#getFileFilter
 */
public final class FileExtensionsFilter extends FileFilter {

    private final String description;

    private final String[] extensions;

    public FileExtensionsFilter(String description, String... extensions) {
        if (extensions == null || extensions.length == 0) {
            throw new IllegalArgumentException("Extensions must be non-null and not empty");
        }
        this.description = description;
        this.extensions = extensions;
    }

    @Override
    public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }
            String fileName = f.getName();
            int i = fileName.lastIndexOf('.');
            if (i > 0 && i < fileName.length() - 1) {
                String fileExtension = fileName.substring(i + 1);
                for (String extension : extensions) {
                    if ("*".equals(extension) || fileExtension.equalsIgnoreCase(extension)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
