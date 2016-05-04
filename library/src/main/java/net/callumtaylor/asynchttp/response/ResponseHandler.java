package net.callumtaylor.asynchttp.response;

import net.callumtaylor.asynchttp.obj.ClientTaskImpl;
import net.callumtaylor.asynchttp.obj.ConnectionInfo;
import net.callumtaylor.asynchttp.obj.Packet;

import java.io.InputStream;
import java.net.SocketTimeoutException;

/**
 * This is the base class for response handlers in AsyncHttpClient. The method
 * flow is as follows:
 *
 * <pre>
 * onSend -> onByteChunkSent -> onByteChunkReceived -> beforeSuccess -> onSuccess/onFailure -> beforeFinish -> onFinish
 * </pre>
 *
 * {@link ResponseHandler#onByteChunkReceived}, {@link ResponseHandler#onByteChunkSent},
 * {@link ResponseHandler#beforeSuccess}, {@link ResponseHandler#onSuccess}, and {@link ResponseHandler#onFailure} all run in
 * the background thread. All your processing should be handled in one of those
 * 4 methods and then either call to run on UI thread a new runnable, or handle
 * in {@link ResponseHandler#onFinish} which runs on the UI thread
 *
 * In order to get the content created from the response handler, you must
 * call {@link ResponseHandler#getContent} which can be accessed in {@link ResponseHandler#onSuccess} or
 * {@link ResponseHandler#onFailure}
 */
public abstract class ResponseHandler<E>
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
	 * by chunk, to {@link ResponseHandler#onByteChunkReceived}
	 *
	 * @param stream
	 *            The response InputStream
	 * @param client
	 *            The client task. In order to call
	 *            {@link ResponseHandler#onByteChunkReceivedProcessed}, you must call
	 *            <code>client.postPublishProgress(new Packet(int readCount, int totalLength, boolean isDownload))</code>
	 *            This is required when displaying a progress indicator.
	 * @param totalLength
	 *            The total length of the stream
	 * @throws SocketTimeoutException
	 * @throws Exception
	 */
	public void onRecieveStream(InputStream stream, ClientTaskImpl client, long totalLength) throws SocketTimeoutException, Exception
	{
		byte[] buffer = new byte[8196];

		int len = 0;
		int readCount = 0;
		while ((len = stream.read(buffer)) > -1 && !client.isCancelled())
		{
			onByteChunkReceived(buffer, len, readCount, totalLength);

			client.transferProgress(new Packet(readCount, totalLength, true));

			readCount += len;
		}

		if (!client.isCancelled())
		{
			getConnectionInfo().responseLength = readCount;

			// we fake the content length, because it can be -1
			onByteChunkReceived(null, readCount, readCount, readCount);

			client.transferProgress(new Packet(readCount, totalLength, true));
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
	 * @param totalProcessed
	 *            The total amount of data processed from the request.
	 * @param totalLength
	 *            The total size of the request. <b>note:</b> This <i>can</i> be
	 *            -1 during download.
	 */
	public void onByteChunkReceived(byte[] chunk, int chunkLength, long totalProcessed, long totalLength){}

	/**
	 * Runs on the UI thread. Useful for updating progress bars.
	 *
	 * @param totalProcessed
	 *            The total processed sized of the request
	 * @param totalLength
	 *            The total length of the request
	 */
	public void onByteChunkReceivedProcessed(long totalProcessed, long totalLength){}

	/**
	 * Called when a chunk has been uploaded to the request. This will be
	 * called once every chunk request
	 *
	 * @param chunk
	 *            The chunk of data
	 * @param chunkLength
	 *            The length of the chunk
	 * @param totalProcessed
	 *            The total amount of data processed from the request.
	 * @param totalLength
	 *            The total size of the request.
	 */
	public void onByteChunkSent(byte[] chunk, long chunkLength, long totalProcessed, long totalLength){}

	/**
	 * Runs on the UI thread. Useful for updating progress bars.
	 *
	 * @param totalProcessed
	 *            The total processed sized of the request
	 * @param totalLength
	 *            The total length of the request
	 */
	public void onByteChunkSentProcessed(long totalProcessed, long totalLength){}

	/**
	 * Called just before {@link ResponseHandler#onSuccess}
	 */
	public void beforeSuccess(){}

	/**
	 * Override this method to efficiently generate your content from any buffers you have have
	 * used.
	 *
	 * This is called directly after {@link ResponseHandler#onRecieveStream} has finished
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
	public abstract E getContent();

	/**
	 * Processes the response from the stream.
	 * This is <b>not</b> ran on the UI thread
	 *
	 * @return The modified data set, or null
	 */
	public void onSuccess(){}

	/**
	 * Called when a response was not 2xx.
	 *
	 * @return The modified data set, or null
	 */
	public void onFailure(){}

	/**
	 * Called before {@link ResponseHandler#onFinish}
	 */
	public void beforeFinish(){}

	/**
	 * Called when the streams have all finished, success or not
	 */
	public void onFinish(){}
}
