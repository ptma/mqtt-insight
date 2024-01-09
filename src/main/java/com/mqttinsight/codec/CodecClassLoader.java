package com.mqttinsight.codec;

import lombok.Getter;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author ptma
 */
public class CodecClassLoader extends URLClassLoader {

    @Getter
    private final URL[] urls;

    public CodecClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.urls = urls;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
