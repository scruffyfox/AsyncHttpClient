
package net.callumtaylor.asynchttp.response;

import org.json.JSONObject;

/**
 * This uses the standard JSON parser which is bundled with Android.
 *
 * This is <b>not</b> the same as {@link JsonResponseHandler}
 */
public class JSONObjectResponseHandler extends AsyncHttpResponseHandler
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
	@Override public JSONObject onSuccess()
	{
		try
		{
			return new JSONObject(new String(stringBuffer.toString()));
		}
		catch (Exception e)
		{
			return null;
		}
	}

	@Override public JSONObject onFailure()
	{
		try
		{
			return new JSONObject(new String(stringBuffer.toString()));
		}
		catch (Exception e)
		{
			return null;
		}
	}
}