package mejje;

public interface DownloadListener {

	/**
	 * Called when download is completed.
	 * 
	 * @param data downloaded data.
	 */
	public void downloadCompleted(int statusCode, byte[] data);

	/**
	 * Called to update download status.
	 * 
	 * @param percent percent downloaded.
	 */
	public void downloadStatus(float percent);
	
	/**
	 * Called upon download error.
	 * 
	 * @param e error.
	 */
	public void downloadError(Exception e);
}
