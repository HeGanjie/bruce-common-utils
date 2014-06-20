package bruce.common.productLine;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 工人类，保证一有任务立即启动、逐个完成任务并且完成一个通知一个给上级Group或ProductLine
 * @author Bruce
 *
 */
public abstract class Worker<T extends Task> extends WorkUnit<T> implements Runnable {
	final protected Queue<T> mTasks = new LinkedBlockingQueue<T>();
	private Thread workerThread = createNewThread();

	private Thread createNewThread() {
		return new Thread(this, getName());
	}

	/**
	 * 添加任务，工人会自动启动独有的线程来处理任务
	 */
	@Override
	public synchronized void appendTask(final T t) {
		mTasks.add(t);

		// 保证加入Task之后，能够马上被执行
		if (workerThread.getState() == Thread.State.NEW) {
			//有可能start之前线程已经start了
			workerThread.start();
		}
	}

	/**
	 * 执行任务，所有的任务都执行完成后线程停止，如果再添加，又会再重新执行此方法
	 */
	@Override
	public void run() {
		while (!mTasks.isEmpty()) {
			T t =  mTasks.remove();
			if (handleTask(t)) {
				workUnitDoneTask(this, t);
			}
		}
		synchronized (this) {
			workerThread = createNewThread();
			//有可能执行完createNewThread就已经有新任务了，这时候需要继续执行
			if (!mTasks.isEmpty()) {
				workerThread.start();
			}
		}
	}

	/**
	 * 让子类实现处理任务的逻辑
	 * @param t	任务
	 * @return	是否传递这个任务给下一个worker
	 */
	protected abstract boolean handleTask(T t);

	/**
	 * 
	 * @param task
	 * @return		任务本来已经在队列中
	 */
	public boolean requeueTask(T task) {
		boolean contains = false;
		synchronized (mTasks) { //避免在此期间任务被执行
			contains = mTasks.contains(task);
			if (contains) mTasks.remove(task);
		}
		appendTask(task);
		return contains;
	}
}
