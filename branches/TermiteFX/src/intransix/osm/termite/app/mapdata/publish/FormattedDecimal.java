package intransix.osm.termite.app.mapdata.publish;

import java.util.Locale;
import org.json.JSONString;

/**
 * This class extends the JSONString method to give custom formatted output to allow the
 * number of decimal points to be specifed
 * @author sutter
 */
public class FormattedDecimal implements JSONString {

	private double value;
	private int decimalPlaces;

	public FormattedDecimal(double value, int decimalPlaces) {
		this.value = value;
		this.decimalPlaces = decimalPlaces;
	}

	public String toJSONString() {
		return String.format(Locale.US,"%1$." + String.valueOf(decimalPlaces) + "f", value);
	}
	
	public double getValue() {
		return value;
	}
}
