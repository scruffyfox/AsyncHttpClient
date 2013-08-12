package net.callumtaylor.asynchttp.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class GsonResponseHandler<T extends Class> extends AsyncHttpResponseHandler
{
	private T outClass;
	StringBuffer stringBuffer = new StringBuffer();

	public GsonResponseHandler(T outClass)
	{
		this.outClass = outClass;
	}

	@Override public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalProcessed, long totalLength)
	{
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
	@Override public T getContent()
	{
		try
		{
			Gson parser = new GsonBuilder().create();
			return (T)parser.fromJson(new String(stringBuffer.toString()), outClass);
		}
		catch (Exception e)
		{
			try
			{
				return (T)outClass.newInstance();
			}
			catch (Exception e2)
			{
				return null;
			}
		}
	}
}