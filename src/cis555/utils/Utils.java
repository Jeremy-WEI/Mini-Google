package cis555.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

public class Utils {

    private static final Logger logger = Logger.getLogger(Utils.class);
    private static final String CLASSNAME = Utils.class.getName();

    private static MessageDigest digest;

    /**
     * Hashes a url using MD5, and returns a Hex-string representation of the
     * hash
     * 
     * @param url
     * @return
     */
    public static String hashUrlToHexStringArray(String urlString) {
        byte[] hash = hashUrlToByteArray(urlString);
        return DatatypeConverter.printHexBinary(hash);
    }

    /**
     * Hashes a url using MD5, returns a byte array
     * 
     * @param url
     * @return
     */
    public static byte[] hashUrlToByteArray(String urlString) {
        if (null == digest) {
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        digest.reset();
        digest.update(urlString.getBytes());
        return digest.digest();
    }

    /**
     * Append an array of bytes representing a byte array to the beginning of a
     * file, of length set by CrawlerConstants.MAX_URL_LENGTH * 2 (since each
     * character could be up to 2 bytes) Null characters will be appended to
     * fill up the byte array
     * 
     * @param url
     * @param contents
     * @return
     */
    public static byte[] appendURL(URL url, byte[] contents) {
        try {
            byte[] chars = url.toString().getBytes(CrawlerConstants.CHARSET);
            byte[] returnByteArray = new byte[CrawlerConstants.MAX_URL_LENGTH * 2];

            int j = 0;
            for (int i = 0; i < returnByteArray.length; i++) {
                if (j < chars.length) {
                    returnByteArray[i] = chars[j];
                    j++;
                } else {
                    returnByteArray[i] = 0;
                }
            }

            return concatenate(returnByteArray, contents);

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Concatentate two byte ararys
     * 
     * @param a
     * @param b
     * @return
     */
    private static byte[] concatenate(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    /**
     * Zips a file. The filename should end in gzip
     * 
     * @param contents
     * @param fileName
     * @throws IOException
     */
    public static void zip(byte[] contents, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        GZIPOutputStream gz = new GZIPOutputStream(fos);
        gz.write(contents);
        gz.close();
    }

    /**
     * Unzips a file. Assumes file is a valid gzip file
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] unzip(File file) throws IOException {
        GZIPInputStream gs = new GZIPInputStream(new FileInputStream(file));
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        while ((len = gs.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        gs.close();
        out.close();
        return out.toByteArray();
    }

    /**
     * Unzips a byte array
     * 
     * @param bytes
     * @return
     * @throws IOException
     */
    public static byte[] unzip(byte[] bytes) throws IOException {
        GZIPInputStream gs = new GZIPInputStream(
                new ByteArrayInputStream(bytes));
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        while ((len = gs.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        gs.close();
        out.close();
        return out.toByteArray();
    }

    /**
     * Get URL from a byte array
     * 
     * @param bytes
     * @return URL
     * @throws IOException
     */
    public static String getURL(byte[] bytes) throws IOException {
        int i = 0;
        while (i <= CrawlerConstants.MAX_URL_LENGTH * 2) {
            if (bytes[i] == 0)
                break;
            i++;
        }
        return new String(bytes, 0, i);
    }

    /**
     * Log the entire stack trace to the logger
     * 
     * @param e
     */
    public static void logStackTrace(Exception e) {
        StackTraceElement[] traces = e.getStackTrace();
        if (null != traces && traces.length > 0) {
            logger.error(CLASSNAME);
            for (int i = 0; i < traces.length; i++) {
                logger.error(traces[i]);
            }
        }
    }

    /****
     * Other helper methods
     */

    /**
     * Creates a directory to store the database and environment settings
     */
    public static void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            try {
                directory.mkdirs();
                logger.info(CLASSNAME + ": New directory created " + path);
            } catch (SecurityException e) {
                throw new RuntimeException("Unable to create directory");
            }
        }

    }
}
