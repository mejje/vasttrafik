package mejje;

import java.util.Vector;

public class Trip {
	protected String _text;
	public String getText() {
		return _text;
	}

	protected Vector _legs;
	public Vector getLegs() {
		return _legs;
	}

	public Trip(String text) {
		_text = text;
		_legs = new Vector();
	}
}
