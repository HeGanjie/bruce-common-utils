package bruce.common.productLine;


/**
 * ʩ����λ���������ݷ����ö�����ʹ���������
 * @author Bruce
 *
 */
public abstract class ProductLineFactory {

	/**
	 * �µ�������(��Ҫ����ʵ�֣����ݲ�ͬ�ķ������ƣ����ɲ�ͬ��ProductLine)
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
