/**
 * 
 */
package com.css.system.utility;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.css.system.annotation.Static;
import com.css.system.vo.PeakRegion;

/**
 * @author steven.lam.t.f
 * 
 */
@Static
public class MathUtility{
	private static final byte[] INTERNAL_HEX_TABLE = new byte[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	private MathUtility(){
	}
	public static final <T>PeakRegion[] getPeaks(final List<?> values, final int minIndexDiff, final Comparator<T> c, final Field f) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return getPeaks(values.toArray(), minIndexDiff, c, f);
	}
	@SuppressWarnings("unchecked")
	public static final <T>PeakRegion[] getPeaks(final Object[] values, final int minIndexDiff, final Comparator<T> c, final Field f) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		f.setAccessible(true);
		final IndexedValue<T>[] ivs = new IndexedValue[values.length];
		for(int i = 0; i < values.length; i++){
			ivs[i] = new IndexedValue<T>(i, (T)f.get(values[i]));
		}
		return getPeaks(ivs, minIndexDiff, c);
	}
	public static final <T>PeakRegion[] getPeaks(final List<?> values, final int minIndexDiff, final Comparator<T> c, final Method m) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return getPeaks(values.toArray(), minIndexDiff, c, m);
	}
	@SuppressWarnings("unchecked")
	public static final <T>PeakRegion[] getPeaks(final Object[] values, final int minIndexDiff, final Comparator<T> c, final Method m) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		m.setAccessible(true);
		final IndexedValue<T>[] ivs = new IndexedValue[values.length];
		for(int i = 0; i < values.length; i++){
			ivs[i] = new IndexedValue<T>(i, (T)m.invoke(values[i], (Object[])null));
		}
		return getPeaks(ivs, minIndexDiff, c);
	}
	public static final <T>PeakRegion[] getPeaks(final T[] values, final int minIndexDiff, final Comparator<T> c){
		@SuppressWarnings("unchecked")
		final IndexedValue<T>[] ivs = new IndexedValue[values.length];
		for(int i = 0; i < values.length; i++){
			ivs[i] = new IndexedValue<T>(i, values[i]);
		}
		return getPeaks(ivs, minIndexDiff, c);
	}
	private static final <T>PeakRegion[] getPeaks(final IndexedValue<T>[] ivs, final int minIndexDiff, final Comparator<T> c){
		Arrays.sort(ivs, 0, ivs.length, new Comparator<IndexedValue<T>>(){
			@Override
			public int compare(final IndexedValue<T> o1, final IndexedValue<T> o2){
				return c.compare(o1.value, o2.value);
			}
		});
		final List<TempPeakRegion> peaks = new ArrayList<>();
		final int halfMinIndexDiff = minIndexDiff >> 1;
		for(final IndexedValue<T> iv : ivs){
			final int index = iv.index;
			boolean found = false;
			for(final TempPeakRegion peak : peaks){
				if(peak.startIndex < index && index < peak.endIndex){
					found = true;
					break;
				}else if(Math.abs(peak.peakIndex - index) <= halfMinIndexDiff){
					if(index < peak.startIndex){
						peak.startIndex = index;
					}else if(index > peak.endIndex){
						peak.endIndex = index;
					}
					found = true;
					break;
				}
			}
			if(found == false){
				TempPeakRegion tpk = null;
				for(final TempPeakRegion peak : peaks){
					if(peak.startIndex < index + minIndexDiff && index < peak.endIndex + minIndexDiff){
						if(tpk == null){
							tpk = peak;
						}else{
							if(tpk.peakIndex < peak.peakIndex){
								if(index - tpk.endIndex < peak.startIndex - index){
									tpk.endIndex = index;
								}else{
									peak.startIndex = index;
								}
							}else{
								if(index - peak.endIndex < tpk.startIndex - index){
									peak.endIndex = index;
								}else{
									tpk.startIndex = index;
								}
							}
							found = true;
							if(peak.startIndex > peak.peakIndex){
								System.out.println("oh1");
							}
							if(tpk.startIndex > tpk.peakIndex){
								System.out.println("oh2");
							}
							break;
						}
					}
				}
				if(found == false){
					if(tpk == null){
						peaks.add(new TempPeakRegion(index));
					}else{
						if(index < tpk.startIndex){
							tpk.startIndex = index;
						}else if(index > tpk.endIndex){
							tpk.endIndex = index;
						}
					}
				}
			}
		}
		Collections.sort(peaks);
		final PeakRegion[] regions = new PeakRegion[peaks.size()];
		for(int i = 0; i < regions.length; i++){
			final TempPeakRegion tpk = peaks.get(i);
			regions[i] = new PeakRegion(tpk.peakIndex, tpk.startIndex, tpk.endIndex);
		}
		return regions;
	}
	public static final int[] excludeZero(final int[] values){
		final int[] copy = new int[values.length];
		int nextCopyIndex = 0;
		for(final int value : values){
			if(value != 0){
				copy[nextCopyIndex] = value;
				nextCopyIndex++;
			}
		}
		return Arrays.copyOf(copy, nextCopyIndex);
	}
	public static final double[] excludeZero(final double[] values){
		final double[] copy = new double[values.length];
		int nextCopyIndex = 0;
		for(final double value : values){
			if(value != 0){
				copy[nextCopyIndex] = value;
				nextCopyIndex++;
			}
		}
		return Arrays.copyOf(copy, nextCopyIndex);
	}
	public static final <T extends Number>T[] excludeZero(final T[] values, final Class<T> clazz){
		@SuppressWarnings("unchecked")
		final T[] copy = (T[])Array.newInstance(clazz, values.length);
		int nextCopyIndex = 0;
		for(final T value : values){
			if(value.doubleValue() != 0){
				copy[nextCopyIndex] = value;
				nextCopyIndex++;
			}
		}
		return Arrays.copyOf(copy, nextCopyIndex);
	}
	public static final int getMedian(final int[] values, final boolean excludeZero){
		final int[] copy = (excludeZero ? excludeZero(values) : values.clone());
		Arrays.sort(copy);
		return copy[copy.length / 2];
	}
	public static final double getMedian(final double[] values, final boolean excludeZero){
		final double[] copy = (excludeZero ? excludeZero(values) : values.clone());
		Arrays.sort(copy);
		return copy[copy.length / 2];
	}
	public static final <T extends Number>T getMedian(final T[] values, final Class<T> clazz, final boolean excludeZero){
		return getMedian((excludeZero ? excludeZero(values, clazz) : values.clone()), new Comparator<T>(){
			@Override
			public int compare(final T o1, final T o2){
				final double d1 = o1.doubleValue();
				final double d2 = o2.doubleValue();
				if(d1 > d2){
					return 1;
				}else if(d1 < d2){
					return -1;
				}
				return 0;
			}
		});
	}
	public static final <T>T getMedian(final T[] values, final Comparator<T> c){
		final T[] copy = values.clone();
		Arrays.sort(copy, c);
		return copy[copy.length / 2];
	}
	public static final double getVariance(final int[] values, final boolean excludeZero){
		final double mean = getMean(values, excludeZero);
		return getMeanOfSquare(values, excludeZero) - mean * mean;
	}
	public static final double getVariance(final double[] values, final boolean excludeZero){
		final double mean = getMean(values, excludeZero);
		return getMeanOfSquare(values, excludeZero) - mean * mean;
	}
	public static final double getVariance(final Number[] values, final boolean excludeZero){
		final double mean = getMean(values, excludeZero);
		return getMeanOfSquare(values, excludeZero) - mean * mean;
	}
	public static final double getMean(final int[] values, final boolean excludeZero){
		double mean = 0;
		double count = 0;
		int nextCount = 1;
		if(excludeZero){
			for(final int value : values){
				if(value != 0){
					mean = mean * (count / nextCount) + value / nextCount;
					count++;
					nextCount++;
				}
			}
		}else{
			for(final int value : values){
				mean = mean * (count / nextCount) + value / nextCount;
				count++;
				nextCount++;
			}
		}
		return mean;
	}
	public static final double getMean(final double[] values, final boolean excludeZero){
		double mean = 0;
		double count = 0;
		int nextCount = 1;
		if(excludeZero){
			for(final double value : values){
				if(value != 0){
					mean = mean * (count / nextCount) + value / nextCount;
					count++;
					nextCount++;
				}
			}
		}else{
			for(final double value : values){
				mean = mean * (count / nextCount) + value / nextCount;
				count++;
				nextCount++;
			}
		}
		return mean;
	}
	public static final double getMean(final Number[] values, final boolean excludeZero){
		double mean = 0;
		double count = 0;
		int nextCount = 1;
		if(excludeZero){
			for(final Number value : values){
				final double v = value.doubleValue();
				if(v != 0){
					mean = mean * (count / nextCount) + v / nextCount;
					count++;
					nextCount++;
				}
			}
		}else{
			for(final Number value : values){
				mean = mean * (count / nextCount) + value.doubleValue() / nextCount;
				count++;
				nextCount++;
			}
		}
		return mean;
	}
	public static final double getMeanOfSquare(final int[] values, final boolean excludeZero){
		double mean = 0;
		double count = 0;
		int nextCount = 1;
		if(excludeZero){
			for(final int value : values){
				if(value != 0){
					mean = mean * (count / nextCount) + value * value / nextCount;
					count++;
					nextCount++;
				}
			}
		}else{
			for(final int value : values){
				mean = mean * (count / nextCount) + value * value / nextCount;
				count++;
				nextCount++;
			}
		}
		return mean;
	}
	public static final double getMeanOfSquare(final double[] values, final boolean excludeZero){
		double mean = 0;
		double count = 0;
		int nextCount = 1;
		if(excludeZero){
			for(final double value : values){
				if(value != 0){
					mean = mean * (count / nextCount) + value * value / nextCount;
					count++;
					nextCount++;
				}
			}
		}else{
			for(final double value : values){
				mean = mean * (count / nextCount) + value * value / nextCount;
				count++;
				nextCount++;
			}
		}
		return mean;
	}
	public static final double getMeanOfSquare(final Number[] values, final boolean excludeZero){
		double mean = 0;
		double count = 0;
		int nextCount = 1;
		if(excludeZero){
			for(final Number value : values){
				final double v = value.doubleValue();
				if(v != 0){
					mean = mean * (count / nextCount) + v * v / nextCount;
					count++;
					nextCount++;
				}
			}
		}else{
			for(final Number value : values){
				final double v = value.doubleValue();
				mean = mean * (count / nextCount) + v * v / nextCount;
				count++;
				nextCount++;
			}
		}
		return mean;
	}
	/* break line */
	public static final int max(final int... values){
		return max(values, 0, values.length);
	}
	public static final int max(final int[] values, final int offset, final int length){
		int max = values[offset];
		for(int i = 1, j = offset + 1; i < length; i++, j++){
			final int value = values[j];
			if(value > max){
				max = value;
			}
		}
		return max;
	}
	public static final double max(final double... values){
		return max(values, 0, values.length);
	}
	public static final double max(final double[] values, final int offset, final int length){
		double max = values[offset];
		for(int i = 1, j = offset + 1; i < length; i++, j++){
			final double value = values[j];
			if(value > max){
				max = value;
			}
		}
		return max;
	}
	@SuppressWarnings("unchecked")
	public static final <T extends Comparable<T>>T max(final T... values){
		return max(values, 0, values.length);
	}
	public static final <T extends Comparable<T>>T max(final T[] values, final int offset, final int length){
		T max = values[offset];
		for(int i = 1, j = offset + 1; i < length; i++, j++){
			final T value = values[j];
			if(value.compareTo(max) > 0){
				max = value;
			}
		}
		return max;
	}
	public static final int min(final int... values){
		return min(values, 0, values.length);
	}
	public static final int min(final int[] values, final int offset, final int length){
		int min = values[offset];
		for(int i = 1, j = offset + 1; i < length; i++, j++){
			final int value = values[j];
			if(value < min){
				min = value;
			}
		}
		return min;
	}
	public static final double min(final double... values){
		return min(values, 0, values.length);
	}
	public static final double min(final double[] values, final int offset, final int length){
		double min = values[offset];
		for(int i = 1, j = offset + 1; i < length; i++, j++){
			final double value = values[j];
			if(value < min){
				min = value;
			}
		}
		return min;
	}
	@SuppressWarnings("unchecked")
	public static final <T extends Comparable<T>>T min(final T... values){
		return min(values, 0, values.length);
	}
	public static final <T extends Comparable<T>>T min(final T[] values, final int offset, final int length){
		T min = values[offset];
		for(int i = 1, j = offset + 1; i < values.length; i++, j++){
			final T value = values[j];
			if(value.compareTo(min) < 0){
				min = value;
			}
		}
		return min;
	}
	/**
	 * Calculate EMA.
	 * 
	 * @param values
	 *            newest values at index 0.
	 * @param alpha
	 * @return
	 */
	public static final double[] getExponentialMovingAverage(final double[] values, final double alpha){
		final double invertedAlpha = 1 - alpha;
		final double[] ema = new double[values.length];
		final int startIndex = values.length - 1;
		double prevEma = values[startIndex];
		ema[startIndex] = prevEma;
		for(int i = startIndex - 1; i >= 0; i--){
			prevEma = alpha * values[i] + invertedAlpha * prevEma;
			ema[i] = prevEma;
		}
		return ema;
	}
	/**
	 * Calculate EMA.
	 * 
	 * @param values
	 *            newest values at index 0.
	 * @param alpha
	 * @return
	 */
	public static final <T extends Number>double[] getExponentialMovingAverage(final T[] values, final double alpha){
		final double invertedAlpha = 1 - alpha;
		final double[] ema = new double[values.length];
		final int startIndex = values.length - 1;
		double prevEma = values[startIndex].doubleValue();
		ema[startIndex] = prevEma;
		for(int i = startIndex - 1; i >= 0; i--){
			prevEma = alpha * values[i].doubleValue() + invertedAlpha * prevEma;
			ema[i] = prevEma;
		}
		return ema;
	}
	public static final String byteToHex(final byte[] bytes){
		final byte[] tmp = new byte[bytes.length * 2];
		int i = 0;
		for(final byte b : bytes){
			final int value = b & 0xff;
			tmp[i++] = INTERNAL_HEX_TABLE[value >>> 4];
			tmp[i++] = INTERNAL_HEX_TABLE[value & 0xf];
		}
		return new String(tmp);
	}
	public static final int byteToUnsignedByte(final byte b){
		return b & 0xff;
	}
	public static final int byteToSignedShort(final boolean bigEndian, final byte b1, final byte b2){
		if(bigEndian){
			return (short)((b1 & 0xff) << 8) | (b2 & 0xff);
		}else{
			return (short)((b2 & 0xff) << 8) | (b1 & 0xff);
		}
	}
	public static final int byteToSignedShort(final boolean bigEndian, final byte[] bytes, final int offset){
		return byteToSignedShort(bigEndian, bytes[offset], bytes[offset + 1]);
	}
	public static final int byteToUnSignedShort(final boolean bigEndian, final byte b1, final byte b2){
		if(bigEndian){
			return ((b1 & 0xff) << 8) | (b2 & 0xff);
		}else{
			return ((b2 & 0xff) << 8) | (b1 & 0xff);
		}
	}
	public static final int byteToUnSignedShort(final boolean bigEndian, final byte[] bytes, final int offset){
		return byteToUnSignedShort(bigEndian, bytes[offset], bytes[offset + 1]);
	}
	public static final int byteToUnsignedSubInt(final boolean bigEndian, final byte b1, final byte b2, final byte b3){
		if(bigEndian){
			return ((b1 % 0xff) << 16) | ((b2 & 0xff) << 8) | (b3 & 0xff);
		}else{
			return ((b3 % 0xff) << 16) | ((b2 & 0xff) << 8) | (b1 & 0xff);
		}
	}
	public static final int byteToUnsignedSubInt(final boolean bigEndian, final byte[] bytes, final int offset){
		return byteToUnsignedSubInt(bigEndian, bytes[offset], bytes[offset + 1], bytes[offset + 2]);
	}
	public static final int byteToSignedInt(final boolean bigEndian, final byte b1, final byte b2, final byte b3, final byte b4){
		if(bigEndian){
			return ((b1 % 0xff) << 24) | ((b2 % 0xff) << 16) | ((b3 & 0xff) << 8) | (b4 & 0xff);
		}else{
			return ((b4 % 0xff) << 24) | ((b3 % 0xff) << 16) | ((b2 & 0xff) << 8) | (b1 & 0xff);
		}
	}
	public static final int byteToSignedInt(final boolean bigEndian, final byte[] bytes, final int offset){
		return byteToSignedInt(bigEndian, bytes[offset], bytes[offset + 1], bytes[offset + 2], bytes[offset + 3]);
	}
	public static final long byteToSignedLong(final boolean bigEndian, final byte b1, final byte b2, final byte b3, final byte b4, final byte b5, final byte b6, final byte b7, final byte b8){
		if(bigEndian){
			return ((b1 % 0xffL) << 56) | ((b2 % 0xffL) << 48) | ((b3 & 0xffL) << 40) | ((b4 & 0xffL) << 32) | ((b5 & 0xffL) << 24) | ((b6 & 0xffL) << 16) | ((b7 & 0xffL) << 8) | (b8 & 0xffL);
		}else{
			return ((b8 % 0xffL) << 56) | ((b7 % 0xffL) << 48) | ((b6 & 0xffL) << 40) | ((b5 & 0xffL) << 32) | ((b4 & 0xffL) << 24) | ((b3 & 0xffL) << 16) | ((b2 & 0xffL) << 8) | (b1 & 0xffL);
		}
	}
	public static final long byteToSignedLong(final boolean bigEndian, final byte[] bytes, final int offset){
		return byteToSignedLong(bigEndian, bytes[offset], bytes[offset + 1], bytes[offset + 2], bytes[offset + 3], bytes[offset + 4], bytes[offset + 5], bytes[offset + 6], bytes[offset + 7]);
	}

	private static class IndexedValue<T>{
		private final int index;
		private final T value;

		private IndexedValue(final int index, final T value){
			this.index = index;
			this.value = value;
		}
		@Override
		public String toString(){
			return this.index + ": " + this.value;
		}
	}
	private static class TempPeakRegion implements Comparable<TempPeakRegion>{
		private final int peakIndex;
		private int startIndex;
		private int endIndex;

		private TempPeakRegion(final int peakIndex){
			this.peakIndex = peakIndex;
			this.startIndex = peakIndex;
			this.endIndex = peakIndex;
		}
		@Override
		public int compareTo(final TempPeakRegion o){
			if(this.peakIndex < o.peakIndex){
				return -1;
			}else if(this.peakIndex > o.peakIndex){
				return 1;
			}
			return 0;
		}
	}
}
