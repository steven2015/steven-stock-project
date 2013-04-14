/**
 * 
 */
package steven.stock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.css.system.utility.DateUtility;
import com.css.system.utility.MathUtility;

/**
 * @author Steven
 * 
 */
public class StockData{
	private static final BigDecimal INTERNAL_OUTER_FACTOR = new BigDecimal("0.2");
	private static final BigDecimal INTERNAL_INNER_FACTOR = new BigDecimal("0.3");
	private static final double INTERNAL_ALPHA_10 = 2.0 / (10 + 1);
	private static final double INTERNAL_ALPHA_15 = 2.0 / (15 + 1);
	private static final double INTERNAL_ALPHA_20 = 2.0 / (20 + 1);
	private static final double INTERNAL_ALPHA_50 = 2.0 / (50 + 1);
	private static final double INTERNAL_ALPHA_100 = 2.0 / (100 + 1);
	private static final double INTERNAL_ALPHA_250 = 2.0 / (250 + 1);
	private static final Date INTERNAL_START_DATE = DateUtility.getDate(1950, 1, 1);
	private static final Date INTERNAL_END_DATE = DateUtility.getDate(2050, 12, 31);
	private final int stockNumber;
	private final Date startDate;
	private final Date endDate;
	private final Date[] dates;
	private final BigDecimal[] openPrices;
	private final BigDecimal[] highPrices;
	private final BigDecimal[] lowPrices;
	private final BigDecimal[] closePrices;
	private final long[] volumes;
	private final int dayCount;
	// analysis
	private final double[] centerPrices;
	private final double[] priceFluctuations;
	private final double[] priceFluctuationRatios;
	private final double[] centerEma10s;
	private final double[] centerEma15s;
	private final double[] centerEma20s;
	private final double[] centerEma50s;
	private final double[] centerEma100s;
	private final double[] centerEma250s;
	private final double[] centerRsiUEmas;
	private final double[] centerRsiDEmas;
	private final double[] centerRsis;

	public StockData(final int stockNumber) throws IOException, ParseException{
		this(stockNumber, INTERNAL_START_DATE);
	}
	public StockData(final int stockNumber, final Date startDate) throws IOException, ParseException{
		this(stockNumber, startDate, INTERNAL_END_DATE);
	}
	public StockData(final int stockNumber, final Date startDate, final Date endDate) throws IOException, ParseException{
		this.stockNumber = stockNumber;
		this.startDate = startDate;
		this.endDate = endDate;
		final Calendar start = DateUtility.getCalendar(startDate);
		final Calendar end = DateUtility.getCalendar(endDate);
		final String urlString = "http://ichart.finance.yahoo.com/table.csv?s=" + StringUtils.leftPad(String.valueOf(stockNumber), 4, '0') + ".HK&a=" + (DateUtility.getMonth(start) - 1) + "&b=" + DateUtility.getDay(start) + "&c=" + DateUtility.getYear(start) + "&d=" + (DateUtility.getMonth(end) - 1) + "&e=" + DateUtility.getDay(end) + "&f=" + DateUtility.getYear(end) + "&g=d&ignore=.csv";
		final List<String> rawDatas = new ArrayList<>();
		final URL url = new URL(urlString);
		try(final InputStream is = url.openStream(); final InputStreamReader isr = new InputStreamReader(is); final BufferedReader br = new BufferedReader(isr);){
			String line = null;
			line = br.readLine();
			while((line = br.readLine()) != null){
				final String[] cols = line.split(",");
				final long volume = Long.parseLong(cols[5]);
				if(volume > 0){
					rawDatas.add(line);
				}
			}
		}
		this.dayCount = rawDatas.size();
		this.dates = new Date[rawDatas.size()];
		this.openPrices = new BigDecimal[rawDatas.size()];
		this.highPrices = new BigDecimal[rawDatas.size()];
		this.lowPrices = new BigDecimal[rawDatas.size()];
		this.closePrices = new BigDecimal[rawDatas.size()];
		this.volumes = new long[rawDatas.size()];
		this.centerPrices = new double[rawDatas.size()];
		this.priceFluctuations = new double[rawDatas.size()];
		this.priceFluctuationRatios = new double[rawDatas.size()];
		for(int i = 0; i < rawDatas.size(); i++){
			final String rawData = rawDatas.get(i);
			final String[] cols = rawData.split(",");
			final Date date = new SimpleDateFormat("yyyy-MM-dd").parse(cols[0]);
			final BigDecimal openPrice = new BigDecimal(cols[1]);
			final BigDecimal highPrice = new BigDecimal(cols[2]);
			final BigDecimal lowPrice = new BigDecimal(cols[3]);
			final BigDecimal closePrice = new BigDecimal(cols[6]);
			final long volume = Long.parseLong(cols[5]);
			final BigDecimal adjustedClosePrice = new BigDecimal(cols[6]);
			this.dates[i] = date;
			this.openPrices[i] = openPrice;
			final BigDecimal highestPrice = MathUtility.max(openPrice, highPrice, lowPrice, closePrice, adjustedClosePrice);
			this.highPrices[i] = highestPrice;
			final BigDecimal lowestPrice = MathUtility.min(openPrice, highPrice, lowPrice, closePrice, adjustedClosePrice);
			this.lowPrices[i] = lowestPrice;
			this.closePrices[i] = adjustedClosePrice;
			this.volumes[i] = volume;
			final BigDecimal centerPrice = ((highestPrice.add(lowestPrice)).multiply(INTERNAL_OUTER_FACTOR)).add((openPrice.add(closePrice)).multiply(INTERNAL_INNER_FACTOR));
			this.centerPrices[i] = centerPrice.doubleValue();
			final BigDecimal priceFluctuation = highestPrice.subtract(lowestPrice);
			this.priceFluctuations[i] = priceFluctuation.doubleValue();
			this.priceFluctuationRatios[i] = priceFluctuation.divide(centerPrice, 6, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		this.centerEma10s = MathUtility.getExponentialMovingAverage(this.centerPrices, INTERNAL_ALPHA_10);
		this.centerEma15s = MathUtility.getExponentialMovingAverage(this.centerPrices, INTERNAL_ALPHA_15);
		this.centerEma20s = MathUtility.getExponentialMovingAverage(this.centerPrices, INTERNAL_ALPHA_20);
		this.centerEma50s = MathUtility.getExponentialMovingAverage(this.centerPrices, INTERNAL_ALPHA_50);
		this.centerEma100s = MathUtility.getExponentialMovingAverage(this.centerPrices, INTERNAL_ALPHA_100);
		this.centerEma250s = MathUtility.getExponentialMovingAverage(this.centerPrices, INTERNAL_ALPHA_250);
		final double[] rsiUs = new double[rawDatas.size()];
		final double[] rsiDs = new double[rawDatas.size()];
		double prevCenterPrice = this.centerPrices[this.centerPrices.length - 1];
		for(int i = this.centerPrices.length - 2; i >= 0; i--){
			final double currCenterPrice = this.centerPrices[i];
			final double diff = currCenterPrice - prevCenterPrice;
			if(diff > 0){
				rsiUs[i] = diff;
			}else if(diff < 0){
				rsiDs[i] = -diff;
			}
			prevCenterPrice = currCenterPrice;
		}
		this.centerRsiUEmas = MathUtility.getExponentialMovingAverage(rsiUs, 1.0 / 14);
		this.centerRsiDEmas = MathUtility.getExponentialMovingAverage(rsiDs, 1.0 / 14);
		this.centerRsis = new double[rawDatas.size()];
		for(int i = 0; i < rawDatas.size(); i++){
			final double rsiU = this.centerRsiUEmas[i];
			final double rsiD = this.centerRsiDEmas[i];
			final double rsiRs;
			if(rsiD == 0.0){
				rsiRs = 100;
			}else{
				rsiRs = rsiU / rsiD;
			}
			this.centerRsis[i] = 100 - 100 / (1 + rsiRs);
		}
	}
	public final int getStockNumber(){
		return this.stockNumber;
	}
	public final Date getStartDate(){
		return this.startDate;
	}
	public final Date getEndDate(){
		return this.endDate;
	}
	public final Date[] getDates(){
		return this.dates;
	}
	public final BigDecimal[] getOpenPrices(){
		return this.openPrices;
	}
	public final BigDecimal[] getHighPrices(){
		return this.highPrices;
	}
	public final BigDecimal[] getLowPrices(){
		return this.lowPrices;
	}
	public final BigDecimal[] getClosePrices(){
		return this.closePrices;
	}
	public final long[] getVolumes(){
		return this.volumes;
	}
	public final double[] getCenterPrices(){
		return this.centerPrices;
	}
	public final double[] getPriceFluctuations(){
		return this.priceFluctuations;
	}
	public final double[] getPriceFluctuationRatios(){
		return this.priceFluctuationRatios;
	}
	public final double[] getCenterEma10s(){
		return this.centerEma10s;
	}
	public final double[] getCenterEma15s(){
		return this.centerEma15s;
	}
	public final double[] getCenterEma20s(){
		return this.centerEma20s;
	}
	public final double[] getCenterEma50s(){
		return this.centerEma50s;
	}
	public final double[] getCenterEma100s(){
		return this.centerEma100s;
	}
	public final double[] getCenterEma250s(){
		return this.centerEma250s;
	}
	public final double[] getCenterRsiUEmas(){
		return this.centerRsiUEmas;
	}
	public final double[] getCenterRsiDEmas(){
		return this.centerRsiDEmas;
	}
	public final double[] getCenterRsis(){
		return this.centerRsis;
	}
	public final int getDayCount(){
		return this.dayCount;
	}
}
