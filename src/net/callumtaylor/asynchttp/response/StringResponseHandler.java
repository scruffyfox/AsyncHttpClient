package net.callumtaylor.asynchttp.response;


public class StringResponseHandler extends AsyncHttpResponseHandler
{
	/**
	 * Processes the response from the stream.
	 * This is <b>not</b> ran on the UI thread
	 *
	 * @param response
	 *            The complete byte array of content recieved from the
	 *            connection.
	 * @return The data represented as a String
	 */
	@Override public String onSuccess(byte[] response)
	{
		return new String(response);
	}

	@Override public String onFailure(byte[] response)
	{
		return new String(response);
	}
}