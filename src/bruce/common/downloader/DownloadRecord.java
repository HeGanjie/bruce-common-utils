package bruce.common.downloader;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Serializable;

import bruce.common.functional.EAction1;
import bruce.common.functional.Pointer;
import bruce.common.utils.FileUtil;

public final class DownloadRecord implements Serializable {
	private static final long serialVersionUID = -5033815744564217186L;
	String url;
	int threadCount;
	long fileLength;

	protected DownloadRecord(String url, int threadCount, long fileLength) {
		this.url = url;
		this.threadCount = threadCount;
		this.fileLength = fileLength;
	}

	protected void write(String savePath) {
		FileUtil.withOpen(savePath, "rw", new EAction1<RandomAccessFile>() {
			@Override
			public void call(RandomAccessFile recordFile) throws Throwable {
				String lineBreakStr = System.getProperty("line.separator");
				recordFile.writeBytes(url);		recordFile.writeChars(lineBreakStr);
				recordFile.writeBytes(String.valueOf(threadCount));		recordFile.writeChars(lineBreakStr);
				recordFile.writeBytes(String.valueOf(fileLength));		recordFile.writeChars(lineBreakStr);
			}
		});
	}

	protected static DownloadRecord load(File tmpFile) {
		final Pointer<DownloadRecord> ptr = new Pointer<DownloadRecord>();
		FileUtil.withOpen(tmpFile.getAbsolutePath(), "r", new EAction1<RandomAccessFile>() {
			@Override
			public void call(RandomAccessFile readingTmpFile) throws Throwable {
				String urlInFile = readingTmpFile.readLine().trim();
				int threadCountInFile = Integer.parseInt(readingTmpFile.readLine().trim());
				int downLenInFile = Integer.parseInt(readingTmpFile.readLine().trim());
				ptr.value = new DownloadRecord(urlInFile, threadCountInFile, downLenInFile);
			}
		});
		return ptr.value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (fileLength ^ (fileLength >>> 32));
		result = prime * result + threadCount;
		return prime * result + ((url == null) ? 0 : url.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DownloadRecord other = (DownloadRecord) obj;
		if (fileLength != other.fileLength) return false;
		if (threadCount != other.threadCount) return false;
		if (url == null) {
			if (other.url != null) return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
}
