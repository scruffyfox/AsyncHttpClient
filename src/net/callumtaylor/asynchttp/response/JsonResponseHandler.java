package net.callumtaylor.asynchttp.response;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonResponseHandler extends AsyncHttpResponseHandler
{
	/**
	 * Processes the response from the stream.
	 * This is <b>not</b> ran on the UI thread
	 *
	 * @param response
	 *            The complete byte array of content recieved from the
	 *            connection.
	 * @return The data represented as a gson JsonElement primitive type
	 */
	@Override public JsonElement onSuccess(byte[] response)
	{
		return new JsonParser().parse(new String(response));
	}

	@Override public JsonElement onFailure(byte[] response)
	{
		return new JsonParser().parse(new String(response));
	}
}