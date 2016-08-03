/*
 * Copyright (c) 2009 Hyperic, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bven.jni;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;

/**
 * A utility for loading distributed platform native libraries
 */
public class ArchLoader implements AutoCloseable {

    private final String name;
    private final String libLocation;
    private final ClassLoader classLoader;
    private Path tmpFile;

     /**
     * Extracts the appropriate library file from the classpath, and returns it
     * to be loaded, e.g: <code>
     * static {
     *   ArchLoader.load(Native.class);
     * }
     * </code>
     * 
     * @param clazz
     *            the library name and location will be derived from this
     *            class's name and package.
     */
    public static void load(Class<?> clazz) {
        try (ArchLoader loader = new ArchLoader(clazz)) {
            System.load(loader.getLibFile());
        }
    }

    /**
     * Extracts the appropriate library file from the classpath, and returns it
     * to be loaded. <code>
     * static {
     *   ArchLoader.load("MyLibrary", "libs", MyLibrary.class.getClassLoader());
     * }
     * </code>
     * 
     * @param name
     *            the base name of the library.
     * @param libLocation
     *            the root location within the classpath to search for libraries
     * @param classLoader
     *            the classloader to load the library with
     */
    public static void load(String name, String libLocation, ClassLoader classLoader) {
        try (ArchLoader loader = new ArchLoader(name, libLocation, classLoader)) {
            System.load(loader.getLibFile());
        }
    }

    /**
     * Extracts the appropriate library file from the classpath, and returns it
     * to be loaded, e.g: <code>
     * static {
     *   try (ArchLoader loader = new ArchLoader(Native.class)) {
     *     System.load(loader.getLibFile());
     *   }
     * }
     * </code>
     * 
     * @param clazz
     *            the library name and location will be derived from this
     *            class.
     */
    public ArchLoader(Class<?> clazz) {
        this(clazz.getSimpleName(), "lib/" + clazz.getPackage().getName(), clazz.getClassLoader());
    }

    /**
     * Extracts the appropriate library file from the classpath, and returns it
     * to be loaded. <code>
     * static {
     *   try (ArchLoader loader = new ArchLoader("MyLibrary", "libs", MyLibrary.class.getClassLoader())) {
     *     System.load(loader.getLibFile());
     *   }
     * }
     * </code>
     * 
     * @param name
     *            the base name of the library.
     * @param libLocation
     *            the root location within the classpath to search for libraries
     * @param classLoader
     *            the classloader to load the library with
     */
    public ArchLoader(String name, String libLocation, ClassLoader classLoader) {
        this.name = System.mapLibraryName(name);
        this.libLocation = libLocation;
        this.classLoader = classLoader;
    }

    private String getLibPath() {
        String libLoc = trimSeparators(libLocation);
        String libName = trimSeparators(name);
        return libLoc + "/" + ArchName.getName() + "/" + libName;
    }

    private static String trimSeparators(String path) {
        while (path.startsWith(File.separator) || path.startsWith("/")) {
            path = path.substring(1);
        }
        while (path.endsWith(File.separator) || path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Finds the file at {@link #getLibPath()} from the classpath, extracts to a
     * temporary file if necessary, and returns the absolute location as a
     * string. Note that any temporary file created will be cleaned up by
     * {@link #close()}.
     * 
     * @return the absolute path name of the loadable library file.
     * @throws ArchNotSupportedException
     *             if the library was not found
     * @throws ArchLoaderException
     *             if an error occurs extracting the file.
     */
    public synchronized String getLibFile() throws ArchLoaderException {
        if (tmpFile != null) {
            return tmpFile.toAbsolutePath().toString();
        }
        final String libPath = getLibPath();
        try {
            URI uri = classLoader.getResource(libPath).toURI();
            if ("file".equals(uri.getScheme())) {
                return new File(uri).getAbsolutePath();
            }
        } catch (NullPointerException | URISyntaxException e) {
            /* Continue on with the next option */ }

        try (InputStream resourceAsStream = classLoader.getResourceAsStream(libPath)) {
            if (resourceAsStream == null) {
                throw new ArchNotSupportedException(libPath + " not found in classpath.");
            }
            this.tmpFile = Files.createTempFile("", new File(libPath).getName());
            Files.copy(resourceAsStream, tmpFile, StandardCopyOption.REPLACE_EXISTING);
            return tmpFile.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new ArchLoaderException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void close() {
        if (this.tmpFile != null) {
            try {
                Files.deleteIfExists(tmpFile);
            } catch (IOException e) {
                tmpFile.toFile().deleteOnExit();
            } finally {
                tmpFile = null;
            }
        }
    }
}
