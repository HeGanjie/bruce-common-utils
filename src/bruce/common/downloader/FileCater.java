package bruce.common.downloader;

import java.io.File;
import java.util.List;

import bruce.common.functional.Action1;
import bruce.common.functional.Func1;
import bruce.common.functional.LambdaUtils;
import bruce.common.productLine.Worker;
import bruce.common.utils.FileUtil;

public final class FileCater extends Worker<DownloadTask> {
	private Action1<String> _finishCallBack;

	protected FileCater(Action1<String> finishCallBack) { _finishCallBack = finishCallBack; }

	@Override
	protected boolean handleTask(DownloadTask mainTask) {
		List<File> selected = LambdaUtils.select(mainTask.tasks, new Func1<File, DownloadTask>() {
			@Override
			public File call(DownloadTask t) { return new File(t.saveFileAt); }
		});
		File destFile = selected.remove(0);
		File[] files = selected.toArray(new File[]{});
		FileUtil.catFile(destFile, files);
		destFile.renameTo(new File(mainTask.saveFileAt));

		mainTask.deleteTempFile();
		_finishCallBack.call(mainTask.saveFileAt);
		mainTask.markFinished();
		return true;
	}
}
