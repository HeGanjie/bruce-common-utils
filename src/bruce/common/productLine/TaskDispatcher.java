package bruce.common.productLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务分发者，缓存工厂和ProductLine
 * 可将需要处理的任务通过dispatchTask加入，多个工厂能有相同的supportedService
 * @author Bruce
 *
 */
public final class TaskDispatcher {
	Map<String, List<ProductLine<?>>> mProductLinesNameMap = new HashMap<String, List<ProductLine<?>>>();
	private List<ProductLineFactory> factoryList = null;

	/**
	 * 创建任务处理工厂(如果只有一个工厂的话推荐直接使用具体的ProductLineFactory)
	 * @param factory	生产线工厂
	 */
	public TaskDispatcher(final ProductLineFactory ...factories) {
		assert(factories.length > 1);
		factoryList = Arrays.asList(factories);
	}

	/**
	 * 分发任务，将任务根据选择的服务分发给内定的生产线
	 * @param supportedService	生产线工厂支持的服务
	 * @param t	任务
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