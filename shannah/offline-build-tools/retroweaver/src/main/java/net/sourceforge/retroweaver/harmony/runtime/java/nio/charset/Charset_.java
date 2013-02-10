package net.sourceforge.retroweaver.harmony.runtime.java.nio.charset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.spi.CharsetProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import java.nio.charset.*;

public class Charset_ {

    public static Charset defaultCharset() {
        Charset defaultCharset = null;
        String encoding = AccessController
                .doPrivileged(new PrivilegedAction<String>() {
                    public String run() {
                        return System.getProperty("file.encoding"); //$NON-NLS-1$
                    }
                });
        try {
            defaultCharset = Charset.forName(encoding);
        } catch (UnsupportedCharsetException e) {
            defaultCharset = Charset.forName("UTF-8"); //$NON-NLS-1$
        }
        return defaultCharset;
    }

} 