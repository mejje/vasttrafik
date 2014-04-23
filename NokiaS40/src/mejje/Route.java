package mejje;

import java.util.Vector;

public class Route {
	protected String _text;
	public String getText() {
		return _text;
	}

	protected Vector _trips;
	public Vector getTrips() {
		return _trips;
	}

	public Route(String text) {
		_text = text;
		_trips = new Vector();
	}
}
