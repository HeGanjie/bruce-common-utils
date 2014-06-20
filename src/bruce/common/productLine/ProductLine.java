package bruce.common.productLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bruce.common.utils.CommonUtils;

/**
 * 生产线，持有串联的施工单位(WorkUnit)
 * @author Bruce
 *
 */
public final class ProductLine<T extends Task> extends WorkUnit<T> {
	final List<WorkUnit<T>> mWorkUnits;

	/**
	 * 创建生产线
	 * @param workUnits	绑定在此生产线上的施工单位，
	 * 一旦创建则不能修改，并且任务传递的顺序是以创建时的顺序为准
	 * 不推荐少于2个参数，如果少于2个参数的话，推荐只使用一个工人类
	 */
	public ProductLine(final WorkUnit<T> ...workUnits) {
		//assert(workUnits.length > 1);

		mWorkUnits = Arrays.asList(workUnits);
		for (int i = 0; i < workUnits.length; i++) {
			workUnits[i].setBelongsTo(this, i);
		}
	}

	public ProductLine(List<WorkUnit<T>> workUnits) {
		mWorkUnits = new ArrayList<WorkUnit<T>>();
		int index = 0;
		for (WorkUnit<T> workUnit : workUnits) {
			mWorkUnits.add(workUnit);
			workUnit.setBelongsTo(this, index++);
		}
	}
	
	/**
	 * 生产线上的某个工人完成了任务，将任务传递给下个工人或者返回给上级
	 */
	@Override
	public void workUnitDoneTask(final WorkUnit<T> w, final T t) {
		int nextWorkeUnitIndex = mWorkUnits.indexOf(w) + 1;
		if (CommonUtils.isLegalIndex(mWorkUnits, nextWorkeUnitIndex)) {
			mWorkUnits.get(nextWorkeUnitIndex).appendTask(t);
		} else {
			super.workUnitDoneTask(this, t);
		}
	}

	/**
	 * 添加任务 @see WorkUnit#appendTask(Task)
	 */
	@Override
	public void appendTask(final T t) {
		mWorkUnits.get(0).appendTask(t);
	}

	public WorkUnit<T> getChildAt(final int index) {
		return mWorkUnits.get(index);
	}

}
