package net.callumtaylor.asynchttp.response;

import net.callumtaylor.asynchttp.AsyncHttpClient.ClientExecutorTask;
import net.callumtaylor.asynchttp.obj.ConnectionInfo;
import net.callumtaylor.asynchttp.obj.Packet;

import java.io.InputStream;
import java.net.SocketTimeoutException;

/**
 * This is the base class for response handlers in AsyncHttpClient. The method
 * flow is as follows:
 *
 * <pre>
 * onSend -> onPublishedUploadProgress -> onPublishedDownloadProgress -> beforeCallback -> onSuccess/onFailure -> beforeFinish -> onFinish
 * </pre>
 *
 * {@link AsyncHttpResponseHandler#onPublishedDownloadProgress}, {@link AsyncHttpResponseHandler#onPublishedUploadProgress},
 * {@link AsyncHttpResponseHandler#beforeCallback}, {@link AsyncHttpResponseHandler#onSuccess}, and {@link AsyncHttpResponseHandler#onFailure} all run in
 * the background thread. All your processing should be handled in one of those
 * 4 methods and then either call to run on UI thread a new runnable, or handle
 * in {@link AsyncHttpResponseHandler#onFinish} which runs on the UI thread
 *
 * In order to get the content created from the response handler, you must
 * call {@link AsyncHttpResponseHandler#getContent} which can be accessed in {@link AsyncHttpResponseHandler#onSuccess} or
 * {@link AsyncHttpResponseHandler#onFailure}
 */
public abstract class AsyncHttpResponseHandler
{
	private final ConnectionInfo connectionInfo = new ConnectionInfo();

	public ConnectionInfo getConnectionInfo()
	{
		return connectionInfo;
	}

	/**
	 * Called when the connection is first made
	 */
	public void onSend(){}

	/**
	 * Called when processing the response from a stream. Use this to override
	 * the processing of the InputStream to handle the response differently.
	 * Default is to read the response as a byte-array which gets passed, chunk
	 * by chunk, to {@link AsyncHttpResponseHandler#onPublishedDownloadProgress}
	 *
	 * @param stream
	 *            The response InputStream
	 * @param client
	 *            The client task. In order to call
	 *            {@link AsyncHttpResponseHandler#onPublishedDownloadProgressUI}, you must call
	 *            <code>client.postPublishProgress(new Packet(int readCount, int totalLength, boolean isDownload))</code>
	 *            This is required when displaying a progress indicator.
	 * @param totalLength
	 *            The total length of the stream
	 * @throws SocketTimeoutException
	 * @throws Exception
	 */
	public void onBeginPublishedDownloadProgress(InputStream stream, ClientExecutorTask client, long totalLength) throws SocketTimeoutException, Exception
	{
		byte[] buffer = new byte[8196];

		int len = 0;
		int readCount = 0;
		while ((len = stream.read(buffer)) > -1 && !client.isCancelled())
		{
			onPublishedDownloadProgress(buffer, len, totalLength);
			onPublishedDownloadProgress(buffer, len, readCount, totalLength);

			client.postPublishProgress(new Packet(readCount, totalLength, true));

			readCount += len;
		}

		if (!client.isCancelled())
		{
			getConnectionInfo().responseLength = readCount;

			// we fake the content length, because it can be -1
			onPublishedDownloadProgress(null, readCount, readCount);
			onPublishedDownloadProgress(null, readCount, readCount, readCount);

			client.postPublishProgress(new Packet(readCount, totalLength, true));
		}

		stream.close();
	}

	/**
	 * Called when a chunk has been downloaded from the request. This will be
	 * called once every chunk request, and once extra when all the content is
	 * downloaded.
	 *
	 * @param chunk
	 *            The chunk of data. This will be the <b>null</b> after the total amount has been downloaded.
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
	 *            The chunk of data. This will be the <b>null</b> after the total amount has been downloaded.
	 * @param chunkLength
	 *            The length of the chunk
	 * @param totalProcessed
	 *            The total amount of data processed from the request.
	 * @param totalLength
	 *            The total size of the request. <b>note:</b> This <i>can</i> be
	 *            -1 during download.
	 */
	public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalProcessed, long totalLength){}

	/**
	 * Runs on the UI thread. Useful for updating progress bars.
	 *
	 * @param totalProcessed
	 *            The total processed sized of the request
	 * @param totalLength
	 *            The total length of the request
	 */
	public void onPublishedDownloadProgressUI(long totalProcessed, long totalLength){}

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
	 * @param totalProcessed
	 *            The total amount of data processed from the request.
	 * @param totalLength
	 *            The total size of the request.
	 */
	public void onPublishedUploadProgress(byte[] chunk, int chunkLength, long totalProcessed, long totalLength){}

	/**
	 * Runs on the UI thread. Useful for updating progress bars.
	 *
	 * @param totalProcessed
	 *            The total processed sized of the request
	 * @param totalLength
	 *            The total length of the request
	 */
	public void onPublishedUploadProgressUI(long totalProcessed, long totalLength){}

	/**
	 * Called just before {@link AsyncHttpResponseHandler#onSuccess}
	 */
	public void beforeCallback(){}

	/**
	 * Override this method to efficiently generate your content from any buffers you have have
	 * used.
	 *
	 * This is called directly after {@link AsyncHttpResponseHandler#onBeginPublishedDownloadProgress} has finished
	 */
	public abstract void generateContent();

	/**
	 * Gets the content generated from the
	 * response.
	 *
	 * You should only call this once
	 *
	 * @return The generated content object
	 */
	public abstract Object getContent();

	/**
	 * Processes the response from the stream.
	 * This is <b>not</b> ran on the UI thread
	 *
	 * @return The modified data set, or null
	 */
	public abstract void onSuccess();

	/**
	 * Called when a response was not 2xx.
	 *
	 * @return The modified data set, or null
	 */
	public void onFailure(){}

	/**
	 * Called before {@link AsyncHttpResponseHandler#onFinish}
	 */
	public void beforeFinish(){}

	/**
	 * Called when the streams have all finished, success or not
	 */
	public void onFinish(){}

	/**
	 * Called when the streams have all finished, success or not
	 *
	 * @param failed
	 *            If the stream failed or not. Useful to display any UI updates
	 *            here.
	 */
	public void onFinish(boolean failed){}
}