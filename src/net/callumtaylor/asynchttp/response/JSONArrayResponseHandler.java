
package net.callumtaylor.asynchttp.response;

import org.json.JSONArray;

/**
 * This uses the standard JSON parser which is bundled with Android.
 *
 * This is <b>not</b> the same as {@link JsonResponseHandler}
 */
public class JSONArrayResponseHandler extends AsyncHttpResponseHandler
{
	StringBuffer stringBuffer = new StringBuffer();

	@Override public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalProcessed, long totalLength)
	{
		try
		{
			stringBuffer.append(new String(chunk, 0, chunkLength, "UTF-8"));
		}
		catch (Exception e){}
	}

	/**
	 * Processes the response from the stream.
	 * This is <b>not</b> ran on the UI thread
	 *
	 * @return The data represented as a gson JsonElement primitive type
	 */
	@Override public JSONArray onSuccess()
	{
		try
		{
			return new JSONArray(new String(stringBuffer.toString()));
		}
		catch (Exception e)
		{
			return null;
		}
	}

	@Override public JSONArray onFailure()
	{
		try
		{
			return new JSONArray(new String(stringBuffer.toString()));
		}
		catch (Exception e)
		{
			return null;
		}
	}
}