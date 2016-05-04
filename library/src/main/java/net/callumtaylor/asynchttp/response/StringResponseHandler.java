package net.callumtaylor.asynchttp.response;

/**
 * Basic string response handler. Useful for parsing non-binary responses into any format
 */
public class StringResponseHandler extends ResponseHandler<String>
{
	private StringBuffer stringBuffer;
	private String content;

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
