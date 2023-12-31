/*
 * Ut.java
 *
 * Copyright (c) 2010 Flaviano Petrocchi <flavianopetrocchi at gmail.com>.
 * All rights reserved.
 *
 * This file is part of JPdfBookmarks.
 *
 * JPdfBookmarks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPdfBookmarks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPdfBookmarks.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.flavianopetrocchi.utilities;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
//import sun.misc.CharacterEncoder;

/**
 * Class for utlity static methods usefull in different situations.
 */
public final class Ut {

    private Ut() {
    }

    /**
     *  The only value of this could be not save the string in memory with possibly a 
     *  password inside using simple String.valueOf(array).getBytes(). The internal buffer
     *  used for transformation is filled with 0 here, thr parameter charsArray and the
     *  returned byte array must be cleaned by the user.
     * 
     * @param charsArray The character array to transform
     * @return An array of bytes that represent the character array within the default
     *         Charset.
     */
    public static byte[] arrayOfCharsToArrayOfBytes(char[] charsArray) {
        Charset defaultCharset = Charset.defaultCharset();
        CharsetEncoder encoder = defaultCharset.newEncoder();
        
        //calculate maximum lenght of the array with this charset and allocate it
        int maxArrayLen = (int)(charsArray.length * encoder.maxBytesPerChar());
        byte[] bytesArray = new byte[maxArrayLen];

        //perform encoding
        encoder.reset();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytesArray);
        CharBuffer charBuffer = CharBuffer.wrap(charsArray);
        try {
            encoder.encode(charBuffer, byteBuffer, true);
            encoder.flush(byteBuffer);
        } catch (Exception x) {
            //I can do little to handle an exception here
        }

        //truncate not used bytes
        byte[] truncated;
        if (byteBuffer.position() == bytesArray.length) {
            truncated = bytesArray;
        } else {
            truncated = Arrays.copyOf(bytesArray, byteBuffer.position());
            Arrays.fill(bytesArray, (byte)0);
        }
        return truncated;

    }

    private static byte[] encode(CharsetEncoder ce, char[] charsArray) {
        int maxArrayLen = (int)(charsArray.length * ce.maxBytesPerChar());
        byte[] bytesArray = new byte[maxArrayLen];

        ce.reset();
        ByteBuffer bb = ByteBuffer.wrap(bytesArray);
        CharBuffer cb = CharBuffer.wrap(charsArray);
        try {
            CoderResult cr = ce.encode(cb, bb, true);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
            cr = ce.flush(bb);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
        } catch (CharacterCodingException x) {
            throw new Error(x);
        }
        return truncateArrayToTheLastUsedByte(bytesArray, bb.position());
    }

    private static byte[] truncateArrayToTheLastUsedByte(byte[] transcodedArray, int usedBytes) {
        byte[] truncated;
        if (usedBytes == transcodedArray.length) {
            truncated = transcodedArray;
        } else {
            truncated = Arrays.copyOf(transcodedArray, usedBytes);
            Arrays.fill(transcodedArray, (byte)0);
        }
        return truncated;
    }

    /**
     * Get the class name for the current look and feel. The returned string can
     * be used by UIManager.setLookAndFeel(className)
     * @return The class name for the current Look & Fell
     */
    public static String getClassNameForCurrentLAF() {
        String className = null;
        String currentLAF = UIManager.getLookAndFeel().getName();
        LookAndFeelInfo[] infoArray = UIManager.getInstalledLookAndFeels();
        for (LookAndFeelInfo info : infoArray) {
            if (currentLAF.equals(info.getName())) {
                className = info.getClassName();
            }
        }
        return className;
    }

    public static void copyFile(String srcPath, String dstPath)
            throws FileNotFoundException, IOException {
        File f1 = new File(srcPath);
        File f2 = new File(dstPath);
        InputStream in = new FileInputStream(f1);
        OutputStream out = new FileOutputStream(f2);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String rtrim(String s) {

        StringBuilder sb = new StringBuilder(s);

        for (int i = sb.length() - 1; i >= 0; i--) {
            char c = sb.charAt(i);
            if (Character.isWhitespace(c)) {
                sb.deleteCharAt(i);
            } else {
                break;
            }
        }

        return sb.toString();
    }

    /**
     * Try to change the Look & Feel of a user interface.
     * @param laf The class name of the Look & Feel to apply.
     * @param c The Component at the root of the interface, generally the main
     * window.
     * @return True for sucess false for fail.
     */
    public static boolean changeLAF(String laf, Component c) {
        boolean success = true;
        try {
            UIManager.setLookAndFeel(laf);
            SwingUtilities.updateComponentTreeUI(c);
        } catch (Exception ex) {
            success = false;
        }
        return success;
    }

    /**
     * Change the enabled state of the actions passed as parameters.
     * @param enabled The state to change to.
     * @param actions The actions to change.
     */
    public static void enableActions(boolean enabled, Action... actions) {
        for (Action action : actions) {
            action.setEnabled(enabled);
        }
    }

    /**
     * Change the enabled state of the components passed as parameters.
     * @param enabled The state to change to.
     * @param components The components to change.
     */
    public static void enableComponents(boolean enabled, JComponent... components) {
        for (JComponent component : components) {
            component.setEnabled(enabled);
        }
    }

    public static void setSelectedButtons(boolean selected, AbstractButton... buttons) {
        for (AbstractButton button : buttons) {
            button.setSelected(selected);
        }

    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }

    /**
     * Create an absolute path file from a target path relative to a base path
     *
     * @param base The path relative to which target is indicated
     * @param target The path relative to base
     * @return The absolute path to target
     */
    public static File createAbsolutePath(File base, File target) {
        File absoluteTarget = target;
        if (!target.isAbsolute()) {
            String containingFolder = base.getParent();
            String remotePath = containingFolder + File.separator + target.getPath();
            absoluteTarget = new File(remotePath);
        }
        return absoluteTarget.getAbsoluteFile();
    }

    public static String onWindowsReplaceBackslashWithSlash(String path) {
        //most readers use a Unix like separator "/" instead of a "\" even on windows
        StringBuilder s = new StringBuilder(path);
        if (isWindowsSystem()) {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '\\') {
                    s.replace(i, i + 1, "/");
                }
            }
        }
        return s.toString();
    }

    public static boolean isWindowsSystem() {
        String sys = System.getProperty("os.name");
        return sys.startsWith("Windows");
    }

    public static ArrayList<File> getParentDirectories(File f) {
        ArrayList<File> directories = new ArrayList<File>();
        File canonical = f.getAbsoluteFile();
        try {
            canonical = canonical.getCanonicalFile();
        } catch (IOException ex) {
        }
        File parent = canonical.getParentFile();
        while (parent != null) {
            directories.add(parent);
            //On Windows stop adding when you arrive at something like C:\ or D:\
            if (isWindowsSystem() && parent.toString().endsWith(":\\")) {
                break;
            }
            parent = parent.getParentFile();
        }
        return directories;
    }

    public static File createRelativePath(File base, File target) {
        File relativeFile = target;
        ArrayList<File> baseDirectories = getParentDirectories(base);
        ArrayList<File> targetDirectories = getParentDirectories(target);

        File commonBaseDir = null;

        int baseIndex = baseDirectories.size() - 1;
        int targetIndex = targetDirectories.size() - 1;
        for (; baseIndex >= 0 && targetIndex >= 0; baseIndex--, targetIndex--) {
            if (baseDirectories.get(baseIndex).equals(targetDirectories.get(targetIndex))) {
                commonBaseDir = baseDirectories.get(baseIndex);
            } else {
                break;
            }
        }

        if (commonBaseDir != null) {
            int upDirectories = baseIndex + 1;

            StringBuilder path = new StringBuilder();
            for (int j = 0; j < upDirectories; j++) {
                path.append("..");
                path.append(File.separator);
            }
            for (; targetIndex >= 0; targetIndex--) {
                path.append(targetDirectories.get(targetIndex).getName());
                path.append(File.separator);
            }
            path.append(target.getName());
            relativeFile = new File(path.toString());
        }

        return relativeFile;
    }
}
