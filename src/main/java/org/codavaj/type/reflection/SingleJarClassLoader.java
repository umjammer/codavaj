package org.codavaj.type.reflection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


public class SingleJarClassLoader extends ClassLoader {
    JarFile jarFile = null;

    public SingleJarClassLoader(JarFile jarFile) {
        this.jarFile = jarFile;
    }

    public SingleJarClassLoader(ClassLoader arg0, JarFile jarFile) {
        super(arg0);
        this.jarFile = jarFile;
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        System.out.println("findclass: " + name);

        byte[] b = loadClassData(name);

        return defineClass(name, b, 0, b.length);
    }

    private byte[] loadClassData(String name) throws ClassNotFoundException {
        String filename = name.replace('.', '/') + ".class";
        ZipEntry entry = jarFile.getEntry(filename);

        if (entry == null) {
            throw new ClassNotFoundException(name);
        }

        try {
            InputStream is = jarFile.getInputStream(entry);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int i = -1;

            while ((i = is.read()) != -1) {
                baos.write(i);
            }

            return baos.toByteArray();
        } catch (IOException ioex) {
            throw new ClassNotFoundException("could not read " + name);
        }
    }
}
