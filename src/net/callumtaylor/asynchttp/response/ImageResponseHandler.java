package net.callumtaylor.asynchttp.response;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageResponseHandler extends AsyncHttpResponseHandler
{
	ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

	@Override public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalProcessed, long totalLength)
	{
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
	@Override public Bitmap onSuccess()
	{
		return BitmapFactory.decodeByteArray(byteBuffer.toByteArray(), 0, byteBuffer.size(), null);
	}
}