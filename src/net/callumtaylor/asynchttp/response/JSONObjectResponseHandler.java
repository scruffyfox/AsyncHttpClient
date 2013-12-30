
package net.callumtaylor.asynchttp.response;

import org.json.JSONObject;

/**
 * This uses the standard JSON parser which is bundled with Android.
 *
 * This is <b>not</b> the same as {@link JsonResponseHandler}
 */
public abstract class JSONObjectResponseHandler extends AsyncHttpResponseHandler
{
	private StringBuffer stringBuffer;
	private JSONObject content;

	@Override public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalProcessed, long totalLength)
	{
		if (stringBuffer == null)
		{
			int total = (int)(totalLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : totalLength);
			stringBuffer = new StringBuffer(Math.max(8192, total));
		}

		if (chunk != null)
		{
			try
			{
				stringBuffer.append(new String(chunk, 0, chunkLength, "UTF-8"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Generate the JSONObject from the buffer and remove it to allow the GC to clean up properly
	 */
	@Override public void generateContent()
	{
		try
		{
			this.content = new JSONObject(stringBuffer.toString());
			this.stringBuffer = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();;
		}
	}

	/**
	 * @return The data represented as a JSONObject primitive type
	 */
	@Override public JSONObject getContent()
	{
		return content;
	}
}