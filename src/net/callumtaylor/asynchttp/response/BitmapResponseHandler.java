package net.callumtaylor.asynchttp.response;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public abstract class BitmapResponseHandler extends AsyncHttpResponseHandler
{
	private ByteArrayOutputStream byteBuffer;
	private Bitmap bitmap;

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
	 * Generate the bitmap from the buffer and remove it to allow the GC to clean up properly
	 */
	@Override public void generateContent()
	{
		this.bitmap = BitmapFactory.decodeByteArray(byteBuffer.toByteArray(), 0, byteBuffer.size(), null);
		this.byteBuffer = null;
	}

	/**
	 * @return The data represented as a bitmap
	 */
	@Override public Bitmap getContent()
	{
		return bitmap;
	}
}