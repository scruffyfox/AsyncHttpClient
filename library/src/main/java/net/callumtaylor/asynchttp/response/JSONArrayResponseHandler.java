
package net.callumtaylor.asynchttp.response;

import org.json.JSONArray;

/**
 * This uses the standard JSON parser which is bundled with Android.
 *
 * This is <b>not</b> the same as {@link JsonResponseHandler}
 */
public class JSONArrayResponseHandler extends ResponseHandler<JSONArray>
{
	private StringBuffer stringBuffer;
	private JSONArray content;

	@Override public void onByteChunkReceived(byte[] chunk, long chunkLength, long totalProcessed, long totalLength)
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
				stringBuffer.append(new String(chunk, 0, (int)chunkLength, "UTF-8"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Generate the JSONArray from the buffer and remove it to allow the GC to clean up properly
	 */
	@Override public void generateContent()
	{
		try
		{
			this.content = new JSONArray(stringBuffer.toString());
			this.stringBuffer = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();;
		}
	}

	/**
	 * @return The data represented as a JSONArray primitive type
	 */
	@Override public JSONArray getContent()
	{
		return content;
	}
}
