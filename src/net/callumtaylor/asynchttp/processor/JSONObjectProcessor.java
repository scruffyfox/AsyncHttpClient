
package net.callumtaylor.asynchttp.processor;

import org.json.JSONObject;

/**
 * This uses the standard JSON parser which is bundled with Android.
 *
 * This is <b>not</b> the same as {@link JsonProcessor}
 */
public class JSONObjectProcessor extends Processor<JSONObject>
{
	private StringBuffer stringBuffer;

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
	 * Processes the response from the stream.
	 * This is <b>not</b> ran on the UI thread
	 *
	 * @return The data represented as a gson JsonElement primitive type
	 */
	@Override public JSONObject getContent()
	{
		try
		{
			return new JSONObject(stringBuffer.toString());
		}
		catch (Exception e)
		{
			return null;
		}
	}
}