package bruce.common.utils;

import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * ͨ�ù��߼�
 * @author Bruce
 *
 */
public final class CommonUtils {
	private static final String DEBUGGING_ACTIVITY_SIMPLE_NAME = "DownloadUtils";
//	public static final boolean DEBUGGING = true;
	public static final boolean DEBUGGING = false;

	private static Set<String> debuggingActivityList = new HashSet<String>();

	static {
		for (String simpleName : DEBUGGING_ACTIVITY_SIMPLE_NAME.split(",")) {
			debuggingActivityList.add(simpleName);
		}
	}

	/**
	 * ������ת�����ַ����������Ӧ����Arrays.toString(Object[] arr)
	 * @param arrObjects	��������
	 * @param joinString	����ÿ�����ݵ��ַ���
	 * @return	ת�����
	 */
	public static String displayArray(final Object[] arrObjects, final String joinString) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arrObjects.length; i++) {
			sb.append(arrObjects[i]);
			if (i + 1 < arrObjects.length) sb.append(joinString);
		}
		return sb.toString();
	}

	/**
	 * ��List��toString֮���ֵ����������List<br/>
	 * ע�⣺���ص��������ַ����б�����toStringǰ�������е�Ԫ��toString��Ӧ�ð���", "
	 * @param listStr
	 * @return
	 */
	public static List<String> parseList(String listStr) {
		if (isStringNullOrWriteSpace(listStr) || listStr.length() < 2) return null;
		listStr = listStr.substring(1, listStr.length() - 1);
		if (isStringNullOrWriteSpace(listStr)) return new ArrayList<String>();
		
		return Arrays.asList(listStr.split(", "));
	}

	public static Set<String> parseSet(String setStr) {
		List<String> parseList = parseList(setStr);
		if (parseList == null) return null;
		return new HashSet<String>(parseList);
	}

	/**
	 * ��Ч�ʵظ��ݲ����������ַ���
	 * @param args
	 * @return
	 */
	public static String buildString(final Object ... args) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++)
			sb.append(args[i]);
		return sb.toString();
	}

	/**
	 * ��ʽ�����������Ϣ��ʹ�÷������ƣ�{@link String#format(String, Object...)}
	 * @param format
	 * @param objects
	 */
	public static void formatTrace(final String format, final Object ... objects) {
		trace(String.format(format, objects));
	}

	/**
	 * ȡ�÷������β��������飬�����ڽӿڵķ����������������磺@ParamsDetail( ParamsDetail_paramName="username,password")
	 * @param method
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws Throwable
	 */
	public static String[] getParamsName(final Method method, final String seperator) throws Throwable {
		String paramsName = "";
		Annotation[] annotations = method.getAnnotations();
		if (annotations == null || annotations.length == 0) return null;

		Annotation annotation = annotations[0];

		String annotationSimpleName = annotation.annotationType().getSimpleName();
		Method[] declaredMethods = annotation.getClass().getDeclaredMethods();
		for (Method m : declaredMethods) {
			if (m.getName().startsWith(annotationSimpleName)) {
				paramsName = m.invoke(annotation).toString();
				break;
			}
		}
		if ("".equals(paramsName)) return null;
		return paramsName.split(seperator);
	}

	/**
	 * �ж��ַ����Ƿ�Ϊ�ջ�null
	 * @param mValue
	 * @return
	 */
	public static boolean isStringNullOrEmpty(final String mValue) {
		return mValue == null || mValue.isEmpty();
	}

	/**
	 * �ж��ַ����Ƿ�Ϊ�ջ�null
	 * @param mValue
	 * @return
	 */
	public static boolean isStringNullOrWriteSpace(final String mValue) {
		return mValue == null || mValue.trim().isEmpty();
	}

	/**
	 * �ж��ַ����Ƿ�ƥ��������ʽ
	 * @param preMatch	�ַ���
	 * @param regex	������ʽ
	 * @return
	 */
	public static boolean matchRegex(final String preMatch, final String regex) {
		if (isStringNullOrWriteSpace(regex)) return false;
		return preMatch.matches(regex);
	}

	/**
	 * ������������ {@link #DEBUGGING} Ϊfalse�����
	 * @param msg
	 */
	public static void traceSpec(Class<?> cls, final String msg) {
		if (DEBUGGING && debuggingActivityList.contains(cls.getSimpleName()))
			System.out.println(msg);
	}
	
	public static void trace(Class<?> class1, String msg) {
		System.out.println(buildString(class1.getSimpleName(), " | ", msg));
	}
	
	/**
	 * ������������ {@link #DEBUGGING} Ϊfalse�����
	 * @param msg
	 */
	public static void trace(final String msg) {
		if(DEBUGGING)
			System.out.println(msg);
	}

	public static void centerTrace(final String msg) {
		if (DEBUGGING)
			System.out.println(center(msg, 60, " "));
	}
	
	/**
	 * ���쳣����ȡ���еĴ�����Ϣ
	 * @param t
	 * @return
	 */
	public static String extractErrorMessage(final Throwable t) {
		if (t != null)
			return String.format("%s\n\n%s", t.toString(), extractErrorMessage(t.getCause()));
		return "";
	}

	/**
	 * �ַ������	����: stringDecrease("abc123", "123") == "abc"
	 * @param src	Դ�ַ���
	 * @param regex	����
	 * @return
	 */
	public static String stringDecrease(final String src, final String regex) {
		return src.replaceFirst(regex, "");
	}

	/**
	 * ��ʽ������
	 * @param d	����
	 * @param seperator	������ʱ��֮��ķָ���
	 * @param dateFormatStr	���ڵĸ�ʽ
	 * @param timeFormatStr	ʱ���ʽ
	 * @return
	 */
	public static String dateFormat(final Date d, final String seperator,
			final String dateFormatStr, final String timeFormatStr) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
		SimpleDateFormat timeFormat = new SimpleDateFormat(timeFormatStr);
		return String.format("%s%s%s", dateFormat.format(d), seperator, timeFormat.format(d));
	}

	/**
	 * ʹ��Ĭ�ϵ�����ʱ���ʽ��ʽ������
	 * @see CommonUtils#dateFormat(Date, String, String, String)
	 * @param d
	 * @param seperator
	 * @return
	 */
	public static String dateFormat(final Date d, final String seperator) {
		return dateFormat(d, seperator, "yyyy-MM-dd", "HH:mm:ss.SSS");
	}

	/**
	 * ���������ַ���
	 * @param dateStr	�����ַ���
	 * @param seperatorRegex	������ʱ��֮��ķָ�������
	 * @param dateFormatStr		���ڸ�ʽ
	 * @param timeFormatStr		ʱ���ʽ
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(final String dateStr, final String seperatorRegex,
			final String dateFormatStr, final String timeFormatStr) throws ParseException {
		String[] splitd = dateStr.split(seperatorRegex);
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
		Date parse1 = dateFormat.parse(splitd[0]);
		
		if (splitd.length == 1) return parse1;
		//ʱ����к�����޺��봦��
		String timeStr = splitd[1];
		Date parse2 = null;
		SimpleDateFormat timeFormat = new SimpleDateFormat(timeFormatStr);
		parse2 = timeFormat.parse(timeStr);
		parse2.setYear(parse1.getYear());
		parse2.setMonth(parse1.getMonth());
		parse2.setDate(parse1.getDate());
		return parse2;
	}

	/**
	 * �Զ����������ַ���
	 * @see CommonUtils#parseDate(String, String, String, String)
	 * @param dateStr
	 * @param seperatorRegex
	 * @return
	 */
	public static Date parseDate(final String dateStr, final String seperatorRegex) {
		try {
			if (0 < dateStr.indexOf('.'))
				return parseDate(dateStr, seperatorRegex, "yyyy-MM-dd", "HH:mm:ss.SSS");
			else if (dateStr.length() == "yyyy-MM-dd HH:mm".length())
				return parseDate(dateStr, seperatorRegex, "yyyy-MM-dd", "HH:mm");
			else
				return parseDate(dateStr, seperatorRegex, "yyyy-MM-dd", "HH:mm:ss");
		} catch (ParseException e) { e.printStackTrace(); }
		return null;
	}

	/**
	 * ���ַ�������16���Ƶ���ʽת��Ϊ�ַ��������磺displayBytes(new byte[]{154, 186}) = "9ABA"
	 * @param bytes
	 * @return
	 */
	public static String displayBytes(final byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hexString = Integer.toHexString(0x000000FF & bytes[i]);
			if (hexString.length() == 1) sb.append('0');
			sb.append(hexString);
		}
		return sb.toString();
	}

	/**
	 * �Ǻ��� {@link #displayBytes(byte[])}��������
	 * @param data
	 * @return
	 */
	public static byte[] toByteArray(final String data) {
		byte[] result = new byte[(data.length() + 1) / 2];
		int pos = 0;
		for (int i = 0; i < data.length(); i+=2) {
			result[pos++] = Short.decode("#" + data.substring(i, i + 2)).byteValue();
		}
		return result;
	}

	/**
	 * �����ַ����Ƚ׶��ַ���������
	 * @param str
	 * @param length
	 * @return
	 */
	public static List<String> breakStringByLength(String str, final int length) {
		List<String> strs = new ArrayList<String>();
		while (str.length() > length) {
			strs.add(str.substring(0, length));
			str = str.substring(length);
		}
		strs.add(str);
		return strs;
	}

	/**
	 * �±�index�Ƿ����б�l�Ϸ����±�
	 * @param l
	 * @param index
	 * @return
	 */
	public static boolean isLegalIndex(final List<?> l, final int index) {
		return 0 <= index && index < l.size();
	}
	
	public static Map<String, String> parseHashMap(String mapToStringResult) {
		return parseHashMap(mapToStringResult, ", ");
	}
	
	/**
	 * ����HashMap.toString()��Ľ������ԭHashMapʵ���� Map��key��value���ܺ���", "(����+�ո�)
	 * @param mapDescriptionStr
	 * @return
	 */
	public static Map<String, String> parseHashMap(String mapDescriptionStr, String seprator) {
		if (isStringNullOrWriteSpace(mapDescriptionStr) || mapDescriptionStr.length() < 2) return null;

		mapDescriptionStr = mapDescriptionStr.substring(1, mapDescriptionStr.length() - 1);
		if (isStringNullOrWriteSpace(mapDescriptionStr)) return new LinkedHashMap<String, String>();
		
		String[] kvs = mapDescriptionStr.split(seprator);
		Map<String, String> map = null;
		for (String kv : kvs) {
			if (map == null) map = new LinkedHashMap<String, String>();
			String[] split = kv.split("=");
			if (split.length == 1)
				map.put(split[0], "");
			else
				map.put(split[0], split[1]);
		}
		return map;
	}

	/**
	 * ȡ���ַ�����MD5
	 * @param str
	 * @return
	 */
	public static String getMD5(final String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return displayBytes(md5.digest(str.getBytes()));
        } catch(Exception e) {
            throwRuntimeExceptionAndPrint(e);
        }
        return "";
    }

	/**
	 * �����ַ������ַ����ȣ������˳��ȵĻ���������ַ�����������endingString
	 * @param src
	 * @param keepLength
	 * @param endingString
	 * @return
	 */
	public static String limitStringCharCount(final String src, final int keepLength, final String endingString) {
		if (src != null && src.length() > keepLength) {
			return src.substring(0, keepLength) + endingString;
		} else {
			return src;
		}
	}

	/**
	 * �����ַ������ȣ����ַ�����Ϊ��λ�������ַ������������ǵĳ���Ϊ1��ȫ��Ϊ2��
	 * @param src		Դ�ַ���
	 * @param maxByteCharLength		����ַ�������
	 * @param endingString		�����ַ���
	 * @param endingStringByteLength		�����ַ�������
	 * @return
	 * @throws IOException
	 */
	public static String limitStringLength(final String src, final int maxByteCharLength,
			final String endingString, final int endingStringByteLength) {
		if (isStringNullOrWriteSpace(src)) return src;

		StringReader sr = new StringReader(src);
		int read = 0;
		List<Character> arrayList = new ArrayList<Character>();
		int byteCharCount = 0, shortCharCount = 0;

		try {
			//ѭ����ɺ�������������ָ�����������ƣ�	a|		��|		��|��
			while ((read = sr.read()) != -1) {
				if (read < 256) {
					byteCharCount++;
				} else {
					shortCharCount++;
				}
				arrayList.add((char) read);
				if (byteCharCount + (shortCharCount << 1) >= maxByteCharLength) {
					read = sr.read();
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		int charLen = byteCharCount + (shortCharCount << 1);
		if (read != -1) {
			//���滹���ַ��������ַ�ֱ�����ڵ���maxByteCharLength - endingStringByteLengthλ������endingString
			if (charLen > maxByteCharLength) {
				arrayList.remove(arrayList.size() - 1);
				arrayList.remove(arrayList.size() - 1);
			} else if (charLen == maxByteCharLength) {
				if (arrayList.remove(arrayList.size() - 1) < 256) {
					arrayList.remove(arrayList.size() - 1);
				}
			}
			return displayArray(arrayList.toArray(), "") + endingString;
		} else {
			if (charLen > maxByteCharLength) {
				arrayList.remove(arrayList.size() - 1);
				arrayList.remove(arrayList.size() - 1);
				return displayArray(arrayList.toArray(), "") + endingString;
			} else {
				return displayArray(arrayList.toArray(), "");
			}
		}
	}
	
	/**
	 * �����ַ������ı��г��ֵĴ���
	 * @param src
	 * @param targetString
	 * @return
	 */
	public static int countStringInText(String src, String targetString) {
		if (isStringNullOrWriteSpace(src) || isStringNullOrWriteSpace(targetString)) return 0;
		int start = 0, index = -1, count = 0;
		while ((index = src.indexOf(targetString, start)) != -1) {
			count++;
			start = index + targetString.length();
		}
		return count;
	}

	/**
	 * �ӳ�һ��ʱ���ٷ���
	 * @param ms
	 */
	public static void delay(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throwRuntimeExceptionAndPrint(e);
		}
	}

	public static String noPointIfFloat(Object val) {
		if (!(val instanceof Number)) {
			return buildString(val);
		}
		float value =  ((Number) val).floatValue();
		if (value == (int) value) {
			return String.format("%d", (int) value);
		} else {
			return buildString(val);
		}
	}

	public static List<Integer> range(int low, int high) { // range(0, 5) => 0, 1, 2, 3, 4
		int step = (int) Math.signum(high - low);
		List<Integer> list = new ArrayList<Integer>(Math.abs(high - low));
		for (int i = low; i != high; i += step) {
			list.add(i);
		}
		return list;
	}
	
	private static String just(String src, int length, String fill, boolean ljust) throws IOException {
		if (isStringNullOrEmpty(fill) || length <= src.length()) {
			return src;
		} else {
			StringReader sr = new StringReader(fill);
			StringBuilder sb = new StringBuilder();
			if (ljust) sb.append(src);
			int toFillLen = length - src.length(), read = -1;
			while (toFillLen-- > 0) {
				if ((read = sr.read()) == -1) {
					sr.reset();
					read = sr.read();
				}
				sb.append((char)read);
			}
			if (!ljust) sb.append(src);
			return sb.toString();
		}
	}
	
	public static String ljust(String src, int length, String fill) {
		try {
			return just(src, length, fill, true);
		} catch (IOException e) { e.printStackTrace(); }
		return null;
	}
	
	public static String rjust(String src, int length, String fill) {
		try {
			return just(src, length, fill, false);
		} catch (IOException e) { e.printStackTrace(); }
		return null;
	}
	
	public static String center(String src, int length, String fill) {
		int leftJustLen = (length - src.length()) / 2 + src.length();
		String ljust = rjust(src, leftJustLen, fill);
		return ljust(ljust, length, fill);
	}
	
	public static double round(double src, int precision) {
		return new BigDecimal(src).setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static String emptyIfNull(String strValue) {
		return strValue == null ? "" : strValue;
	}
	
	public static void throwRuntimeExceptionAndPrint(Throwable e) {
		if (DEBUGGING) e.printStackTrace();
		throw new RuntimeException(e);
	}

	public static String hashMapToString(Map<?, ?> dataUploadStatusMap, String seperator) {
		List<Entry<?, ?>> list = new ArrayList<Entry<?, ?>>(dataUploadStatusMap.entrySet());
		return buildString('{', displayArray(list.toArray(), seperator), '}');
	}
	
	public static String getDateStrFromLongTime(final String time){
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	return sdf.format(Long.valueOf(time));
    }
	
	public static Date addDays(Date origin, double deltaDay) {
		return new Date(Math.round((origin.getTime() + deltaDay * 86400000)));
	}

	public static double getDaySpan(Date d1, Date d2) {
		return (double)(d1.getTime() - d2.getTime()) / 86400000;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getPrivateField(Object obj, String fieldName) {
		Field f;
		try {
			f = obj.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			return (T) f.get(obj);
		} catch (Exception e) {
			if (DEBUGGING) e.printStackTrace();
		}
		return null;
	}
	
	public static <E> List<List<E>> partitionAll(int n, List<E> ls) {
		List<List<E>> resultList = new ArrayList<List<E>>();
		int lsLen = ls.size();
		for (int i = 0, end; i < lsLen; i += n) {
			end = i + n;
			resultList.add(ls.subList(i, end <= lsLen ? end : lsLen));
		}
		return resultList;
	}
}