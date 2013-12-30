package net.callumtaylor.asynchttp.response;

public abstract class StringResponseHandler extends AsyncHttpResponseHandler
{
	private StringBuffer stringBuffer;
	private String content;

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
	 * Generate the String from the buffer and remove it to allow the GC to clean up properly
	 */
	@Override public void generateContent()
	{
		this.content = stringBuffer.toString();
		this.stringBuffer = null;
	}

	/**
	 * @return The data represented as a String
	 */
	@Override public String getContent()
	{
		return content;
	}
}