package net.callumtaylor.asynchttp.response;

import net.callumtaylor.asynchttp.obj.ConnectionInfo;

/**
 * This is the base class for response handlers in AsyncHttpClient. The method
 * flow is as follows:
 *
 * <pre>
 * onSend -> onPublishedUploadProgress -> onPublishedDownloadProgress -> beforeCallback -> onSuccess/onFailure -> beforeFinish -> onFinish
 * </pre>
 *
 * {@link onPublishedDownloadProgress}, {@link onPublishedUploadProgress},
 * {@link beforeCallback}, {@link onSuccess}, and {@link onFailure} all run in
 * the bacgkround thread. All your processing should be handled in one of those
 * 4 methods and then either call to run on UI thread a new runnable, or handle
 * in {@link onFinish} which runs on the UI thread
 */
public abstract class AsyncHttpResponseHandler
{
	private ConnectionInfo connectionInfo = new ConnectionInfo();

	public ConnectionInfo getConnectionInfo()
	{
		return connectionInfo;
	}

	/**
	 * Called when the connection is first made
	 */
	public void onSend(){}

	/**
	 * Called when a chunk has been downloaded from the request. This will be
	 * called once every chunk request, and once extra when all the content is
	 * downloaded.
	 *
	 * @param chunk
	 *            will be the total byte array when this is called.
	 * @param chunk
	 *            The chunk of data
	 * @param chunkLength
	 *            The length of the chunk
	 * @param totalLength
	 *            The total size of the request. <b>note:</b> This <i>can</i> be
	 *            -1 during download.
	 */
	public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalLength){}

	/**
	 * Called when a chunk has been downloaded from the request. This will be
	 * called once every chunk request, and once extra when all the content is
	 * downloaded.
	 *
	 * @param chunk
	 *            will be the total byte array when this is called.
	 * @param chunk
	 *            The chunk of data
	 * @param chunkLength
	 *            The length of the chunk
	 * @param totalLength
	 *            The total size of the request. <b>note:</b> This <i>can</i> be
	 *            -1 during download.
	 * @param totalProcessed
	 *            The total amount of data processed from the request.
	 */
	public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalLength, long totalProcessed){}

	/**
	 * Called when a chunk has been uploaded to the request. This will be
	 * called once every chunk request
	 *
	 * @param chunk
	 *            will be the total byte array when this is called.
	 * @param chunk
	 *            The chunk of data
	 * @param chunkLength
	 *            The length of the chunk
	 * @param totalLength
	 *            The total size of the request.
	 */
	public void onPublishedUploadProgress(byte[] chunk, int chunkLength, long totalLength){}

	/**
	 * Called when a chunk has been uploaded to the request. This will be
	 * called once every chunk request
	 *
	 * @param chunk
	 *            will be the total byte array when this is called.
	 * @param chunk
	 *            The chunk of data
	 * @param chunkLength
	 *            The length of the chunk
	 * @param totalLength
	 *            The total size of the request.
	 * @param totalProcessed
	 *            The total amount of data processed from the request.
	 */
	public void onPublishedUploadProgress(byte[] chunk, int chunkLength, long totalLength, long totalProcessed){}

	/**
	 * Called just before {@link onSuccess}
	 */
	public void beforeCallback(){}

	/**
	 * Processes the response from the stream.
	 * This is <b>not</b> ran on the UI thread
	 *
	 * @param response
	 *            The complete byte array of content recieved from the
	 *            connection. Override this to handle the content and return it
	 *            if you plan on subclassing your handler to allow the subclass
	 *            to call super to get the modified content.
	 * @return The modified data set, or null
	 */
	public abstract Object onSuccess(byte[] response);

	/**
	 * Called when a response was not 2xx.
	 *
	 * @param response
	 *            The returned data from the server The complete byte array of
	 *            content recieved from the connection. Override this to handle
	 *            the content and return it if you plan on subclassing your
	 *            handler to allow the subclass to call super to get the
	 *            modified content.
	 * @return The modified data set, or null
	 */
	public Object onFailure(byte[] response){ return null; }

	/**
	 * Called before {@link onFinish}
	 */
	public void beforeFinish(){}

	/**
	 * Called when the streams have all finished, success or not
	 */
	public void onFinish(){}
}