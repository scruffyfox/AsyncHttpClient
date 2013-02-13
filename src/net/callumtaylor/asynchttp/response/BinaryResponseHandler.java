package net.callumtaylor.asynchttp.response;

public class BinaryResponseHandler extends AsyncHttpResponseHandler
{
	/**
	 * Processes the response from the stream.
	 * This is <b>not</b> ran on the UI thread
	 *
	 * @param response
	 *            The complete byte array of content recieved from the
	 *            connection.
	 * @return The data represented as a byte array
	 */
	@Override public byte[] onSuccess(byte[] response)
	{
		return response;
	}
}