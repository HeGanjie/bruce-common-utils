package bruce.common.productLine;

import java.util.HashMap;
import java.util.Map;

import bruce.common.utils.CommonUtils;

/**
 * ʩ����λ�������������ߡ�ʩ����λ�����
 * @author Bruce
 *
 */
public abstract class WorkUnit<T extends Task> {
	private static Map<String, String> mBlackBoardMap = new HashMap<String, String>();
	protected WorkUnit<T> mBelongsToWorkUnit;
	int indexInParent;

	public WorkUnit() { }

	/**
	 * ȡ��ʩ����λ�����ƺ�ID
	 * @return
	 */
	protected String getName() {
		return CommonUtils.buildString(getClass().getSimpleName(), "#", getId());
	}

	/**
	 * ȡ��ID�����繤�˵�ID��010�����ʾ�����������ڸ������ߵ��׸�ʩ����λ����ĵڶ�����ʩ����λ�ĵ�һ������
	 * @return
	 */
	protected String getId() {
		if (mBelongsToWorkUnit == null) {
			return "";
		}
		return CommonUtils.buildString(mBelongsToWorkUnit.getId(), indexInParent);
	}

	/**
	 * ��ȡ�ڰ壬�ڰ�����key-value�洢���ݵĹ���ռ�
	 * @param clue
	 * @return
	 */
	public String readBlackBoard(final String clue) {
		return mBlackBoardMap.get(clue);
	}

	/**
	 * д��ڰ壬 {@link #readBlackBoard(String)}
	 * @param name
	 * @param data
	 */
	protected void writeBlackBoard(final String name, final String data) {
		mBlackBoardMap.put(name, data);
	}

	/**
	 * �������ʩ����λ���ϼ���λ
	 * @param w �ϼ�ʩ����λ
	 * @param index ��ʩ����λ�����ϼ������е�λ��
	 */
	protected void setBelongsTo(final WorkUnit<T> w, final int index) {
		mBelongsToWorkUnit = w;
		indexInParent = index;
	}

	/**
	 * �������
	 * @param t	��Ҫ��ӵ�����
	 */
	public abstract void appendTask(final T t);

	/**
	 * ����ʩ����λ�����һ������Ļص�����
	 * @param w	������������
	 * @param t	��ɵ�����
	 */
	public void workUnitDoneTask(final WorkUnit<T> w, final T t) {
		if (mBelongsToWorkUnit != null) {
			mBelongsToWorkUnit.workUnitDoneTask(w, t);
		} else {
			// end of productline
		}
	}

	public boolean belongsToAWorkUnit() {
		return mBelongsToWorkUnit != null;
	}
}
