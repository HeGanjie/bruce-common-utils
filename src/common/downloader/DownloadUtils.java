package common.downloader;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import common.functional.Action1;


public final class DownloadUtils {
	public static int CONNECT_TIMEOUT = 15000;
	public static int READ_TIMEOUT = 30000;
	public static int DEFAULT_TRY_TIMES = 3;
	public static final Map<String, String> ORIGIN_EXTRA_HEADER = new HashMap<String, String>(){
		private static final long serialVersionUID = -4912991629273948247L;
		{
			put(DownloadTask.EXTRA_HEADER_USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31");
		}
	};

	public static DownloadTask download(String url, String savePath, int threadCount,
			 Action1<Float> progressCallback, Action1<String> finishCallBack)
					throws IOException, CloneNotSupportedException {
		DownloaderAgent downloaderAgent = newDownloaderAgent(threadCount, finishCallBack);
		DownloadTask task = downloaderAgent.createDownloadTask(url, savePath, progressCallback);
		task.start();
		return task;
	}
	
	public static DownloaderAgent newDownloaderAgent(int threadCount, Integer tryTimes, Action1<String> finishCallBack) {
		if (tryTimes == null)
			tryTimes = DEFAULT_TRY_TIMES;
		return new DownloaderAgent(threadCount, tryTimes, finishCallBack);
	}
	
	public static DownloaderAgent newDownloaderAgent(int threadCount, Action1<String> finishCallBack) {
		return newDownloaderAgent(threadCount, DEFAULT_TRY_TIMES, finishCallBack);
	}

	public static boolean hasUnfinishedDownloadTask(String preDownloadFilePath) {
		return new File(DownloadTask.getRecordFilePath(preDownloadFilePath)).exists();
	}
}
