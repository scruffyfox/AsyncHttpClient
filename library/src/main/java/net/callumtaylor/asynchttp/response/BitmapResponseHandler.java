package net.callumtaylor.asynchttp.response;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Convenience response handler class for receiving a bitmap stream.
 */
public class BitmapResponseHandler extends ResponseHandler<Bitmap>
{
	private ByteArrayOutputStream byteBuffer;
	private Bitmap bitmap;

	@Override public void onByteChunkReceived(byte[] chunk, long chunkLength, long totalProcessed, long totalLength)
	{
		if (byteBuffer == null)
		{
			int total = (int)(totalLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : totalLength);
			byteBuffer = new ByteArrayOutputStream(Math.max(8192, total));
		}

		if (chunk != null)
		{
			byteBuffer.write(chunk, 0, (int)chunkLength);
		}
	}

	/**
	 * Generate the bitmap from the buffer and remove it to allow the GC to clean up properly
	 */
	@Override public void generateContent()
	{
		if (byteBuffer.size() > 0)
		{
			this.bitmap = BitmapFactory.decodeByteArray(byteBuffer.toByteArray(), 0, byteBuffer.size(), null);
		}

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
