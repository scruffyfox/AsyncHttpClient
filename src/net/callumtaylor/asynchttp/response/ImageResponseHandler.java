package net.callumtaylor.asynchttp.response;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageResponseHandler extends AsyncHttpResponseHandler
{
	/**
	 * Processes the response from the stream.
	 * This is <b>not</b> ran on the UI thread
	 *
	 * @param response
	 *            The complete byte array of content recieved from the
	 *            connection.
	 * @return The data represented as a bitmap
	 */
	@Override public Bitmap onSuccess(byte[] response)
	{
		return BitmapFactory.decodeByteArray(response, 0, response.length, null);
	}
}