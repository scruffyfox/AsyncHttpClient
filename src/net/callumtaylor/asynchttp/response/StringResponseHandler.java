package net.callumtaylor.asynchttp.response;

public class StringResponseHandler extends AsyncHttpResponseHandler
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
	 * @return The data represented as a String
	 */
	@Override public String onSuccess()
	{
		return stringBuffer.toString();
	}

	@Override public String onFailure()
	{
		return stringBuffer.toString();
	}
}