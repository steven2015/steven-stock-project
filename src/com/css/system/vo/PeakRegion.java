/**
 * 
 */
package com.css.system.vo;

import com.css.system.annotation.Immutable;

/**
 * @author Steven
 * 
 */
@Immutable
public class PeakRegion{
	private final int peakIndex;
	private final int startIndex;
	private final int endIndex;

	public PeakRegion(final int peakIndex, final int startIndex, final int endIndex){
		this.peakIndex = peakIndex;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}
	public final int getPeakIndex(){
		return this.peakIndex;
	}
	public final int getStartIndex(){
		return this.startIndex;
	}
	public final int getEndIndex(){
		return this.endIndex;
	}
}
