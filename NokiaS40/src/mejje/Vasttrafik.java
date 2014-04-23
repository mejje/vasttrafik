package mejje;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

public class Vasttrafik extends MIDlet implements CommandListener, DownloadListener {

	private DownloadManager manager;
	private Display display;
	private Splash splash;
	private Form errorForm;
	private List listRoutes;
	private List listTrips;
	private List listLegs;
	private Command update;
	private Command exit;
	private Command selectRoute;
	private Command selectTrip;
	private Command backTrips;
	private Command backLegs;

	private Date _time;
	private Vector _routes;
	private Route _selectedRoute;
	private Trip _selectedTrip;

	public Vasttrafik() {
		display = Display.getDisplay(this);
		splash = new Splash();

		errorForm = new Form("Error");

		listRoutes = new List("Routes", List.IMPLICIT);
		listRoutes.setFitPolicy(List.TEXT_WRAP_ON);
		listRoutes.setCommandListener(this);

		update = new Command("+1 öl", Command.HELP, 1);
		exit = new Command("Exit", Command.EXIT, 1);
		selectRoute = new Command("Select", Command.ITEM, 1);
		listRoutes.addCommand(update);
		listRoutes.addCommand(exit);
		listRoutes.setSelectCommand(selectRoute);

		listTrips = new List("Trips", List.IMPLICIT);
		listTrips.setFitPolicy(List.TEXT_WRAP_ON);
		listTrips.setCommandListener(this);

		backTrips = new Command("Back", Command.BACK, 1);
		selectTrip = new Command("Select", Command.ITEM, 1);
		listTrips.addCommand(backTrips);
		listTrips.setSelectCommand(selectTrip);

		listLegs = new List("Legs", List.IMPLICIT);
		listLegs.setFitPolicy(List.TEXT_WRAP_ON);
		listLegs.setCommandListener(this);

		backLegs = new Command("Back", Command.BACK, 1);
		listLegs.addCommand(backLegs);
		listLegs.setSelectCommand(null);

		manager = new DownloadManager();
		manager.addListener(this);
	}

	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#startApp()
	 */
	protected void startApp() throws MIDletStateChangeException {
		_time = new Date();
		download();
		// uncomment for testing
		/*_routes = getRoutes(_testData);
		renderRoutes();*/
	}

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command c, Displayable d) {
		if (c == this.update) {
			_time.setTime(_time.getTime() + (40 * 60 * 1000));
			download(_time);
		} else if (c == selectRoute) {
			int idx = listRoutes.getSelectedIndex();
			_selectedRoute = (Route) _routes.elementAt(idx);
			renderTrips();
			display.setCurrent(listTrips);
		} else if (c == selectTrip) {
			int idx = listTrips.getSelectedIndex();
			_selectedTrip = (Trip) _selectedRoute.getTrips().elementAt(idx);
			renderLegs();
			display.setCurrent(listLegs);
		} else if (c == backTrips) {
			display.setCurrent(listRoutes);
		} else if (c == backLegs) {
			display.setCurrent(listTrips);
		} else if (c == exit) {
			notifyDestroyed();
		}
	}

	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
	 */
	protected void destroyApp(boolean unconditional)
			throws MIDletStateChangeException {}

	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#pauseApp()
	 */
	protected void pauseApp() {}

	private void download() {
		display.setCurrent(splash);
		manager.download("http://vasttrafik.azurewebsites.net/trip", null);
	}

	private void download(Date time) {
		splash.setProgress(0);
		display.setCurrent(splash);
		String dateString = Utils.getDateString(time);
		String timeString = Utils.getTimeString(time);
		manager.download("http://vasttrafik.azurewebsites.net/trip",
			"date=" + dateString + "&time=" + timeString);
	}

	public void downloadError(Exception e) {
		e.printStackTrace();
		errorForm.deleteAll();
		errorForm.append(e.toString());
		display.setCurrent(errorForm);
	}

	public void downloadStatus(float percent) {
		splash.setProgress(percent / 100);
	}

	public void downloadCompleted(int responseCode, byte[] data) {
		String received;
		try {
			received = new String(data, "utf-8");
		} catch (UnsupportedEncodingException e) {
			received = null;
		}

		if (responseCode != 200) {
			errorForm.deleteAll();
			errorForm.append(received);
			display.setCurrent(errorForm);
			return;
		}

		_routes = received != null ? getRoutes(received) : new Vector();
		renderRoutes();
		display.setCurrent(listRoutes);
	}

	private Vector getRoutes(String data) {
		Vector routes = new Vector();
		Enumeration rows = Utils.split(data, "\n\n").elements();
		Route route = null;
		Trip trip = null;
		while (rows.hasMoreElements()) {
			String row = (String) rows.nextElement();
			String str = Utils.trimLines(row);
			if (row.startsWith("    ")) {
				trip.getLegs().addElement(new Leg(str));
			} else if (row.startsWith("  ")) {
				route.getTrips().addElement(trip = new Trip(str));
			} else if (row.length() > 0) {
				routes.addElement(route = new Route(str));
			}
		}
		return routes;
	}

	private void renderRoutes() {
		listRoutes.deleteAll();
		Enumeration routes = _routes.elements();
		while (routes.hasMoreElements()) {
			Route route = (Route) routes.nextElement();
			listRoutes.append(route.getText(), null);
		}
	}

	private void renderTrips() {
		listTrips.deleteAll();
		Enumeration trips = _selectedRoute.getTrips().elements();
		while (trips.hasMoreElements()) {
			Trip trip = (Trip) trips.nextElement();
			listTrips.append(trip.getText(), null);
		}
	}

	private void renderLegs() {
		listLegs.deleteAll();
		Enumeration legs = _selectedTrip.getLegs().elements();
		while (legs.hasMoreElements()) {
			Leg leg = (Leg) legs.nextElement();
			listLegs.append(leg.getText(), null);
		}
	}

	private String _testData = "Domkyrkan 22:50\nStubbvägen 0h35m\n\n  Domkyrkan A 22:50\n  Stubbvägen B 0h35m\n\n    Spårvagn 1\n    Östra Sjukhuset\n    Domkyrkan A 22:50\n    Svingeln B 22:57\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Svingeln D 23:02\n    Partille Centrum 5 23:11\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 23:15\n    Stubbvägen B 23:25\n\n  Domkyrkan A 23:10\n  Stubbvägen B 0h45m\n\n    Buss 16\n    Eketrägatan\n    Domkyrkan A 23:10\n    Nordstan B 23:15\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Nordstan C 23:28\n    Partille Centrum 5 23:42\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 23:45\n    Stubbvägen B 23:55\n\n  Domkyrkan A 23:15\n  Stubbvägen B 0h40m\n\n    Spårvagn 2\n    Mölndal\n    Domkyrkan A 23:15\n    Centralstationen A 23:19\n\n    Spårvagn 3\n    Kålltorp\n    Centralstationen B 23:24\n    Svingeln B 23:27\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Svingeln D 23:32\n    Partille Centrum 5 23:42\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 23:45\n    Stubbvägen B 23:55\n\n  Domkyrkan A 23:49\n  Stubbvägen B 0h36m\n\n    Spårvagn 1\n    Östra Sjukhuset\n    Domkyrkan A 23:49\n    Svingeln B 23:56\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Svingeln D 00:02\n    Partille Centrum 5 00:12\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 00:15\n    Stubbvägen B 00:25\n\n  Domkyrkan A 00:10\n  Stubbvägen B 0h45m\n\n    Buss 16\n    Eketrägatan\n    Domkyrkan A 00:10\n    Nordstan B 00:15\n\n    GUL EXPRESS\n    Partille centrum\n    Nordstan C 00:28\n    Partille Centrum 5 00:42\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 00:45\n    Stubbvägen B 00:55\n\n  Domkyrkan A 00:11\n  Stubbvägen B 0h44m\n\n    Spårvagn 11\n    Bergsjön\n    Domkyrkan A 00:11\n    Centralstationen A 00:15\n\n    BLÅ EXPRESS\n    Gråbo via Stenared\n    Centralstationen J 00:21\n    Svingeln D 00:22\n\n    GUL EXPRESS\n    Partille centrum\n    Svingeln D 00:32\n    Partille Centrum 5 00:42\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 00:45\n    Stubbvägen B 00:55\n\nCentralstationen 23:24\nStubbvägen 0h31m\n\n  Centralstationen B 23:24\n  Stubbvägen B 0h31m\n\n    Spårvagn 3\n    Kålltorp\n    Centralstationen B 23:24\n    Svingeln B 23:27\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Svingeln D 23:32\n    Partille Centrum 5 23:42\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 23:45\n    Stubbvägen B 23:55\n\nNordstan 22:58\nStubbvägen 0h27m\n\n  Nordstan C 22:58\n  Stubbvägen B 0h27m\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Nordstan C 22:58\n    Partille Centrum 5 23:11\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 23:15\n    Stubbvägen B 23:25\n\n  Nordstan C 23:28\n  Stubbvägen B 0h27m\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Nordstan C 23:28\n    Partille Centrum 5 23:42\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 23:45\n    Stubbvägen B 23:55\n\n  Nordstan C 23:58\n  Stubbvägen B 0h27m\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Nordstan C 23:58\n    Partille Centrum 5 00:12\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 00:15\n    Stubbvägen B 00:25\n\n  Nordstan C 00:28\n  Stubbvägen B 0h27m\n\n    GUL EXPRESS\n    Partille centrum\n    Nordstan C 00:28\n    Partille Centrum 5 00:42\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 00:45\n    Stubbvägen B 00:55\n\n  Nordstan C 00:58\n  Stubbvägen A 4h03m\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Nordstan C 00:58\n    Partille Centrum 5 01:12\n\n    Buss 513\n    Öjersjö brunn\n    Partille Centrum 6 01:15\n    Furulund B 01:24\n\n    Buss 515\n    Östra Sjukhuset via Partille centrum\n    Furulunds centrum A 05:00\n    Stubbvägen A 05:01\n\nHeden 22:46\nFurulund 0h38m\n\n  Heden A 22:46\n  Furulund B 0h38m\n\n    GRÖN EXPRESS\n    Kungälv, Ytterby\n    Heden A 22:46\n    Nordstan D 22:50\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Nordstan C 22:58\n    Partille Centrum 5 23:11\n\n    Buss 513\n    Heden via Öjersjö\n    Partille Centrum 6 23:15\n    Furulund B 23:24\n\n  Heden C 23:03\n  Furulund A 0h28m\n\n    Buss 513\n    Partille centrum via Öjersjö\n    Heden C 23:03\n    Furulund A 23:31\n\n  Heden A 23:17\n  Furulund B 0h37m\n\n    GRÖN EXPRESS\n    Kungälv, Ytterby\n    Heden A 23:17\n    Nordstan D 23:22\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Nordstan C 23:28\n    Partille Centrum 5 23:42\n\n    Buss 513\n    Heden via Öjersjö\n    Partille Centrum 6 23:45\n    Furulund B 23:54\n\n  Heden C 23:33\n  Furulund A 0h28m\n\n    Buss 513\n    Partille centrum via Öjersjö\n    Heden C 23:33\n    Furulund A 00:01\n\n  Heden A 23:47\n  Furulund B 0h37m\n\n    GRÖN EXPRESS\n    Kungälv, Ullstorp\n    Heden A 23:47\n    Nordstan D 23:52\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Nordstan C 23:58\n    Partille Centrum 5 00:12\n\n    Buss 513\n    Öjersjö brunn\n    Partille Centrum 6 00:15\n    Furulund B 00:24\n\nSvingeln 23:02\nStubbvägen 0h23m\n\n  Svingeln D 23:02\n  Stubbvägen B 0h23m\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Svingeln D 23:02\n    Partille Centrum 5 23:11\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 23:15\n    Stubbvägen B 23:25\n\n  Svingeln D 23:32\n  Stubbvägen B 0h23m\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Svingeln D 23:32\n    Partille Centrum 5 23:42\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 23:45\n    Stubbvägen B 23:55\n\n  Svingeln D 00:02\n  Stubbvägen B 0h23m\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Svingeln D 00:02\n    Partille Centrum 5 00:12\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 00:15\n    Stubbvägen B 00:25\n\n  Svingeln D 00:32\n  Stubbvägen B 0h23m\n\n    GUL EXPRESS\n    Partille centrum\n    Svingeln D 00:32\n    Partille Centrum 5 00:42\n\n    Buss 515\n    Furulund\n    Partille Centrum 4 00:45\n    Stubbvägen B 00:55\n\n  Svingeln D 00:56\n  Stubbvägen A 4h05m\n\n    Buss 513\n    Partille centrum via Sävedalen\n    Svingeln D 00:56\n    Furulund B 01:24\n\n    Buss 515\n    Östra Sjukhuset via Partille centrum\n    Furulunds centrum A 05:00\n    Stubbvägen A 05:01\n\n  Svingeln D 01:02\n  Stubbvägen A 3h59m\n\n    GUL EXPRESS\n    Jonsered via Partille centrum\n    Svingeln D 01:02\n    Partille Centrum 5 01:12\n\n    Buss 513\n    Öjersjö brunn\n    Partille Centrum 6 01:15\n    Furulund B 01:24\n\n    Buss 515\n    Östra Sjukhuset via Partille centrum\n    Furulunds centrum A 05:00\n    Stubbvägen A 05:01\n\nStubbvägen 23:01\nSvingeln 0h22m\n\n  Stubbvägen A 23:01\n  Svingeln C 0h22m\n\n    Buss 515\n    Östra Sjukhuset via Partille centrum\n    Stubbvägen A 23:01\n    Partille Centrum 7 23:12\n\n    GUL EXPRESS\n    Torslanda\n    Partille Centrum 9 23:15\n    Svingeln C 23:23\n\n  Stubbvägen A 23:31\n  Svingeln C 0h22m\n\n    Buss 515\n    Östra Sjukhuset via Partille centrum\n    Stubbvägen A 23:31\n    Partille Centrum 7 23:42\n\n    GUL EXPRESS\n    Torslanda\n    Partille Centrum 9 23:45\n    Svingeln C 23:53\n\n  Stubbvägen A 00:01\n  Svingeln C 0h22m\n\n    Buss 515\n    Östra Sjukhuset via Partille centrum\n    Stubbvägen A 00:01\n    Partille Centrum 7 00:12\n\n    GUL EXPRESS\n    Torslanda\n    Partille Centrum 9 00:15\n    Svingeln C 00:23\n\n  Stubbvägen B 00:25\n  Svingeln C 0h58m\n\n    Buss 515\n    Furulund\n    Stubbvägen B 00:25\n    Furulunds centrum A 00:28\n\n    Buss 513\n    Partille centrum\n    Furulund A 01:01\n    Partille Centrum 8 01:12\n\n    GUL EXPRESS\n    Torslanda\n    Partille Centrum 9 01:15\n    Svingeln C 01:23\n\n  Stubbvägen A 05:01\n  Svingeln C 0h22m\n\n    Buss 515\n    Östra Sjukhuset via Partille centrum\n    Stubbvägen A 05:01\n    Partille Centrum 7 05:12\n\n    GUL EXPRESS\n    Torslanda via Volvo Torslanda\n    Partille Centrum 9 05:15\n    Svingeln C 05:23\n\nFurulund 23:03\nSvingeln 0h20m\n\n  Furulund A 23:03\n  Svingeln C 0h20m\n\n    Buss 513\n    Nils Ericson Terminalen via Partille centrum\n    Furulund A 23:03\n    Partille Centrum 8 23:09\n\n    GUL EXPRESS\n    Torslanda\n    Partille Centrum 9 23:15\n    Svingeln C 23:23\n\n  Furulund A 23:03\n  Svingeln C 0h25m\n\n    Buss 513\n    Nils Ericson Terminalen via Partille centrum\n    Furulund A 23:03\n    Svingeln C 23:28\n\n  Furulund A 23:31\n  Svingeln C 0h22m\n\n    Buss 513\n    Nils Ericson Terminalen via Partille centrum\n    Furulund A 23:31\n    Partille Centrum 8 23:42\n\n    GUL EXPRESS\n    Torslanda\n    Partille Centrum 9 23:45\n    Svingeln C 23:53\n\n  Furulund A 23:31\n  Svingeln C 0h31m\n\n    Buss 513\n    Nils Ericson Terminalen via Partille centrum\n    Furulund A 23:31\n    Svingeln C 00:02\n\n  Furulund A 00:01\n  Svingeln C 0h22m\n\n    Buss 513\n    Nils Ericson Terminalen via Partille centrum\n    Furulund A 00:01\n    Partille Centrum 8 00:12\n\n    GUL EXPRESS\n    Torslanda\n    Partille Centrum 9 00:15\n    Svingeln C 00:23\n\n  Furulund A 00:01\n  Svingeln C 0h31m\n\n    Buss 513\n    Nils Ericson Terminalen via Partille centrum\n    Furulund A 00:01\n    Svingeln C 00:32\n\n";
}
