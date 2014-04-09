package net.callumtaylor.asynchttp.response;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public abstract class JsonResponseHandler extends AsyncHttpResponseHandler
{
	private StringBuffer stringBuffer;
	private JsonElement content;

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
	 * Generate the json object from the buffer and remove it to allow the GC to clean up properly
	 */
	@Override public void generateContent()
	{
		this.content = new JsonParser().parse(stringBuffer.toString());
		this.stringBuffer = null;
	}

	/**
	 * @return The data represented as a GSON JsonElement primitive type
	 */
	@Override public JsonElement getContent()
	{
		return this.content;
	}
}