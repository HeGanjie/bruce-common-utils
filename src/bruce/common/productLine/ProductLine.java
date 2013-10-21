package bruce.common.productLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bruce.common.utils.CommonUtils;

/**
 * �����ߣ����д�����ʩ����λ(WorkUnit)
 * @author Bruce
 *
 */
public final class ProductLine<T extends Task> extends WorkUnit<T> {
	List<WorkUnit<T>> mWorkUnits;

	/**
	 * ����������
	 * @param workUnits	���ڴ��������ϵ�ʩ����λ��
	 * һ�����������޸ģ��������񴫵ݵ�˳�����Դ���ʱ��˳��Ϊ׼
	 * ���Ƽ�����2���������������2�������Ļ����Ƽ�ֻʹ��һ��������
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
	 * �������ϵ�ĳ��������������񣬽����񴫵ݸ��¸����˻��߷��ظ��ϼ�
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
	 * ������� @see WorkUnit#appendTask(Task)
	 */
	@Override
	public void appendTask(final T t) {
		mWorkUnits.get(0).appendTask(t);
	}

	public WorkUnit<T> getChildAt(final int index) {
		return mWorkUnits.get(index);
	}

}
