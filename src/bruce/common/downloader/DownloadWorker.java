package bruce.common.downloader;

import java.io.FileNotFoundException;

import bruce.common.functional.Pointer;
import bruce.common.productLine.Worker;
import bruce.common.utils.CommonUtils;

public final class DownloadWorker extends Worker<DownloadTask> {
	private DownloadTask myTask;
	private int myIndex = -1;
	private Pointer<Integer> maxTryTimesPtr;

	protected DownloadWorker(Pointer<Integer> tryTimesPtr) {
		maxTryTimesPtr = tryTimesPtr;
	}

	@Override
	protected boolean handleTask(DownloadTask mainTask) {
		myTask = getMyTask(mainTask);
		
		while (myTask.getState() != DownloadTaskState.finished &&
				mainTask.getState() == DownloadTaskState.downloading &&
				maxTryTimesPtr.value > 0) {
			try {
				myTask.download(mainTask, (long) Math.ceil((float)mainTask.downLen / 100));
				CommonUtils.trace(getClass(), CommonUtils.buildString("分段 ", myIndex,
						" 下载 ", myTask.getState().name(), " ", myTask.saveFileAt));
			} catch (RuntimeException e) {
				maxTryTimesPtr.value--;
				e.printStackTrace();
				if (e.getCause() instanceof FileNotFoundException) {
					CommonUtils.trace(getClass(), "分段 " + myIndex + " 文件不存在，下载失败！");
					if (mainTask.getState() == DownloadTaskState.downloading) { //使得只会通知一次
						mainTask.stop();
						mainTask.deleteTempFile();
					}
					return false;
				}
				if (maxTryTimesPtr.value > 0)
					CommonUtils.trace(getClass(), "分段 " + myIndex + " 下载失败！重试...");
				else {
					CommonUtils.trace(getClass(), "分段 " + myIndex + " 下载失败！");
					mainTask.allSubTaskIsFailure();
				}
			}
		}
		return myTask.getState() == DownloadTaskState.finished;
	}

	protected DownloadTask getMyTask(DownloadTask mainTask) {
		if (myIndex < 0) {
			String id = getId();
			String subId = id.substring(id.length() - 1);
			myIndex = Integer.parseInt(subId);
		}
		return mainTask.tasks.get(myIndex);
	}
}
