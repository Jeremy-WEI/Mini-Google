package cis555.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ZipUtils {
	
	/**
	 * Zips a file. The filename should end in gzip
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
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static byte[] unzip(File file) throws IOException {
		GZIPInputStream gs = new GZIPInputStream(new FileInputStream(file));
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int len;
		while ((len = gs.read(buffer)) > 0){
			out.write(buffer, 0, len);
		}
		gs.close();
		out.close();
		return out.toByteArray();
	}
	
	public static void main(String[] args) throws Exception {
		String s = "Hello";
		String filename = "9.txt.gzip";
		zip(s.getBytes(), filename);
		File file = new File(filename);
		byte[] unzipped = unzip(file);
		System.out.println(new String(unzipped));
	}	
}
