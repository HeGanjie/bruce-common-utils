package bruce.common.productLine;


/**
 * 施工单位工厂，根据服务的枚举类型创建生产线
 * @author Bruce
 *
 */
public abstract class ProductLineFactory {

	/**
	 * 新的生产线(需要重新实现：根据不同的服务名称，生成不同的ProductLine)
	 * @param supportService
	 * @return
	 */
	public abstract ProductLine<?> newProductLine(final String supportServiceName);

	/*
	@Override
	public ProductLine newProductLine(String supportServiceName) {
		if (supportServiceName.equals("Hello World!")) {
			return new ProductLine(
					new WorkerGroup(new SaveFormDataWorker(), new SaveFileWorker()),
					new ContinueWorkFlowWorker());
		}
		return null;
	}
	}*/
}
