package bruce.common.utils;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bruce.common.functional.EAction1;

/**
 * 文件工具类，操作文件的工具都写在这里
 * @author Bruce
 *
 */
public final class FileUtil {
	public static final String DEFAULT_CHARSET = "utf-8";
	private static Pattern pathPattern = Pattern.compile("(.+(?:\\/|\\\\))(.+)?$");
	
	public static String getExternalStorageDirPath(Object... dirNames) {
		return CommonUtils.buildString(File.separator, CommonUtils.displayArray(dirNames, File.separator), File.separator);
	}
	
	/**
	 * 使用默认的编码读取项目文本资源文件
	 * @param resPath	资源文件路径
	 * @return	文件内容
	 */
	public static String readTextFileForDefaultEncoding(final String resPath) {
		return readResourceTextFile(resPath, DEFAULT_CHARSET);
	}

	/**
	 * 读取项目文本资源文件
	 * @param resPath	资源文件路径
	 * @param encodingName	编码名称
	 * @return	文件内容
	 */
	public static String readResourceTextFile(final String resPath, final String encodingName) {
		InputStream xmlResourceInputStream = FileUtil.class.getClassLoader()
				.getResourceAsStream(resPath);
		BufferedReader xmlFileReader = new BufferedReader(
				new InputStreamReader(xmlResourceInputStream, Charset.forName(encodingName)));

		return readTextFromReader(xmlFileReader);
	}

	public static boolean writeTextFile(File filePath, String fileContent) {
		// TODO ..
		throw new UnsupportedOperationException();
	}
	
	public static boolean writeObj(Serializable src, String absPath) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(absPath)));
			oos.writeObject(src);
			oos.flush();
			return true;
		}
		catch (IOException e) { e.printStackTrace(); }
		finally {
			try {
				if (oos != null) oos.close();
			} catch (IOException e) { e.printStackTrace(); }
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T readObj(String absPath) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(absPath)));
			return (T) ois.readObject();
		}
		catch (IOException e) { e.printStackTrace(); }
		catch (ClassNotFoundException e) { e.printStackTrace(); }
		finally {
			try {
				if (ois != null) ois.close();
			} catch (IOException e) { e.printStackTrace(); }
		}
		return null;
	}
	
	/**
	 * 合并文件
	 * @param destFile
	 * @param files
	 * @throws IOException
	 */
	public static void catFile(final File destFile, final File...files) {
		if (files.length == 0) return;
		open(destFile.getAbsolutePath(), "rw", new EAction1<RandomAccessFile>() {
			@Override
			public void call(RandomAccessFile writing) throws Throwable {
				byte[] buf = new byte[8192];
				int nRead = 0;
				writing.seek(destFile.length());
				for (File file : files) {
					RandomAccessFile reading = new RandomAccessFile(file, "r");
					while ((nRead = reading.read(buf)) > 0) {
						writing.write(buf, 0, nRead);
					}
					reading.close();
				}
			}
		});
	}
	
	/**
	 * 读入文本文件
	 * @param textFile
	 * @return
	 */
	public static String readTextFile(File textFile) {
		return readTextFile(textFile, DEFAULT_CHARSET);
	}

	/**
	 * 读入文本文件
	 * @param file
	 * @param encoding		文件编码
	 * @return
	 */
	public static String readTextFile(final File file, String encoding) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
			return readTextFromReader(br);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
    }

	/**
	 * 根据路径取得文件名
	 * @param path	路径
	 * @return	文件名
	 */
	public static String getBaseNameByPath(final String path) {
		Matcher m = pathPattern.matcher(path);
		if (!m.matches()) return null;
		return m.group(2);
	}
	
	/**
	 * 根据路径取得文件夹
	 * @param path	路径
	 * @return	文件夹路径
	 */
	public static String getFileDirPath(final String path) {
		Matcher m = pathPattern.matcher(path);
		if (!m.matches()) return null;
		return m.group(1);
	}

	/**
	 * 从BufferedReader读取文本内容
	 * @param xmlFileReader bufferedReader
	 * @return	文本内容
	 */
	private static String readTextFromReader(final BufferedReader xmlFileReader) {
		StringBuffer sb = new StringBuffer();
		String temString = null;

		try {
			while (null != (temString = xmlFileReader.readLine())) {
				sb.append(temString);
				sb.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != xmlFileReader) {
				try {
					xmlFileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	public static void open(String filePath, String mode, EAction1<RandomAccessFile> block) {
		RandomAccessFile recordFile = null;
		try {
			recordFile = new RandomAccessFile(filePath, mode);
			block.call(recordFile);
		}
		catch (Throwable e) { throw new RuntimeException(e); }
		finally {
			try {
				if (recordFile != null)
					recordFile.close();
			} catch (IOException e) { e.printStackTrace(); }
		}
	}
}
