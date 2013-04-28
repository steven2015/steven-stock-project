/**
 * 
 */
package steven.stock;

import java.io.IOException;
import java.text.ParseException;

import com.css.system.utility.DateUtility;
import com.css.system.utility.MathUtility;
import com.css.system.vo.PeakRegion;

/**
 * @author Steven
 * 
 */
public class Main{
	public static void main(final String[] args) throws IOException, ParseException{
		final StockData data = new StockData(116);
		for(int i = 0; i < 10; i++){
			System.out.println(DateUtility.dateFormat.format(data.getDates()[i]) + "\t" + data.getCenterRsis()[i] + "\t" + data.getCenterPrices()[i]);
		}
		final PeakRegion[] regions = MathUtility.getPeaks(data.getCenterPrices(), 10);
		final StringBuilder sb = new StringBuilder(4096);
		for(int i = 0; i < regions.length && i < 10; i++){
			final PeakRegion region = regions[i];
			final int startIndex = region.getStartIndex();
			final int peakIndex = region.getPeakIndex();
			final int endIndex = region.getEndIndex();
			sb.setLength(0);
			sb.append("(up:").append(endIndex - peakIndex).append(",down:").append(peakIndex - startIndex).append(",total:").append(endIndex - startIndex + 1).append("\t");
			sb.append(DateUtility.dateFormat.format(data.getDates()[startIndex])).append(" : ").append(data.getCenterPrices()[startIndex]).append("\t- ");
			sb.append(DateUtility.dateFormat.format(data.getDates()[peakIndex])).append(" : ").append(data.getCenterPrices()[peakIndex]).append("\t- ");
			sb.append(DateUtility.dateFormat.format(data.getDates()[endIndex])).append(" : ").append(data.getCenterPrices()[endIndex]);
			System.out.println(sb.toString());
		}
	}
}
