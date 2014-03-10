package net.callumtaylor.asynchttp.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class GsonResponseHandler<T> extends AsyncHttpResponseHandler
{
	private Class<T> outClass;
	private T content;
	private StringBuffer stringBuffer;

	public GsonResponseHandler(Class<T> outClass)
	{
		this.outClass = outClass;
	}

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
	 * Generate the class from the buffer and remove it to allow the GC to clean up properly
	 */
	@SuppressWarnings("unchecked") @Override public void generateContent()
	{
		try
		{
			Gson parser = new GsonBuilder().create();
			this.content = parser.fromJson(stringBuffer.toString(), (Class<T>)outClass);
		}
		catch (Exception e)
		{
			try
			{
				this.content = ((Class<T>)outClass).newInstance();
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}
		}

		this.stringBuffer = null;
	}


	/**
	 * @return The data represented as a gson JsonElement primitive type, or a
	 *         new instance of T if failed to parse Json
	 */
	@Override public T getContent()
	{
		return content;
	}
}