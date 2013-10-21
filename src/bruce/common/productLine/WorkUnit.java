package bruce.common.productLine;

import java.util.HashMap;
import java.util.Map;

import bruce.common.utils.CommonUtils;

/**
 * 施工单位，可以是生产线、施工单位组或工人
 * @author Bruce
 *
 */
public abstract class WorkUnit<T extends Task> {
	private static Map<String, String> mBlackBoardMap = new HashMap<String, String>();
	protected WorkUnit<T> mBelongsToWorkUnit;
	int indexInParent;

	public WorkUnit() { }

	/**
	 * 取得施工单位的名称和ID
	 * @return
	 */
	protected String getName() {
		return CommonUtils.buildString(getClass().getSimpleName(), "#", getId());
	}

	/**
	 * 取得ID，比如工人的ID是010，则表示：工人正处于根生产线的首个施工单位里面的第二个子施工单位的第一个工人
	 * @return
	 */
	protected String getId() {
		if (mBelongsToWorkUnit == null) {
			return "";
		}
		return CommonUtils.buildString(mBelongsToWorkUnit.getId(), indexInParent);
	}

	/**
	 * 读取黑板，黑板是用key-value存储数据的共享空间
	 * @param clue
	 * @return
	 */
	public String readBlackBoard(final String clue) {
		return mBlackBoardMap.get(clue);
	}

	/**
	 * 写入黑板， {@link #readBlackBoard(String)}
	 * @param name
	 * @param data
	 */
	protected void writeBlackBoard(final String name, final String data) {
		mBlackBoardMap.put(name, data);
	}

	/**
	 * 设置这个施工单位的上级单位
	 * @param w 上级施工单位
	 * @param index 此施工单位处于上级容器中的位置
	 */
	protected void setBelongsTo(final WorkUnit<T> w, final int index) {
		mBelongsToWorkUnit = w;
		indexInParent = index;
	}

	/**
	 * 添加任务
	 * @param t	需要添加的任务
	 */
	public abstract void appendTask(final T t);

	/**
	 * 下属施工单位完成了一个任务的回调函数
	 * @param w	完成任务的下属
	 * @param t	完成的任务
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
