package net.callumtaylor.asynchttp.response;

import java.io.ByteArrayOutputStream;

public abstract class BinaryResponseHandler extends AsyncHttpResponseHandler
{
	ByteArrayOutputStream byteBuffer;

	@Override public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalProcessed, long totalLength)
	{
		if (byteBuffer == null)
		{
			byteBuffer = new ByteArrayOutputStream(Math.max(8192, (int)totalLength));
		}

		if (chunk != null)
		{
			byteBuffer.write(chunk, 0, chunkLength);
		}
	}

	/**
	 * Processes the response from the stream.
	 * This is <b>not</b> ran on the UI thread
	 *
	 * @return The data represented as a byte array
	 */
	@Override public byte[] getContent()
	{
		return byteBuffer.toByteArray();
	}
}