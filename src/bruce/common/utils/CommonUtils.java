package bruce.common.utils;

import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.DecimalFormat;
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
 * 通用工具集
 * @author Bruce
 *
 */
public final class CommonUtils {
	private static final DecimalFormat FILE_SIZE_FORMAT = new DecimalFormat("#,##0.#");
	private static final String[] FILE_SIZE_UNITS = new String[] { "B", "KB", "MB", "GB", "TB", "EB" };
	
	private static final String DEBUGGING_ACTIVITY_SIMPLE_NAME = "DownloadUtils";
//	public static final boolean DEBUGGING = true;
	public static final boolean DEBUGGING = false;

	private static Set<String> debuggingActivityList = new HashSet<String>();

	static {
		for (String simpleName : DEBUGGING_ACTIVITY_SIMPLE_NAME.split(",")) {
			debuggingActivityList.add(simpleName);
		}
	}

	public static String readableFileSize(long size) {
		if (size <= 0) return "0";
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return buildString(FILE_SIZE_FORMAT.format(size / Math.pow(1024, digitGroups)), " ", FILE_SIZE_UNITS[digitGroups]);
	}
	
	/**
	 * 将数组转换成字符串，简单起见应该用Arrays.toString(Object[] arr)
	 * @param arrObjects	对象数组
	 * @param joinString	隔开每个内容的字符串
	 * @return	转换结果
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
	 * 将List的toString之后的值解析，返回List<br/>
	 * 注意：返回的类型是字符串列表，并且toString前的数组中的元素toString后不应该包含", "
	 * @param listStr
	 * @return
	 */
	public static List<String> parseList(String listStr) {
		if (isStringNullOrWhiteSpace(listStr) || listStr.length() < 2) return null;
		listStr = listStr.substring(1, listStr.length() - 1);
		if (isStringNullOrWhiteSpace(listStr)) return new ArrayList<String>();
		
		return Arrays.asList(listStr.split(", "));
	}

	public static Set<String> parseSet(String setStr) {
		List<String> parseList = parseList(setStr);
		if (parseList == null) return null;
		return new HashSet<String>(parseList);
	}

	/**
	 * 高效率地根据参数，构造字符串
	 * @param args
	 * @return
	 */
	public static String buildString(final Object ... args) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++)
			sb.append(args[i]);
		return sb.toString();
	}
	
	public static String buildCompactString(final Object ... args) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (args[i] != null)
				sb.append(args[i]);
		}
		return sb.toString();
	}

	/**
	 * 格式化输出调试信息，使用方法类似：{@link String#format(String, Object...)}
	 * @param format
	 * @param objects
	 */
	public static void formatTrace(final String format, final Object ... objects) {
		trace(String.format(format, objects));
	}

	/**
	 * 取得方法的形参名称数组，必须在接口的方法名上声明，例如：@ParamsDetail( ParamsDetail_paramName="username,password")
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
	 * 判断字符串是否为空或null
	 * @param mValue
	 * @return
	 */
	public static boolean isStringNullOrEmpty(final String mValue) {
		return mValue == null || mValue.isEmpty();
	}

	/**
	 * 判断字符串是否为空或null
	 * @param mValue
	 * @return
	 */
	public static boolean isStringNullOrWhiteSpace(final String mValue) {
		return mValue == null || mValue.trim().isEmpty();
	}

	/**
	 * 判断字符串是否匹配正则表达式
	 * @param preMatch	字符串
	 * @param regex	正则表达式
	 * @return
	 */
	public static boolean matchRegex(final String preMatch, final String regex) {
		if (isStringNullOrWhiteSpace(regex)) return false;
		return preMatch.matches(regex);
	}

	/**
	 * 调试输出，如果 {@link #DEBUGGING} 为false则不输出
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
	 * 调试输出，如果 {@link #DEBUGGING} 为false则不输出
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
	 * 从异常中提取所有的错误信息
	 * @param t
	 * @return
	 */
	public static String extractErrorMessage(final Throwable t) {
		if (t != null)
			return String.format("%s\n\n%s", t.toString(), extractErrorMessage(t.getCause()));
		return "";
	}

	/**
	 * 字符串相减	比如: stringDecrease("abc123", "123") == "abc"
	 * @param src	源字符串
	 * @param regex	正则
	 * @return
	 */
	public static String stringDecrease(final String src, final String regex) {
		return src.replaceFirst(regex, "");
	}

	/**
	 * 格式化日期
	 * @param d	日期
	 * @param seperator	日期与时间之间的分隔符
	 * @param dateFormatStr	日期的格式
	 * @param timeFormatStr	时间格式
	 * @return
	 */
	public static String dateFormat(final Date d, final String seperator,
			final String dateFormatStr, final String timeFormatStr) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
		SimpleDateFormat timeFormat = new SimpleDateFormat(timeFormatStr);
		return String.format("%s%s%s", dateFormat.format(d), seperator, timeFormat.format(d));
	}

	/**
	 * 使用默认的日期时间格式格式化日期
	 * @see CommonUtils#dateFormat(Date, String, String, String)
	 * @param d
	 * @param seperator
	 * @return
	 */
	public static String dateFormat(final Date d, final String seperator) {
		return dateFormat(d, seperator, "yyyy-MM-dd", "HH:mm:ss.SSS");
	}

	/**
	 * 解析日期字符串
	 * @param dateStr	日期字符串
	 * @param seperatorRegex	日期与时间之间的分隔符正则
	 * @param dateFormatStr		日期格式
	 * @param timeFormatStr		时间格式
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(final String dateStr, final String seperatorRegex,
			final String dateFormatStr, final String timeFormatStr) throws ParseException {
		String[] splitd = dateStr.split(seperatorRegex);
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
		Date parse1 = dateFormat.parse(splitd[0]);
		
		if (splitd.length == 1) return parse1;
		//时间分有毫秒和无毫秒处理
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
	 * 自动解析日期字符串
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
	 * 将字符数组以16进制的形式转换为字符串，例如：displayBytes(new byte[]{154, 186}) = "9ABA"
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
	 * 是函数 {@link #displayBytes(byte[])}的逆向函数
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
	 * 根据字符长度阶段字符串到集合
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
	 * 下标index是否是列表l合法的下标
	 * @param l
	 * @param index
	 * @return
	 */
	public static boolean isLegalIndex(final List<?> l, final int index) {
		return l != null && 0 <= index && index < l.size();
	}
	
	public static Map<String, String> parseHashMap(String mapToStringResult) {
		return parseHashMap(mapToStringResult, ", ");
	}
	
	/**
	 * 解析HashMap.toString()后的结果，还原HashMap实例， Map的key和value不能含有", "(逗号+空格)
	 * @param mapDescriptionStr
	 * @return
	 */
	public static Map<String, String> parseHashMap(String mapDescriptionStr, String seprator) {
		if (isStringNullOrWhiteSpace(mapDescriptionStr) || mapDescriptionStr.length() < 2) return null;

		mapDescriptionStr = mapDescriptionStr.substring(1, mapDescriptionStr.length() - 1);
		if (isStringNullOrWhiteSpace(mapDescriptionStr)) return new LinkedHashMap<String, String>();
		
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
	 * 取得字符串的MD5
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
	 * 限制字符串的字符长度，超过了长度的话，后面的字符丢弃，补上endingString
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
	 * 限制字符串长度（以字符长度为单位，不是字符个数。假设半角的长度为1，全角为2）
	 * @param src		源字符串
	 * @param maxByteCharLength		最大字符串长度
	 * @param endingString		结束字符串
	 * @param endingStringByteLength		结束字符串长度
	 * @return
	 * @throws IOException
	 */
	public static String limitStringLength(final String src, final int maxByteCharLength,
			final String endingString, final int endingStringByteLength) {
		if (isStringNullOrWhiteSpace(src)) return src;

		StringReader sr = new StringReader(src);
		int read = 0;
		List<Character> arrayList = new ArrayList<Character>();
		int byteCharCount = 0, shortCharCount = 0;

		try {
			//循环完成后有三种情况，分隔符代表最长限制：	a|		啊|		口|阿
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
			//后面还有字符，丢弃字符直到少于等于maxByteCharLength - endingStringByteLength位，补充endingString
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
	 * 计算字符串在文本中出现的次数
	 * @param src
	 * @param targetString
	 * @return
	 */
	public static int countStringInText(String src, String targetString) {
		if (isStringNullOrWhiteSpace(src) || isStringNullOrWhiteSpace(targetString)) return 0;
		int start = 0, index = -1, count = 0;
		while ((index = src.indexOf(targetString, start)) != -1) {
			count++;
			start = index + targetString.length();
		}
		return count;
	}

	/**
	 * 延迟一段时间再返回
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

	public static void setPrivateField(Object obj, String fieldName, Object val) {
		try {
			Field f = obj.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(obj, val);
		} catch (Exception e) {
			if (DEBUGGING) e.printStackTrace();
		}
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