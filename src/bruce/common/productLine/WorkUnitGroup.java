package bruce.common.productLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 施工单位组，持有并联的施工单位，是同步任务的单位，施工单位组里面的施工单位能够同时处理同一个任务，
 * 完成之后通知WorkUnitGroup，然后WorkUnitGroup等待同一个任务完成后就向上级施工单位汇报.
 * @author Bruce
 *
 */
public final class WorkUnitGroup<T extends Task> extends WorkUnit<T> {
	List<WorkUnit<T>> mWorkUnits;
	protected Map<T, Set<WorkUnit<T>>> doneTaskWorkUnitMap = new HashMap<T, Set<WorkUnit<T>>>();

	/**
	 * 不推荐worker的数量小于2，如果只有一个worker的话应该直接实例化Worker
	 * @param workers
	 */
	public WorkUnitGroup(final WorkUnit<T> ...workUnits) {
		assert(workUnits.length > 1);
		mWorkUnits = Arrays.asList(workUnits);
		for (int i = 0; i < workUnits.length; i++) {
			workUnits[i].setBelongsTo(this, i);
		}
	}

	public WorkUnitGroup(List<WorkUnit<T>> workUnits) {
		mWorkUnits = new ArrayList<WorkUnit<T>>();
		int index = 0;
		for (WorkUnit<T> workUnit : workUnits) {
			mWorkUnits.add(workUnit);
			workUnit.setBelongsTo(this, index++);
		}
	}
	
	@Override
	public void appendTask(final T t) {
		for (WorkUnit<T> workUnit : mWorkUnits) {
			workUnit.appendTask(t);
		}
	}

	/**
	 * 同步完成任务，某个任务会等组中的所有工人都完成了之后才会返回给上级
	 * 因为可能有多个工人同时调用此方法，所以设置了同步
	 */
	@Override
	public synchronized void workUnitDoneTask(final WorkUnit<T> workUnit, final T t) {
		Set<WorkUnit<T>> donedWorkUnits = doneTaskWorkUnitMap.get(t);
		if (donedWorkUnits == null) {
			donedWorkUnits = new HashSet<WorkUnit<T>>();
			donedWorkUnits.add(workUnit);
			doneTaskWorkUnitMap.put(t, donedWorkUnits);
		} else {
			donedWorkUnits.add(workUnit);
		}
		if (donedWorkUnits.size() == mWorkUnits.size()) { //所有人都完成了任务
			doneTaskWorkUnitMap.remove(t);
			super.workUnitDoneTask(this, t);
		}
	}

	public WorkUnit<T> getChildAt(final int index) {
		return mWorkUnits.get(index);
	}

}
