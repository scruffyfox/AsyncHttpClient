package net.callumtaylor.asynchttp.processor;

import net.callumtaylor.asynchttp.obj.ConnectionInfo;

public abstract class Processor<E>
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
	 * Gets the content generated from the
	 * response.
	 *
	 * @return The generated content object
	 */
	public abstract E getContent();
}