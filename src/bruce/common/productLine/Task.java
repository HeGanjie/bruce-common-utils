package bruce.common.productLine;

import java.io.Serializable;

/**
 * 任务接口
 * 为了使任务独一无二，必须重写 {@link #hashCode()} 和 {@link #equals(Object)} 方法
 * @author Bruce
 *
 */
public interface Task extends Serializable {
	public int hashCode();
	public boolean equals(Object obj);
}
