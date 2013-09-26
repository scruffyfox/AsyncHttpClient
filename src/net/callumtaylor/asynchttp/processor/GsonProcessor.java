package net.callumtaylor.asynchttp.processor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonProcessor<T> extends Processor<T>
{
	private T outClass;
	private StringBuffer stringBuffer;

	public GsonProcessor(T outClass)
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
	 * Processes the response from the stream. This is <b>not</b> ran on the UI
	 * thread
	 *
	 * @return The data represented as a gson JsonElement primitive type, or a
	 *         new instance of T if failed to parse Json
	 */
	@SuppressWarnings("unchecked") @Override public T getContent()
	{
		try
		{
			Gson parser = new GsonBuilder().create();
			return parser.fromJson(stringBuffer.toString(), (Class<T>)outClass);
		}
		catch (Exception e)
		{
			try
			{
				return ((Class<T>)outClass).newInstance();
			}
			catch (Exception e2)
			{
				return null;
			}
		}
	}
}