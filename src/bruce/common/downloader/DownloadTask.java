package bruce.common.downloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import bruce.common.functional.Action1;
import bruce.common.functional.EAction1;
import bruce.common.functional.Func2;
import bruce.common.functional.LambdaUtils;
import bruce.common.functional.Pointer;
import bruce.common.productLine.Task;
import bruce.common.utils.CommonUtils;
import bruce.common.utils.FileUtil;

public final class DownloadTask implements Task, Cloneable, Serializable {
	private Map<String, String> EXTRA_HEADER;
	
	private static final String HTTP_HEAD_RANGE = "RANGE";
	private static final long serialVersionUID = 783497780509917101L;
	public static final String DOWNLOAD_RECORD_FILE_EXT = ".download",
			INFO_KEY_ID = "Id",
			INFO_KEY_PROGRESS = "Progress",
			INFO_KEY_DOWNLOAD_SPEED = "DownloadSpeed",
			INFO_KEY_SECONDS_REMAINING = "SecondsRemaining",
			INFO_KEY_STATE = "State",
			INFO_KEY_FILE_LENGTH = "FileLength",
			INFO_KEY_SECTION_COUNT = "SectionCount",
			INFO_KEY_URL = "Url",
			EXTRA_HEADER_USER_AGENT = "User-Agent",
			EXTRA_HEADER_COOKIE = "Cookie",
			EXTRA_PARAM_MAX_TRY_COUNT = "MaxTryCount";
						
	String id, url, saveFileAt;
	long offset = 0, downLen = 0, finishedLen = 0, lastReportTime = 0;
	int downloadererCount, byteSpeed = 0;
	private DownloadTaskState state = DownloadTaskState.stop;
	
	transient List<DownloadTask> tasks;
	transient private Action1<Float> _progressCallback;
	transient private DownloaderAgent _downloaderAgent;

	protected DownloadTask(String url, String saveFileName, int workerCount) throws IOException, CloneNotSupportedException {
		this(url, saveFileName);
		downloadererCount = workerCount;
		splitTask();
		id = CommonUtils.dateFormat(new Date(), "_", "yyyyMMdd", "HHmmss");

		if (!hasSameDownloadRecord())
			createDownloadRecord().write(getRecordFilePath()); // 创建下载记录文件
	}

	protected DownloadTask(String urlStr, String saveFileName) {
		url = urlStr;
		saveFileAt = saveFileName;
	}

	public void setProgressCallback(Action1<Float> progressCallback) {
		_progressCallback = progressCallback;
	}

	public void stop() {
		if (state != DownloadTaskState.downloading)
			throw new UnsupportedOperationException();
		
		state = DownloadTaskState.stop;
		CommonUtils.trace(getClass(), "下载被终止!");
	}
	
	public void start() {
		if (state == DownloadTaskState.downloading || state == DownloadTaskState.finished)
			throw new UnsupportedOperationException();
		
		state = DownloadTaskState.downloading;
		_downloaderAgent.doDownload(this);
		
		lastReportTime = System.currentTimeMillis();
	}

	protected void splitTask() throws IOException, CloneNotSupportedException {
		if (downLen == 0) { // Was not load from file
			offset = 0;
			downLen = getContentLen();
			if (downLen == 0) throw new FileNotFoundException();
		}

		tasks = new ArrayList<DownloadTask>();

		int sectionLen = (int) Math.ceil((float)downLen / downloadererCount), allottedLen = 0;
		for (int i = 0; i < downloadererCount; i++) {
			DownloadTask task = clone();
			task.offset = allottedLen;
			task.downLen = Math.min(downLen - allottedLen, sectionLen);
			allottedLen += task.downLen;
			task.saveFileAt = CommonUtils.buildString(task.saveFileAt, ".", i);
			tasks.add(task);
		}
	}

	protected boolean hasSameDownloadRecord() {
		// 断点续传，假使有一文件a.doc，检查临时文件a.doc.download：若存在并且当中记录的 URL/分段数/总长度 不一致，则删除所有的临时文件
		final File tmpFile = new File(getRecordFilePath());
		if (tmpFile.exists()) {
			if (!DownloadRecord.load(tmpFile).equals(createDownloadRecord())) deleteTempFile();
		}
		return tmpFile.exists();
	}

	protected DownloadRecord createDownloadRecord() {
		return new DownloadRecord(url, downloadererCount, downLen);
	}

	protected String getRecordFilePath() {
		return getRecordFilePath(saveFileAt);
	}

	protected static String getRecordFilePath(String saveFileAt) {
		return saveFileAt + DOWNLOAD_RECORD_FILE_EXT;
	}

	/**
	 * 下载前检查是否能断点续传，是则从上次断开的地方开始下载
	 * @return
	 */
	protected long checkResumable() {
		File writefileAt = new File(saveFileAt);
		return writefileAt.exists() ? writefileAt.length() : 0; // 断点续传
	}

	protected int getContentLen() throws IOException, MalformedURLException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		configConnection(conn);
		conn.setRequestMethod("HEAD");
		conn.connect();
		int taskLen = conn.getContentLength();
		conn.disconnect();
		return taskLen;
	}
	
	public String getId() { return id; }

	@Override
	protected DownloadTask clone() throws CloneNotSupportedException {
		return new DownloadTask(url, saveFileAt);
	}

	@Override
	public String toString() {
		return "DownloadTask [url=" + url + ", saveFileAt=" + saveFileAt
				+ ", offset=" + offset
				+ ", downLen=" + downLen + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((saveFileAt == null) ? 0 : saveFileAt.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DownloadTask other = (DownloadTask) obj;
		if (saveFileAt == null) {
			if (other.saveFileAt != null) return false;
		} else if (!saveFileAt.equals(other.saveFileAt))
			return false;
		return true;
	}

	protected void download(final DownloadTask mainTask, final long triggerReportSpan) {
		HttpURLConnection conn = null;
		final Pointer<InputStream> inputStreamPtr = new Pointer<InputStream>();
		try {
			finishedLen = checkResumable();
			long end = offset + downLen - 1;
			if (end < offset + finishedLen) {
				state = DownloadTaskState.finished;
				mainTask.reportProgress();
				return; //此部分已下载完成
			}
			state = DownloadTaskState.downloading;

			conn = (HttpURLConnection) new URL(url).openConnection();
			configConnection(conn);
			conn.setRequestProperty(HTTP_HEAD_RANGE, CommonUtils.buildString("bytes=", offset + finishedLen, "-", end)); //包含 例如0-500个字节，应该是0-499 
			conn.connect();
			inputStreamPtr.value = conn.getInputStream();

			FileUtil.open(saveFileAt, "rw", new EAction1<RandomAccessFile>() {
				@Override
				public void call(RandomAccessFile writingFile) throws Throwable {
					if (finishedLen != 0) writingFile.seek(finishedLen);

					byte[] tmpBuf = new byte[1024];
					int nRead = 0; long lastReportDownLen = finishedLen;
					InputStream inputStream = inputStreamPtr.value;
					while (mainTask.getState() == DownloadTaskState.downloading &&
							(nRead = inputStream.read(tmpBuf)) > 0) {
						writingFile.write(tmpBuf, 0, nRead);
						finishedLen += nRead;
						if (triggerReportSpan < finishedLen - lastReportDownLen) {
							mainTask.reportProgress();
							lastReportDownLen = finishedLen;
						}
					}
				}
			});
			state = finishedLen == downLen ? DownloadTaskState.finished : DownloadTaskState.stop;
			mainTask.reportProgress();
		} catch (IOException e) {
			state = DownloadTaskState.failure;
			throw new RuntimeException(e);
		} finally {
			if (conn != null) conn.disconnect();
			try {
				InputStream inputStream = inputStreamPtr.value;
				if (inputStream != null) inputStream.close();
			} catch (IOException e) { throw new RuntimeException(e); }
		}
	}

	protected void reportProgress() {
		if (_progressCallback != null)
			_progressCallback.call(getProgress());
		
		long originFinishedLen = finishedLen;
		finishedLen = statisticsFinishedLen();
		
		long current = System.currentTimeMillis();
		// elapse / 1000 == downLen / byteSpeed
		byteSpeed = Math.round((float)(finishedLen - originFinishedLen) * 1000 / (current - lastReportTime));
		lastReportTime = current;
	}

	protected float getProgress() { return 100 * (float)statisticsFinishedLen() / downLen; }

	static final Func2<Long, Long, DownloadTask> funcInStatistics = new Func2<Long, Long, DownloadTask>() {
		@Override
		public Long call(Long t, DownloadTask subTask) { return t + subTask.finishedLen; }
	};
		
	protected long statisticsFinishedLen() { return LambdaUtils.reduce(tasks, 0l, funcInStatistics); }

	/**
	 * 删除.download以及其他临时文件
	 */
	public void deleteTempFile() {
		final File tmpFile = new File(getRecordFilePath());
		tmpFile.delete();
		for (DownloadTask subTask : tasks)
			new File(subTask.saveFileAt).delete();
	}

	protected void configConnection(HttpURLConnection conn) {
		conn.setReadTimeout(DownloadUtils.READ_TIMEOUT);
		conn.setConnectTimeout(DownloadUtils.CONNECT_TIMEOUT);
		Set<Entry<String, String>> entrySet = DownloadUtils.ORIGIN_EXTRA_HEADER.entrySet();
		for (Entry<String, String> entry : entrySet) {
			conn.setRequestProperty(entry.getKey(), entry.getValue());
		}
		
		if (EXTRA_HEADER != null) {
			entrySet = EXTRA_HEADER.entrySet();
			for (Entry<String, String> entry : entrySet) {
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public boolean writeDownloadTaskToPath(String absDirPath) {
		return FileUtil.writeObj(this, absDirPath + id + ".task");
	}
	
	public static DownloadTask loadDownloadTaskFormFile(String absPath) {
		DownloadTask readObj = FileUtil.readObj(absPath);
		try {
			readObj.splitTask();
			return readObj;
		}
		catch (IOException e) { e.printStackTrace(); }
		catch (CloneNotSupportedException e) { e.printStackTrace(); }
		return null;
	}
	
	public DownloadTaskState getState() { return state; }

	private int getDownloadSpeed() { return byteSpeed; }

	private int getSecondsRemaining() {
		return Math.round(((float)downLen / byteSpeed));
	}

	public Map<String, Object> getSimpleInfos() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(INFO_KEY_FILE_LENGTH, downLen);
		map.put(INFO_KEY_PROGRESS, getProgress());
		map.put(INFO_KEY_DOWNLOAD_SPEED, getDownloadSpeed());
		map.put(INFO_KEY_SECONDS_REMAINING, getSecondsRemaining());
		map.put(INFO_KEY_STATE, getState().name());
		return map;
	}

	public Map<String, Object> getInfos() {
		Map<String, Object> infos = getSimpleInfos();
		infos.put(INFO_KEY_SECTION_COUNT, downloadererCount);
		infos.put(INFO_KEY_URL, url);
		if (EXTRA_HEADER != null) {
			infos.put(EXTRA_HEADER_USER_AGENT, EXTRA_HEADER.get(EXTRA_HEADER_USER_AGENT));
			infos.put(EXTRA_HEADER_COOKIE, EXTRA_HEADER.get(EXTRA_HEADER_COOKIE));
			infos.put(EXTRA_PARAM_MAX_TRY_COUNT, EXTRA_HEADER.get(EXTRA_PARAM_MAX_TRY_COUNT));
		}
		return infos;
	}

	public String getSaveFileAt() {
		return saveFileAt;
	}
	
	public void setAgent(DownloaderAgent downloaderAgent) {
		if (downloaderAgent.downloaderCount != downloadererCount)
			throw new UnsupportedOperationException();
		_downloaderAgent = downloaderAgent;
	}

	public void allSubTaskIsFailure() { state = DownloadTaskState.failure; }

	public void markFinished() { state = DownloadTaskState.finished; }

	public void setExtraHeader(String userAgent, String cookie) {
		if (EXTRA_HEADER == null && (!CommonUtils.isStringNullOrWriteSpace(userAgent) || !CommonUtils.isStringNullOrWriteSpace(cookie))) {
			EXTRA_HEADER = new HashMap<String, String>();
			EXTRA_HEADER.put(EXTRA_HEADER_USER_AGENT, userAgent);
			EXTRA_HEADER.put(EXTRA_HEADER_COOKIE, cookie);
		} else
			EXTRA_HEADER = null;
	}

	public static DownloadTask create(String url, String savePath, int downloaderCount) throws IOException, CloneNotSupportedException {
		//从记录文件中读取url，如果url为null的话
		if (CommonUtils.isStringNullOrWriteSpace(url) &&
				!CommonUtils.isStringNullOrWriteSpace(FileUtil.getBaseNameByPath(savePath))) {
			url = DownloadRecord.load(new File(DownloadTask.getRecordFilePath(savePath))).url;
		}
		if (CommonUtils.isStringNullOrWriteSpace(url)) throw new MalformedURLException();

		String downloadAt = getDownloadAt(url, savePath);
		if (new File(downloadAt).exists())
			throw new FileAlreadyExistsException(downloadAt);

		return new DownloadTask(url, downloadAt, downloaderCount);
	}
	/**
	 * 取得保存路径，并创建文件夹<br/>
	 * 如果savePath只提供了保存至哪个文件夹，则以url中的文件名命名，否则保存至savePath
	 * @param url
	 * @param savePath
	 * @return
	 * @throws MalformedURLException 
	 */
	public static String getDownloadAt(String url, String savePath) throws MalformedURLException {
		String dirPath = FileUtil.getFileDirPath(savePath);
		File atDir = new File(dirPath);
		if (!atDir.exists()) atDir.mkdirs();

		if (CommonUtils.isStringNullOrWriteSpace(FileUtil.getBaseNameByPath(savePath)))
			return new File(dirPath, new URL(url).getPath()).getAbsolutePath();
		else
			return savePath;
	}
}

