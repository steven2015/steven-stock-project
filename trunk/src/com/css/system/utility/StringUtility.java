package com.css.system.utility;

import java.beans.Introspector;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.css.system.annotation.Static;

@Static
public final class StringUtility{
	public static final String lineSeparator = System.getProperty("line.separator");
	public static final String linuxLineSeparator = "\n";
	public static final String macLineSeparator = "\r";
	public static final String windowsLineSeparator = "\r\n";
	public static final Charset UTF8 = Charset.forName("UTF-8");
	public static final Charset BIG5 = Charset.forName("BIG5");
	public static final Charset GBK = Charset.forName("GBK");
	private static final Map<Character, char[]> simpToTradMap = new HashMap<Character, char[]>();
	private static final Map<Character, Character> tradToSimpMap = new HashMap<Character, Character>();
	private static final Set<Character> undeterminedSet = new HashSet<Character>();
	private static final char[] hexChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	public static enum StringType{
		/**
		 * the string has no characters
		 */
		EMPTY,
		/**
		 * English characters
		 */
		ENGLISH,
		/**
		 * numeric characters
		 */
		NUMBER,
		/**
		 * characters can be interpreted as both traditional and simplified characters
		 */
		CHINESE,
		/**
		 * traditional characters
		 */
		TRADITIONAL_CHINESE,
		/**
		 * simplified characters
		 */
		SIMPLIFIED_CHINESE,
		/**
		 * mixture of traditional and simplified characters
		 */
		MIXED_CHINESE,
		/**
		 * the string contains English, numeric and Chinese characters
		 */
		UNKNOWN
	};

	static{
		final ResourceBundle bundle = ResourceBundle.getBundle(StringUtility.class.getPackage().getName() + ".chinese_conversion");
		final String tradString = convert(bundle.getString("trad"), "ISO-8859-1", "UTF-8");
		final String simpString = convert(bundle.getString("simp"), "ISO-8859-1", "UTF-8");
		final String bothString = convert(bundle.getString("both"), "ISO-8859-1", "UTF-8");
		for(int i = 0; i < tradString.length(); i++){
			tradToSimpMap.put(tradString.charAt(i), simpString.charAt(i));
		}
		for(int i = 0; i < bothString.length(); i++){
			undeterminedSet.add(bothString.charAt(i));
		}
		final Map<Character, List<Character>> tmpMap = new HashMap<Character, List<Character>>();
		for(int i = 0; i < simpString.length(); i++){
			final Character simp = simpString.charAt(i);
			List<Character> trads = tmpMap.get(simp);
			if(trads == null){
				trads = new ArrayList<Character>();
				if(undeterminedSet.contains(simp)){
					trads.add(simp);
				}
			}
			final Character trad = tradString.charAt(i);
			if(trads.contains(trad) == false){
				trads.add(trad);
			}
			tmpMap.put(simp, trads);
		}
		for(final Character c : tmpMap.keySet()){
			final List<Character> trads = tmpMap.get(c);
			final char[] ary = new char[trads.size()];
			for(int i = 0; i < trads.size(); i++){
				ary[i] = trads.get(i);
			}
			simpToTradMap.put(c, ary);
		}
	}

	/**
	 * Static
	 */
	private StringUtility(){
	}
	/**
	 * Convert string from one charset to another.
	 * 
	 * @param input
	 * @param originalCharSet
	 * @param useCharSet
	 * @return
	 */
	public static final String convert(final String input, final String originalCharSet, final String useCharSet){
		try{
			return new String(input.getBytes(originalCharSet), useCharSet);
		}catch(final UnsupportedEncodingException e){
			return input;
		}
	}
	/**
	 * Get escaped BIG5 string.
	 * 
	 * @param input
	 * @return
	 */
	public static final String getEscapedBig5String(final String input){
		final byte[] bytes;
		try{
			bytes = input.getBytes("BIG5");
		}catch(final UnsupportedEncodingException e){
			return "";
		}
		final StringBuilder sb = new StringBuilder(bytes.length * 3);
		for(final byte b : bytes){
			sb.append('%').append(toHex(b));
		}
		return sb.toString();
	}
	/**
	 * Get hex string of byte.
	 * 
	 * @param b
	 * @return
	 */
	public static final String toHex(final byte b){
		return new String(new char[]{hexChars[(b & 0xff) >> 4], hexChars[b & 0xf]});
	}
	/**
	 * Get hex string of byte array.
	 * 
	 * @param bytes
	 * @return
	 */
	public static final String toHex(final byte[] bytes){
		final StringBuilder sb = new StringBuilder(bytes.length * 2);
		for(final byte b : bytes){
			sb.append(hexChars[(b & 0xff) >> 4]).append(hexChars[b & 0xf]);
		}
		return sb.toString();
	}
	public static final String getMessageDigest(final String algorithm, final String plainText) throws NoSuchAlgorithmException{
		return toHex(MessageDigest.getInstance(algorithm).digest(plainText.getBytes()));
	}
	public static final String decodeBase64(final String encodedText){
		return new String(Base64.decodeBase64(encodedText.getBytes()));
	}
	public static final String encodeBase64(final String plainText){
		return new String(Base64.encodeBase64(plainText.getBytes()));
	}
	public static final String capitalizeAfterUnderscore(final String p){
		final String lower = p.toLowerCase();
		final String upper = p.toUpperCase();
		final StringBuilder sb = new StringBuilder(p.length());
		boolean underScored = false;
		for(int i = 0; i < p.length(); i++){
			final char c = p.charAt(i);
			if(c == '_'){
				underScored = true;
			}else if(underScored){
				sb.append(upper.charAt(i));
				underScored = false;
			}else{
				sb.append(lower.charAt(i));
			}
		}
		return sb.toString();
	}
	public static final String decapitalize(final String p){
		return Introspector.decapitalize(p);
	}
	public static final String capitalize(final String p){
		return p.substring(0, 1).toUpperCase() + p.substring(1);
	}
	public static final String toJavaFieldName(final String p){
		boolean hasUppercase = false;
		boolean hasLowercase = false;
		boolean hasUnderScore = false;
		for(final char c : p.toCharArray()){
			if('a' <= c && c <= 'z'){
				hasLowercase = true;
			}else if('A' <= c && c <= 'Z'){
				hasUppercase = true;
			}else if(c == '_'){
				hasUnderScore = true;
			}
		}
		if(hasUppercase){
			if(hasLowercase){
				return decapitalize(p.replace("_", ""));
			}else{
				if(hasUnderScore){
					return capitalizeAfterUnderscore(p);
				}else{
					return p.toLowerCase();
				}
			}
		}else{
			if(hasLowercase){
				if(hasUnderScore){
					return capitalizeAfterUnderscore(p);
				}else{
					return p.toLowerCase();
				}
			}else{
				return "";
			}
		}
	}
	public static final boolean isNumeric(final char c){
		return c >= '0' && c <= '9';
	}
	public static final boolean isAlphabetic(final char c){
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}
	public static final boolean isLowercaseAlphabetic(final char c){
		return (c >= 'a' && c <= 'z');
	}
	public static final boolean isUppercaseAlphabetic(final char c){
		return (c >= 'A' && c <= 'Z');
	}
	public static final boolean isAlphanumeric(final char c){
		return isNumeric(c) || isAlphabetic(c);
	}
	public static final String stripEnd(final String input, final String... substrings){
		int endIndex = input.length();
		boolean end;
		do{
			end = true;
			for(final String substring : substrings){
				final int length = substring.length();
				if(input.regionMatches(endIndex - length, substring, 0, length)){
					endIndex -= length;
					end = false;
					break;
				}
			}
		}while(end == false);
		return input.substring(0, endIndex);
	}
	public static final String stripEndIgnoreCase(final String input, final String... substrings){
		final String tmp = input.toLowerCase();
		final String[] tmpSubstrings = new String[substrings.length];
		for(int i = 0; i < substrings.length; i++){
			tmpSubstrings[i] = substrings[i].toLowerCase();
		}
		int endIndex = input.length();
		boolean end;
		do{
			end = true;
			for(final String substring : tmpSubstrings){
				final int length = substring.length();
				if(tmp.regionMatches(endIndex - length, substring, 0, length)){
					endIndex -= length;
					end = false;
					break;
				}
			}
		}while(end == false);
		return input.substring(0, endIndex);
	}
	public static final String[] splitArgs(final String input){
		final String trimmed = input.trim();
		boolean started = false;
		Character quote = null;
		int i = 0;
		int startIndex = 0;
		final List<String> args = new ArrayList<String>();
		for(final char c : trimmed.toCharArray()){
			if(c <= 32){
				if(started){
					if(quote == null){
						args.add(trimmed.substring(startIndex, i));
						started = false;
					}
				}
			}else if(c == '\'' || c == '\"'){
				if(started){
					if(quote != null && quote.charValue() == c){
						args.add(trimmed.substring(startIndex, i));
						started = false;
						quote = null;
					}
				}else{
					quote = c;
					started = true;
					startIndex = i + 1;
				}
			}else if(started == false){
				started = true;
				startIndex = i;
			}
			i++;
		}
		if(started){
			args.add(trimmed.substring(startIndex));
		}
		return args.toArray(new String[args.size()]);
	}
	public static final int[] toCodePointArray(final String input){
		final int codePointCount = input.codePointCount(0, input.length());
		final int length = input.length();
		final int[] ary = new int[codePointCount];
		if(codePointCount == length){
			for(int i = 0; i < length; i++){
				ary[i] = input.charAt(i);
			}
			return ary;
		}else{
			for(int i = 0, cp, j = 0; i < length; i += Character.charCount(cp), j++){
				cp = input.codePointAt(i);
				ary[j] = cp;
			}
		}
		return ary;
	}
	/**
	 * Get display length of a string. Tab characters have length of 4. Carriage return and line feed characters have
	 * length of 0. Non-printable ASCII characters have length of 0. Ideographic characters have length of 2. Other
	 * printable ASCII characters have length of 1.
	 * 
	 * @param input
	 * @param flat
	 *            if <code>true</code>, return the display length of whole <tt>input</tt>, if <code>false</code>, return
	 *            the maximum display length of lines which is split from <tt>input</tt>.
	 * @return display length of <tt>input</tt>, or 0 if <tt>input</tt> is <code>null</code>.
	 */
	public static final int displayLengthOf(final String input, final boolean flat){
		if(input == null){
			return 0;
		}
		if(flat){
			return displayLengthOf(input);
		}else{
			final String[] lines = toLines(input);
			int max = 0;
			for(final String line : lines){
				final int i = displayLengthOf(line);
				if(max < i){
					max = i;
				}
			}
			return max;
		}
	}
	private static final int displayLengthOf(final String input){
		int length = 0;
		for(final int cp : toCodePointArray(input)){
			length += displayLengthOf(cp);
		}
		return length;
	}
	private static final int displayLengthOf(final int cp){
		return (Character.isIdeographic(cp) ? 2 : (cp > 31 ? 1 : (cp == '\t' ? 4 : 0)));
	}
	/**
	 * Split <tt>input</tt> into multiple lines.
	 * 
	 * @param input
	 * @return the lines, or an string array of length 0 if <tt>input</tt> is null.
	 */
	public static final String[] toLines(final String input){
		if(input == null){
			return new String[0];
		}
		return input.replace("\r\n", "\n").replace('\r', '\n').split("\n");
	}
	public static final String pad(final String p, final int displayLength, final boolean rightPad){
		return (rightPad ? rightPad(p, displayLength) : leftPad(p, displayLength));
	}
	public static final String pad(final String p, final int displayLength, final String padding, final boolean rightPad){
		return (rightPad ? rightPad(p, displayLength, padding) : leftPad(p, displayLength, padding));
	}
	public static final String leftPad(final String p, final int displayLength){
		return leftPad(p, displayLength, " ");
	}
	public static final String leftPad(final String p, final int displayLength, final String padding){
		final int paddingLength = displayLengthOf(padding);
		int remainingLength = displayLength - displayLengthOf(p);
		final StringBuilder sb = new StringBuilder(displayLength);
		while(remainingLength > 0){
			remainingLength -= paddingLength;
			sb.append(padding);
		}
		return sb.append(p).toString();
	}
	public static final String rightPad(final String p, final int displayLength){
		return rightPad(p, displayLength, " ");
	}
	public static final String rightPad(final String p, final int displayLength, final String padding){
		final int paddingLength = displayLengthOf(padding);
		int remainingLength = displayLength - displayLengthOf(p);
		final StringBuilder sb = new StringBuilder(displayLength);
		sb.append(p);
		while(remainingLength > 0){
			remainingLength -= paddingLength;
			sb.append(padding);
		}
		return sb.toString();
	}
	/**
	 * Format <tt>input</tt> into a displayable format. <code>null</code> is represented as an empty string.
	 * 
	 * @param input
	 * @param flat
	 *            if <code>false</code>, the <tt>input</tt> is split into multiple lines.
	 * @return
	 */
	public static final String[] display(final String input, final boolean flat){
		if(input == null){
			return new String[]{""};
		}
		if(flat){
			return new String[]{input.replace("\r", "").replace("\n", "").replace("\t", "    ")};
		}else{
			return toLines(input.replace("\t", "    "));
		}
	}
	public static final String[] display(final String input, final int wrappedDisplayLength, final String lineSeparatorReplacement, final boolean alignLeft){
		if(input == null){
			return new String[]{StringUtils.rightPad("", wrappedDisplayLength, ' ')};
		}
		final String[] lines = toLines(input.replace("\r\n", "\n").replace('\r', '\n').replace("\n", lineSeparatorReplacement));
		final List<String> wrappedLines = new ArrayList<String>();
		for(final String line : lines){
			if(line.length() == 0){
				wrappedLines.add(pad("", wrappedDisplayLength, " ", alignLeft));
			}else{
				int length = 0;
				final StringBuilder sb = new StringBuilder(wrappedDisplayLength);
				for(final int cp : toCodePointArray(line)){
					final int tmp = displayLengthOf(cp);
					length += tmp;
					if(length > wrappedDisplayLength){
						wrappedLines.add(pad(sb.toString(), wrappedDisplayLength, " ", alignLeft));
						sb.setLength(0);
						length = tmp;
					}
					sb.append(Character.toChars(cp));
				}
				if(sb.length() > 0){
					wrappedLines.add(pad(sb.toString(), wrappedDisplayLength, " ", alignLeft));
				}
			}
		}
		return wrappedLines.toArray(new String[wrappedLines.size()]);
	}
	/**
	 * Get traditional string from a string.
	 * 
	 * @param cs
	 * @return
	 */
	public static final String[] toTrad(final CharSequence cs){
		if(cs == null || cs.length() == 0){
			return new String[0];
		}
		final List<String> res = new ArrayList<String>();
		final char[][] chars = new char[cs.length()][];
		final int[] curIndex = new int[cs.length()];
		for(int i = 0; i < cs.length(); i++){
			final char ch = cs.charAt(i);
			chars[i] = simpToTradMap.get(ch);
			if(chars[i] == null){
				chars[i] = new char[]{ch};
			}
			curIndex[i] = 0;
		}
		boolean isEnded = false;
		final StringBuilder sb = new StringBuilder(cs.length());
		while(isEnded == false){
			for(int i = 0; i < cs.length(); i++){
				sb.append(chars[i][curIndex[i]]);
			}
			res.add(sb.toString());
			sb.setLength(0);
			// next permutation
			for(int i = 0; i < cs.length();){
				curIndex[i]++;
				if(curIndex[i] >= chars[i].length){
					curIndex[i] = 0;
				}else{
					break;
				}
				i++;
				if(i >= cs.length()){
					isEnded = true;
				}
			}
		}
		return res.toArray(new String[res.size()]);
	}
	/**
	 * Get traditional string from a string.
	 * 
	 * @param cs
	 * @return
	 */
	public static final String toBasicTrad(final CharSequence cs){
		if(cs == null || cs.length() == 0){
			return "";
		}
		final StringBuilder sb = new StringBuilder(cs.length());
		for(int i = 0; i < cs.length(); i++){
			final char ch = cs.charAt(i);
			final char[] chars = simpToTradMap.get(ch);
			if(chars == null){
				sb.append(ch);
			}else{
				sb.append(chars[0]);
			}
		}
		return sb.toString();
	}
	/**
	 * Get simplified string from a string.
	 * 
	 * @param cs
	 * @return
	 */
	public static final String toSimp(final CharSequence cs){
		if(cs == null){
			return "";
		}
		final StringBuilder sb = new StringBuilder(cs.length());
		for(final char c : cs.toString().toCharArray()){
			final Character ch = tradToSimpMap.get(c);
			sb.append((ch == null ? c : ch));
		}
		return sb.toString();
	}
	/**
	 * Determine whether <code>input</code> contains <code>find</code>.
	 * 
	 * @param input
	 * @param find
	 * @param ignoreCase
	 * @return
	 */
	public static final boolean isSimpContains(final String input, final String find, final boolean ignoreCase){
		if(input == null || input.length() == 0 || find == null || find.length() == 0){
			return false;
		}
		if(ignoreCase){
			return toSimp(input).toLowerCase().contains(toSimp(find).toLowerCase());
		}
		return toSimp(input).contains(toSimp(find));
	}
	/**
	 * Determine type of a string.
	 * 
	 * @param input
	 * @return
	 */
	public static final StringType typeOfString(final String input){
		if(input == null || input.length() == 0){
			return StringType.EMPTY;
		}
		int tradCount = 0;
		int simpCount = 0;
		int bothCount = 0;
		int engCount = 0;
		int numCount = 0;
		for(final char c : input.toCharArray()){
			if(c >= 256){
				if(undeterminedSet.contains(c)){
					bothCount++;
				}else if(simpToTradMap.containsKey(c)){
					simpCount++;
				}else if(tradToSimpMap.containsKey(c)){
					tradCount++;
				}else{
					bothCount++;
				}
			}else if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')){
				engCount++;
			}else if(c >= '0' && c <= '9'){
				numCount++;
			}
		}
		final int chiCount = tradCount + simpCount + bothCount;
		if(engCount > 0 && numCount == 0 && chiCount == 0){
			return StringType.ENGLISH;
		}else if(engCount == 0 && numCount > 0 && chiCount == 0){
			return StringType.NUMBER;
		}else if(engCount == 0 && numCount == 0 && chiCount > 0){
			if(bothCount > 0 && tradCount == 0 && simpCount == 0){
				return StringType.CHINESE;
			}else if(tradCount == 0 && simpCount > 0){
				return StringType.SIMPLIFIED_CHINESE;
			}else if(tradCount > 0 && simpCount == 0){
				return StringType.TRADITIONAL_CHINESE;
			}else{
				return StringType.MIXED_CHINESE;
			}
		}else{
			return StringType.UNKNOWN;
		}
	}
	public static final boolean isSimpEqual(final String p1, final String p2){
		if(p1 == null && p2 == null){
			return true;
		}else if(p1 == null || p2 == null){
			return false;
		}
		return toSimp(p1).equals(toSimp(p2));
	}
	public static final boolean isSimpEqualIgnoreCase(final String p1, final String p2){
		if(p1 == null && p2 == null){
			return true;
		}else if(p1 == null || p2 == null){
			return false;
		}
		return toSimp(p1).equalsIgnoreCase(toSimp(p2));
	}
	public static final String toString(final Object obj){
		return toString(obj, null, null);
	}
	public static final String toString(final Object obj, final Set<Class<?>> extraClasses, final Set<String> extraClassNames){
		final String cssPattern = "^com\\.css\\..+";
		final Set<Class<?>> classes = (extraClasses == null ? new HashSet<Class<?>>() : extraClasses);
		final Pattern[] patterns;
		if(extraClassNames == null){
			patterns = new Pattern[]{Pattern.compile(cssPattern)};
		}else{
			int i;
			if(extraClassNames.contains(cssPattern)){
				patterns = new Pattern[extraClassNames.size()];
				i = 0;
			}else{
				patterns = new Pattern[extraClassNames.size() + 1];
				patterns[0] = Pattern.compile(cssPattern);
				i = 1;
			}
			for(final String p : extraClassNames){
				patterns[i++] = Pattern.compile(p);
			}
		}
		return toString(obj, new HashSet<Object>(), classes, patterns);
	}
	private static final String toString(final Object obj, final Set<Object> handled, final Set<Class<?>> extraClasses, final Pattern[] extraClassNames){
		try{
			if(obj == null){
				return "null" + lineSeparator;
			}
			final Class<?> objClass = obj.getClass();
			if(StringUtility.class.isAssignableFrom(objClass)){
				return StringUtility.class.getName() + lineSeparator;
			}else if(String.class.isAssignableFrom(objClass)){
				final String s = (String)obj;
				if(s.length() > 1 && s.endsWith(lineSeparator)){
					return "\"" + s.substring(0, s.length() - 1) + "\"" + lineSeparator;
				}else{
					return "\"" + s + "\"" + lineSeparator;
				}
			}else if(Boolean.class.isAssignableFrom(objClass)){
				return obj + lineSeparator;
			}else if(Character.class.isAssignableFrom(objClass)){
				return obj + lineSeparator;
			}else if(Byte.class.isAssignableFrom(objClass)){
				return obj + lineSeparator;
			}else if(Short.class.isAssignableFrom(objClass)){
				return obj + lineSeparator;
			}else if(Integer.class.isAssignableFrom(objClass)){
				return obj + lineSeparator;
			}else if(Long.class.isAssignableFrom(objClass)){
				return obj + lineSeparator;
			}else if(Float.class.isAssignableFrom(objClass)){
				return obj + lineSeparator;
			}else if(Double.class.isAssignableFrom(objClass)){
				return obj + lineSeparator;
			}else if(BigDecimal.class.isAssignableFrom(objClass)){
				return obj + lineSeparator;
			}else if(Date.class.isAssignableFrom(objClass)){
				return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS").format(obj) + " - " + ((Date)obj).getTime() + lineSeparator;
			}else{
				final StringBuilder sb = new StringBuilder(1024);
				if(objClass.isArray()){
					final Class<?> c = objClass.getComponentType();
					if(c.isPrimitive()){
						if(Boolean.TYPE.equals(c)){
							final boolean[] ary = (boolean[])obj;
							sb.append("boolean array with length [").append(ary.length).append("]:").append(lineSeparator);
							for(int i = 0; i < ary.length; i++){
								sb.append(i).append("\t").append(ary[i]).append(lineSeparator);
							}
						}else if(Character.TYPE.equals(c)){
							final char[] ary = (char[])obj;
							sb.append("char array with length [").append(ary.length).append("]:").append(lineSeparator);
							for(int i = 0; i < ary.length; i++){
								sb.append(i).append("\t").append(ary[i]).append(lineSeparator);
							}
						}else if(Byte.TYPE.equals(c)){
							final byte[] ary = (byte[])obj;
							sb.append("byte array with length [").append(ary.length).append("]:").append(lineSeparator);
							for(int i = 0; i < ary.length; i++){
								sb.append(i).append("\t").append(ary[i]).append(lineSeparator);
							}
						}else if(Short.TYPE.equals(c)){
							final short[] ary = (short[])obj;
							sb.append("short array with length [").append(ary.length).append("]:").append(lineSeparator);
							for(int i = 0; i < ary.length; i++){
								sb.append(i).append("\t").append(ary[i]).append(lineSeparator);
							}
						}else if(Integer.TYPE.equals(c)){
							final int[] ary = (int[])obj;
							sb.append("int array with length [").append(ary.length).append("]:").append(lineSeparator);
							for(int i = 0; i < ary.length; i++){
								sb.append(i).append("\t").append(ary[i]).append(lineSeparator);
							}
						}else if(Long.TYPE.equals(c)){
							final long[] ary = (long[])obj;
							sb.append("long array with length [").append(ary.length).append("]:").append(lineSeparator);
							for(int i = 0; i < ary.length; i++){
								sb.append(i).append("\t").append(ary[i]).append(lineSeparator);
							}
						}else if(Float.TYPE.equals(c)){
							final float[] ary = (float[])obj;
							sb.append("float array with length [").append(ary.length).append("]:").append(lineSeparator);
							for(int i = 0; i < ary.length; i++){
								sb.append(i).append("\t").append(ary[i]).append(lineSeparator);
							}
						}else if(Double.TYPE.equals(c)){
							final double[] ary = (double[])obj;
							sb.append("double array with length [").append(ary.length).append("]:").append(lineSeparator);
							for(int i = 0; i < ary.length; i++){
								sb.append(i).append("\t").append(ary[i]).append(lineSeparator);
							}
						}
					}else{
						final Object[] ary = (Object[])obj;
						sb.append(c).append(" array with length [").append(ary.length).append("]:").append(lineSeparator);
						for(int i = 0; i < ary.length; i++){
							sb.append(i).append("\t").append(toString(ary[i], handled, extraClasses, extraClassNames));
						}
					}
				}else if(Iterable.class.isAssignableFrom(objClass)){
					final Iterable<?> itr = (Iterable<?>)obj;
					sb.append(itr.getClass().getName());
					final StringBuilder sb2 = new StringBuilder(1024);
					int i = 0;
					Class<?> componentType = null;
					for(final Object element : itr){
						sb2.append(i++).append("\t").append(toString(element, handled, extraClasses, extraClassNames));
						if(element != null){
							if(componentType == null){
								componentType = element.getClass();
							}else{
								final Class<?> c = element.getClass();
								while(componentType.equals(Object.class) == false && componentType.isAssignableFrom(c) == false){
									componentType = componentType.getSuperclass();
								}
							}
						}
					}
					sb.append("<").append(componentType == null ? "?" : componentType.getName()).append(">").append(" with length [").append(i).append("]:").append(lineSeparator);
					sb.append(sb2);
				}else if(Map.class.isAssignableFrom(objClass)){
					final Map<?, ?> map = (Map<?, ?>)obj;
					sb.append(map.getClass().getName());
					Class<?> keyType = null;
					Class<?> valueType = null;
					final StringBuilder sb2 = new StringBuilder(1024);
					for(final Object key : map.keySet()){
						final Object value = map.get(key);
						sb2.append("key:   ").append(toString(key, handled, extraClasses, extraClassNames));
						sb2.append("value: ").append(toString(value, handled, extraClasses, extraClassNames));
						if(key != null){
							if(keyType == null){
								keyType = key.getClass();
							}else{
								final Class<?> c = key.getClass();
								while(keyType.equals(Object.class) == false && keyType.isAssignableFrom(c) == false){
									keyType = keyType.getSuperclass();
								}
							}
						}
						if(value != null){
							if(valueType == null){
								valueType = value.getClass();
							}else{
								final Class<?> c = value.getClass();
								while(valueType.equals(Object.class) == false && valueType.isAssignableFrom(c) == false){
									valueType = valueType.getSuperclass();
								}
							}
						}
					}
					sb.append("<").append(keyType == null ? "?" : keyType.getName()).append(",").append(valueType == null ? "?" : valueType.getName()).append(">").append(" with size [").append(map.size()).append("]:").append(lineSeparator);
					sb.append(sb2);
				}else if(handled.contains(obj)){
					sb.append(obj.getClass().getName()).append(" [").append(System.identityHashCode(obj)).append("]").append(lineSeparator);
				}else{
					handled.add(obj);
					Method m = null;
					try{
						m = objClass.getDeclaredMethod("toString");
					}catch(final SecurityException e){
					}catch(final NoSuchMethodException e){
					}
					if(m != null && String.class.isAssignableFrom(m.getReturnType())){
						final String s = obj.toString();
						if(s.length() > 1 && s.endsWith(lineSeparator)){
							sb.append(s);
						}else{
							sb.append(s).append(lineSeparator);
						}
					}else{
						boolean inspect = false;
						for(final Class<?> c : extraClasses){
							if(c.isAssignableFrom(objClass)){
								inspect = true;
								break;
							}
						}
						if(inspect == false){
							final String className = objClass.getName();
							for(final Pattern p : extraClassNames){
								if(p.matcher(className).matches()){
									inspect = true;
									break;
								}
							}
						}
						if(inspect){
							final int typeMinLength = 32;
							final int nameMinLength = 32;
							Class<?> c = objClass;
							while(c != null && Object.class.equals(c) == false){
								sb.append(c.getName()).append(" object:").append(lineSeparator);
								final Field[] fields = c.getDeclaredFields();
								for(final Field field : fields){
									field.setAccessible(true);
									try{
										sb.append(StringUtils.rightPad(StringUtils.rightPad(field.getType().getName(), typeMinLength) + " " + field.getName(), typeMinLength + 1 + nameMinLength)).append(" = ").append(toString(field.get(obj), handled, extraClasses, extraClassNames));
									}catch(final IllegalArgumentException e){
										sb.append("IllegalArgumentException").append(lineSeparator);
									}catch(final IllegalAccessException e){
										sb.append("IllegalAccessException").append(lineSeparator);
									}
								}
								c = c.getSuperclass();
							}
						}else{
							sb.append(obj).append(lineSeparator);
						}
					}
				}
				return sb.toString();
			}
		}catch(final Exception e){
			return obj + " " + e.toString() + lineSeparator;
		}
	}
	/**
	 * Each element in the iterator is converted to string using <code>String.valueOf</code> and then concatenated via
	 * <tt>separator</tt>.
	 * 
	 * @param itr
	 * @param separator
	 * @param includeNullElement
	 *            if <code>true</code>, <code>null</code> elements are excluded
	 * @return
	 */
	public static final String iteratableToString(final Iterable<?> itr, final String separator, final boolean includeNullElement){
		final StringBuilder sb = new StringBuilder(1024);
		for(final Object element : itr){
			if(element != null || includeNullElement){
				sb.append(element).append(separator);
			}
		}
		if(sb.length() > separator.length()){
			sb.setLength(sb.length() - separator.length());
		}
		return sb.toString();
	}
	public static final String getStackTrace(final Throwable t){
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		pw.close();
		return sw.toString();
	}
	/**
	 * If <tt>obj</tt> is a <code>CharSequence</code>, return <code>obj.toString()</code>. If <tt>obj</tt> is null,
	 * return <tt>"null"</tt>. Otherwise, return
	 * <code>obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj))</code>.
	 * 
	 * @param obj
	 * @return
	 */
	public static final String identityToString(final Object obj){
		if(obj == null){
			return "null";
		}else if(obj instanceof CharSequence){
			return obj.toString();
		}
		return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
	}
	public static final String find(final String content, final String endSymbol, final int startIndex, final String... startSymbols){
		final int resultStartIndex = findBeginIndex(content, startIndex, startSymbols);
		if(resultStartIndex < 0){
			return null;
		}
		final int resultEndIndex = findEndIndex(content, endSymbol, resultStartIndex, startSymbols);
		if(resultEndIndex < 0){
			return null;
		}
		return content.substring(resultStartIndex, resultEndIndex);
	}
	public static final int findBeginIndex(final String content, final int startIndex, final String... startSymbols){
		if(startSymbols == null || startSymbols.length == 0){
			return startIndex;
		}else{
			int prevIndex = startIndex;
			for(final String symbol : startSymbols){
				final int tmp = content.indexOf(symbol, prevIndex);
				if(tmp < 0){
					return -1;
				}
				prevIndex = tmp + symbol.length();
			}
			return prevIndex;
		}
	}
	public static final int findEndIndex(final String content, final String endSymbol, final int startIndex, final String... startSymbols){
		if(endSymbol == null){
			return content.length();
		}else{
			return content.indexOf(endSymbol, startIndex);
		}
	}
	public static final String removeCSICode(final String s){
		final StringBuilder sb = new StringBuilder(s.length());
		final String startPattern = new String(new char[]{27, '['});
		final char[] chars = s.toCharArray();
		int startIndex = 0;
		int findIndex = -1;
		while(true){
			findIndex = s.indexOf(startPattern, startIndex);
			if(findIndex >= 0){
				sb.append(s.subSequence(startIndex, findIndex));
				for(int i = findIndex + startPattern.length(); i < s.length(); i++){
					final char c = chars[i];
					if((c < '0' || c > '9') && c != ';'){
						startIndex = i + 1;
						break;
					}
				}
			}else{
				sb.append(s.substring(startIndex));
				break;
			}
		}
		return sb.toString();
	}
	public static final String useLinuxLineSeparator(final String s){
		return s.replace(windowsLineSeparator, linuxLineSeparator).replace(macLineSeparator, linuxLineSeparator);
	}
	public static final String removeLineSeparator(final String s){
		return s.replace(linuxLineSeparator, "").replace(macLineSeparator, "");
	}
	/**
	 * Determine which of the <tt>candidates</tt> appears first in <tt>s</tt>.
	 * 
	 * @param s
	 * @param candidates
	 * @return
	 */
	public static final String appearFirst(final String s, final String... candidates){
		int index = s.length();
		String first = null;
		for(final String candidate : candidates){
			final int i = s.indexOf(candidate);
			if(i >= 0 && i < index){
				index = i;
				first = candidate;
			}
		}
		return first;
	}
	public static final String getFirstLine(final String s, final boolean eofAsEol, final boolean includeLineSeparator){
		final String lineSeparator = appearFirst(s, windowsLineSeparator, macLineSeparator, linuxLineSeparator);
		if(lineSeparator == null){
			if(eofAsEol){
				return s;
			}else{
				return null;
			}
		}else{
			if(includeLineSeparator){
				return s.substring(0, s.indexOf(lineSeparator) + lineSeparator.length());
			}else{
				return s.substring(0, s.indexOf(lineSeparator));
			}
		}
	}
	/**
	 * Get length of bytes which can be convert to a <code>String</code> of the specified <code>Charset</code>.
	 * 
	 * @param bytes
	 * @param from
	 * @param to
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static final int getReadableLength(final byte[] bytes, final int from, final int to, final Charset charset) throws UnsupportedEncodingException{
		final byte[] newBytes = new String(bytes, from, to - from, charset).getBytes(charset);
		int index = Math.min(to - from, newBytes.length) - 1;
		int origIndex = index + from;
		while(index >= 0 && bytes[origIndex] != newBytes[index]){
			index--;
			origIndex--;
		}
		if(index >= 0){
			return index + 1;
		}else{
			return 0;
		}
	}
	/**
	 * Determine if the bytes are content of the specified <code>Charset</code>.
	 * 
	 * @param bytes
	 * @param from
	 * @param to
	 * @param charset
	 * @return
	 */
	public static final boolean isOfCharset(final byte[] bytes, final int from, final int to, final Charset charset){
		final byte[] newBytes = new String(bytes, from, to - from, charset).getBytes(charset);
		if(newBytes.length != bytes.length){
			return false;
		}
		for(int i = bytes.length - 1; i >= 0; i--){
			if(bytes[i] != newBytes[i]){
				return false;
			}
		}
		return true;
	}
	/**
	 * Return the readable string if the bytes are content of UTF8, BIG5 or GBK. Otherwise return <code>null</code>.
	 * 
	 * @param bytes
	 * @param from
	 * @param to
	 * @return
	 */
	public static final String read(final byte[] bytes, final int from, final int to){
		for(final Charset charset : new Charset[]{UTF8, BIG5, GBK}){
			if(isOfCharset(bytes, from, to, charset)){
				return new String(bytes, from, to - from, charset);
			}
		}
		return null;
	}
}
