package bruce.common.productLine;

import java.io.Serializable;

/**
 * ����ӿ�
 * Ϊ��ʹ�����һ�޶���������д {@link #hashCode()} �� {@link #equals(Object)} ����
 * @author Bruce
 *
 */
public interface Task extends Serializable {
	public int hashCode();
	public boolean equals(Object obj);
}
