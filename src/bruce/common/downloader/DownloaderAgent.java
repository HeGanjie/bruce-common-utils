package bruce.common.downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bruce.common.functional.Action1;
import bruce.common.functional.Pointer;
import bruce.common.productLine.ProductLine;
import bruce.common.productLine.WorkUnit;
import bruce.common.productLine.WorkUnitGroup;

public final class DownloaderAgent {
	private ProductLine<DownloadTask> productLine;
	public final int downloaderCount;
	private final int maxTryTimes;

	@SuppressWarnings("unchecked")
	protected DownloaderAgent(int threadCount, int tryTimes, final Action1<String> endingCallBack) {
		downloaderCount = threadCount;
		maxTryTimes = tryTimes;
		productLine = new ProductLine<DownloadTask>(createDownloadWorkerGroup(), new FileCater(endingCallBack));
	}

	protected WorkUnitGroup<DownloadTask> createDownloadWorkerGroup() {
		List<WorkUnit<DownloadTask>> workerList = new ArrayList<WorkUnit<DownloadTask>>();
		Pointer<Integer> tryTimesPtr = new Pointer<Integer>(maxTryTimes);
		for (int i = 0; i < downloaderCount; i++) {
			workerList.add(new DownloadWorker(tryTimesPtr));
		}
		return new WorkUnitGroup<DownloadTask>(workerList);
	}

	/**
	 * 创建下载任务，可以创建多个，并全部开始下载
	 * @param url
	 * @param savePath
	 * @param resumable
	 * @param progressCallback
	 * @return
	 * @throws IOException
	 * @throws CloneNotSupportedException
	 */
	public DownloadTask createDownloadTask(String url, String savePath, Action1<Float> progressCallback)
			throws IOException, CloneNotSupportedException {
		DownloadTask t = DownloadTask.create(url, savePath, downloaderCount);
		t.setProgressCallback(progressCallback);
		t.setAgent(this);
		return t;
	}

	/**
	 * 开始下载
	 * @param task
	 */
	public void doDownload(DownloadTask task) {
		productLine.appendTask(task);
	}
}
