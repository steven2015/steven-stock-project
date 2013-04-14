/**
 * 
 */
package steven.stock;

import java.io.IOException;
import java.text.ParseException;

import com.css.system.utility.DateUtility;

/**
 * @author Steven
 * 
 */
public class Main{
	public static void main(final String[] args) throws IOException, ParseException{
		final StockData data = new StockData(116);
		for(int i = 0; i < data.getDayCount(); i++){
			System.out.println(DateUtility.dateFormat.format(data.getDates()[i]) + "\t" + data.getCenterRsis()[i] + "\t" + data.getCenterPrices()[i]);
		}
	}
}
