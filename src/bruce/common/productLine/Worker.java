package bruce.common.productLine;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * �����࣬��֤һ�������������������������������һ��֪ͨһ�����ϼ�Group��ProductLine
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
	 * ������񣬹��˻��Զ��������е��߳�����������
	 */
	@Override
	public synchronized void appendTask(final T t) {
		mTasks.add(t);

		// ��֤����Task֮���ܹ����ϱ�ִ��
		if (workerThread.getState() == Thread.State.NEW) {
			//�п���start֮ǰ�߳��Ѿ�start��
			workerThread.start();
		}
	}

	/**
	 * ִ���������е�����ִ����ɺ��߳�ֹͣ���������ӣ��ֻ�������ִ�д˷���
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
			//�п���ִ����createNewThread���Ѿ����������ˣ���ʱ����Ҫ����ִ��
			if (!mTasks.isEmpty()) {
				workerThread.start();
			}
		}
	}

	/**
	 * ������ʵ�ִ���������߼�
	 * @param t	����
	 * @return	�Ƿ񴫵�����������һ��worker
	 */
	protected abstract boolean handleTask(T t);

	/**
	 * 
	 * @param task
	 * @return		�������Ѿ��ڶ�����
	 */
	public boolean requeueTask(T task) {
		boolean contains = false;
		synchronized (mTasks) { //�����ڴ��ڼ�����ִ��
			contains = mTasks.contains(task);
			if (contains) mTasks.remove(task);
		}
		appendTask(task);
		return contains;
	}
}
