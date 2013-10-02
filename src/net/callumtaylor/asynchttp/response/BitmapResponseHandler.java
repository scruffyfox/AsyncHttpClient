package net.callumtaylor.asynchttp.response;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public abstract class BitmapResponseHandler extends AsyncHttpResponseHandler
{
	private ByteArrayOutputStream byteBuffer;

	@Override public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalProcessed, long totalLength)
	{
		if (byteBuffer == null)
		{
			int total = (int)(totalLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : totalLength);
			byteBuffer = new ByteArrayOutputStream(Math.max(8192, total));
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
	 * @return The data represented as a bitmap
	 */
	@Override public Bitmap getContent()
	{
		return BitmapFactory.decodeByteArray(byteBuffer.toByteArray(), 0, byteBuffer.size(), null);
	}
}