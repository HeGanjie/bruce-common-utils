package bruce.common.productLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ����ַ��ߣ����湤����ProductLine
 * �ɽ���Ҫ���������ͨ��dispatchTask���룬�������������ͬ��supportedService
 * @author Bruce
 *
 */
public final class TaskDispatcher {
	Map<String, List<ProductLine<?>>> mProductLinesNameMap = new HashMap<String, List<ProductLine<?>>>();
	private List<ProductLineFactory> factoryList = null;

	/**
	 * ������������(���ֻ��һ�������Ļ��Ƽ�ֱ��ʹ�þ����ProductLineFactory)
	 * @param factory	�����߹���
	 */
	public TaskDispatcher(final ProductLineFactory ...factories) {
		assert(factories.length > 1);
		factoryList = Arrays.asList(factories);
	}

	/**
	 * �ַ����񣬽��������ѡ��ķ���ַ����ڶ���������
	 * @param supportedService	�����߹���֧�ֵķ���
	 * @param t	����
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void dispatchTask(final String supportedService, final Task t) {
		List<ProductLine<?>> lines = getProductLinesBySupportedService(supportedService);
		if (lines == null) {
			lines = new ArrayList<ProductLine<?>>();
			for (ProductLineFactory factory : factoryList) {
				ProductLine<?> productLine = factory.newProductLine(supportedService);
				if (productLine != null) lines.add(productLine);
			}
			mProductLinesNameMap.put(supportedService, lines);
		}

		for (ProductLine line : lines) {
			line.appendTask(t);
		}
	}

	public List<ProductLine<?>> getProductLinesBySupportedService(final String supportedService) {
		return mProductLinesNameMap.get(supportedService);
	}

}