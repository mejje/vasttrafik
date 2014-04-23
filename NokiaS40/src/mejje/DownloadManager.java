package mejje;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;


public class DownloadManager implements Runnable {

	private Vector listeners = new Vector();
	private boolean running;
	private String url;
	private String postData;
	
	public void download(String url, String postData) {
		if (!running) {
			this.url = url;
			this.postData = postData;
			Thread t = new Thread(this);
			t.start();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		ByteArrayOutputStream buffer = null;
		HttpConnection connection  = null;
		OutputStream outputStream = null;
		InputStream inputStream = null;
		Enumeration enumeration = null;
		int responseCode = 0;
		String encoding = null;
		running = true;
		try {
			buffer = new ByteArrayOutputStream();
			
			connection = (HttpConnection) Connector.open(this.url);
			connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
			if (postData != null) {
				connection.setRequestMethod(HttpConnection.POST);
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				outputStream = connection.openOutputStream();
				outputStream.write(postData.getBytes());
			}
			inputStream = connection.openInputStream();
			responseCode = connection.getResponseCode();
			encoding = connection.getEncoding();

			long length = connection.getLength();
			byte[] data = new byte[512];
			int total = 0x00;
			int read  = -1;
			do {
				read = inputStream.read(data);
				if (read > 0x00) {
					total += read;
					buffer.write(data, 0x00, read);
					enumeration = this.listeners.elements();
					while (enumeration.hasMoreElements()) {
						DownloadListener listener = (DownloadListener) enumeration.nextElement();
						listener.downloadStatus(total * 100f / length);
					}
				}
			} while (read != -1);
		} catch (IOException e) {
			enumeration = this.listeners.elements();
			while (enumeration.hasMoreElements()) {
				DownloadListener listener = (DownloadListener) enumeration.nextElement();
				listener.downloadError(e);
			}
		} finally {
			byte[] data = buffer.toByteArray();
			try {
				if (encoding.equals("gzip"))
					data = GZIP.inflate(data);

				inputStream.close();
				if (outputStream != null)
					outputStream.close();
				connection.close();
			} catch (Exception e) {
				enumeration = this.listeners.elements();
				while (enumeration.hasMoreElements()) {
					DownloadListener listener = (DownloadListener) enumeration.nextElement();
					listener.downloadError(e);
				}
			}

			enumeration = this.listeners.elements();
			while (enumeration.hasMoreElements()) {
				DownloadListener listener = (DownloadListener) enumeration.nextElement();
				listener.downloadCompleted(responseCode, data);
			}
		}
		running = false;
	}

	/**
	 * Adds a listener for downloads.
	 * 
	 * @param listener target listener.
	 */
	public void addListener(DownloadListener listener) {
		if (!this.listeners.contains(listener)) {			
			this.listeners.addElement(listener);
		}
	}
	
	/**
	 * Removes a listener for downloads.
	 * 
	 * @param listener target listener.
	 */
	public void removeListener(DownloadListener listener) {
		this.listeners.removeElement(listener);
	}
}
