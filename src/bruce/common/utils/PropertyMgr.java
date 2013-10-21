package bruce.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ksoap2 Ĭ�����ö�ȡ��
 * @author Bruce
 *
 */
public class PropertyMgr {
//	private static Properties props = new Properties();
//
//	static {
//		try {
//			props.load(PropertyMgr.class.getClassLoader().getResourceAsStream("ksoap2.properties"));
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//	}

	private PropertyMgr() {};
	private static Properties props;

	/**
	 * ��InputStream��ȡ������Ϣ
	 * @param configStream
	 * @param key
	 * @return
	 */
	public static String getProperty(InputStream configStream, String key) {
		if(props == null){
			props = new Properties();
			try {
				props.load(configStream);
				configStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return props.getProperty(key);
	}
}
