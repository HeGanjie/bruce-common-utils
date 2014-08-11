package bruce.common.utils;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import bruce.common.functional.Action1;
import bruce.common.functional.EAction1;
import bruce.common.functional.Func1;
import bruce.common.functional.LambdaUtils;
import bruce.common.functional.PersistentSet;

/**
 * 文件工具类，操作文件的工具都写在这里
 * @author Bruce
 *
 */
public final class FileUtil {
	public static final String DEFAULT_CHARSET = "utf-8";
	private static final int BUFFER_SIZE = 1024 * 8;
	private static Pattern pathPattern = Pattern.compile("(.+(?:\\/|\\\\))(.+)?$");
	private static final Set<String> imgSuffixSet = new PersistentSet<>("JPG", "GIF", "PNG", "JPEG", "BMP").getModifiableCollection();

	public static boolean isImageFileSuffix(String path) {
		String suffix = getSuffixByFileName(getBaseNameByPath(path));
		return imgSuffixSet.contains(CommonUtils.emptyIfNull(suffix).toUpperCase());
	}
	
	public static String getSuffixByFileName(String fileName) {
		if (CommonUtils.isStringNullOrWhiteSpace(fileName)) {
			return null;
		} else {
			int lastIndexOfPoint = fileName.lastIndexOf('.');
			return lastIndexOfPoint == -1 ? null : fileName.substring(lastIndexOfPoint + 1);
		}
	}
	
	public static void copy(File src, File dst) {
	    FileInputStream is = null;
		try {
			is = new FileInputStream(src);
			writeFile(dst, is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) { }
			}
		}
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
		return writeFile(filePath, new ByteArrayInputStream(fileContent.getBytes()));
	}
	
	public static boolean writeTextFile(File filePath, String fileContent, String enc) {
		try {
			return writeFile(filePath, new ByteArrayInputStream(fileContent.getBytes(enc)));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean writeFile(File filePath, InputStream is) {
		return writeFile(filePath, is, null);
	}
	
	public static boolean writeFile(File filePath, InputStream is, Action1<Long> progressCallback) {
		FileOutputStream os = null;
		File outFile = new File(filePath.getAbsolutePath() + ".tmp");
		if (!outFile.getParentFile().exists()) outFile.mkdirs();
		try {
			if (outFile.exists() && !outFile.delete()) {
				throw new IllegalStateException("File already writing");
			}
			os = new FileOutputStream(outFile);
			copy(is, os, progressCallback);
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					filePath.delete();
					if (!outFile.renameTo(filePath)) {
						throw new IllegalStateException("Writing to an opening file!");
					}
				}
			}
		}
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
	
	public static void catFile(final File destFile, final File...files) {
		catFile(destFile, Arrays.asList(files));
	}
	
	/**
	 * 合并文件
	 * @param destFile
	 * @param files
	 * @throws IOException
	 */
	public static void catFile(final File destFile, final List<File> files) {
		if (files == null || files.isEmpty()) return;
		withOpen(destFile.getAbsolutePath(), "rw", new EAction1<RandomAccessFile>() {
			@Override
			public void call(RandomAccessFile writing) throws Throwable {
				byte[] buf = new byte[1024 * 8];
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
	 * @param reader bufferedReader
	 * @return	文本内容
	 */
	private static String readTextFromReader(final BufferedReader reader) {
		StringBuffer sb = new StringBuffer();

		char[] buf = new char[1024 * 4];
		int readLen;
		try {
			while (-1 != (readLen = reader.read(buf))) {
				sb.append(buf, 0, readLen);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != reader) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	public static void withOpen(String filePath, String mode, EAction1<RandomAccessFile> block) {
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

	public static void writeObject(String parent, String fileName, Serializable obj) {
		File parentDir = new File(parent);
		parentDir.mkdirs();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(parentDir, fileName))));
			oos.writeObject(obj);
			oos.flush();
		} catch (IOException e) {
			CommonUtils.throwRuntimeExceptionAndPrint(e);
		} finally {
			if (oos != null)
				try {
					oos.close();
				} catch (IOException e) {
					CommonUtils.throwRuntimeExceptionAndPrint(e);
				}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T readObject(String path) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(path)));
			return (T) ois.readObject();
		} catch (Exception e) {
			CommonUtils.throwRuntimeExceptionAndPrint(e);
		} finally {
			try {
				if (ois != null)
					ois.close();
			} catch (Exception e) {
				CommonUtils.throwRuntimeExceptionAndPrint(e);
			}
		}
		return null;
	}

	public static List<File> recurListFiles(File root, final String ...suffixs) {
        File[] dirs = root.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) { return f.isDirectory(); }
		});
        
        List<File> selectMany = LambdaUtils.selectMany(Arrays.asList(dirs), new Func1<Collection<File>, File>() {
			@Override
			public Collection<File> call(File dir) { return recurListFiles(dir, suffixs); }
		});
        
        File[] files = root.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
            	for (String suffix : suffixs) {
            		if (name.endsWith(suffix)) return true;
				}
            	return false;
            }
        });
        selectMany.addAll(Arrays.asList(files));
        return selectMany;
	}
	
	public static long copy(InputStream input, OutputStream output) throws IOException {
	    return copy(input, output, null);
	}
	
	public static long copy(InputStream input, OutputStream output, Action1<Long> progressCallback) throws IOException {
	    byte[] buffer = new byte[BUFFER_SIZE];
	    long count = 0;
	    int n;
	    while ((n = input.read(buffer)) != -1) {
	        output.write(buffer, 0, n);
	        count += n;
	        if (progressCallback != null) progressCallback.call(count);
	    }
	    return count;
	}
	
	public static String zip(String[] files, String zipFile) throws IOException {
	    ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
	    try { 
	        byte data[] = new byte[BUFFER_SIZE];
	        BufferedInputStream origin = null;
	        for (String file : files) {
	            origin = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
	            try {
	                out.putNextEntry(new ZipEntry(FileUtil.getBaseNameByPath(file)));
	                int count;
	                while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
	                    out.write(data, 0, count);
	                }
	            } finally {
	                origin.close();
	            }
	        }
	    } finally {
	        out.close();
	    }
	    return zipFile;
	}

	public static void unzip(String zipFile, String location) throws IOException {
	    try {
	        File f = new File(location);
	        if (!f.isDirectory()) f.mkdirs();
	        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
	        try {
	            ZipEntry ze = null;
	            while ((ze = zin.getNextEntry()) != null) {
	                String path = location + ze.getName();

	                if (ze.isDirectory()) {
	                    File unzipFile = new File(path);
	                    if (!unzipFile.isDirectory()) unzipFile.mkdirs();
	                } else {
	                	BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(path, false));
	                    try {
	                        for (int c = zin.read(); c != -1; c = zin.read()) {
	                            fout.write(c);
	                        }
	                        zin.closeEntry();
	                    } finally {
	                        fout.close();
	                    }
	                }
	            }
	        } finally {
	            zin.close();
	        }
	    } catch (Exception e) {
	    	CommonUtils.throwRuntimeExceptionAndPrint(e);
	    }
	}
	
	public static int recurEncodingConvert(String path, String fileSuffix, String originalEnc, String finalEnc) {
		List<File> txtFiles = recurListFiles(new File(path), fileSuffix);
		for (File f : txtFiles) {
			String content = readTextFile(f, originalEnc);
			writeTextFile(f, content, finalEnc);
		}
		return txtFiles.size();
	}
	
	public static void deleteDir(File dir) {
		if (!dir.isDirectory()) return;
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory())
				deleteDir(files[i]);
			else
				files[i].delete();
		}
		dir.delete();
	}

	public static void main(String[] args) {
	}
}