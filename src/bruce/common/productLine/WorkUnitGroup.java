package bruce.common.productLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ʩ����λ�飬���в�����ʩ����λ����ͬ������ĵ�λ��ʩ����λ�������ʩ����λ�ܹ�ͬʱ����ͬһ������
 * ���֮��֪ͨWorkUnitGroup��Ȼ��WorkUnitGroup�ȴ�ͬһ��������ɺ�����ϼ�ʩ����λ�㱨.
 * @author Bruce
 *
 */
public final class WorkUnitGroup<T extends Task> extends WorkUnit<T> {
	List<WorkUnit<T>> mWorkUnits;
	protected Map<T, Set<WorkUnit<T>>> doneTaskWorkUnitMap = new HashMap<T, Set<WorkUnit<T>>>();

	/**
	 * ���Ƽ�worker������С��2�����ֻ��һ��worker�Ļ�Ӧ��ֱ��ʵ����Worker
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
	 * ͬ���������ĳ�����������е����й��˶������֮��Ż᷵�ظ��ϼ�
	 * ��Ϊ�����ж������ͬʱ���ô˷���������������ͬ��
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
		if (donedWorkUnits.size() == mWorkUnits.size()) { //�����˶����������
			doneTaskWorkUnitMap.remove(t);
			super.workUnitDoneTask(this, t);
		}
	}

	public WorkUnit<T> getChildAt(final int index) {
		return mWorkUnits.get(index);
	}

}
