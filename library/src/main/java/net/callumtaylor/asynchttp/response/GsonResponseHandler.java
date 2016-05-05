package net.callumtaylor.asynchttp.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

/**
 * Gson response handler used for automatically parsing a response into objects using Gson and class types
 * @param <T>
 */
public class GsonResponseHandler<T> extends StreamResponseHandler<T>
{
	private Class<T> outClass;
	private T content;
	private Gson gson;

	public GsonResponseHandler(Class<T> outClass)
	{
		this(new Gson(), outClass);
	}

	public GsonResponseHandler(GsonBuilder builder, Class<T> outClass)
	{
		this(builder.create(), outClass);
	}

	public GsonResponseHandler(Gson builder, Class<T> outClass)
	{
		this.outClass = outClass;
		this.gson = builder;
	}

	/**
	 * Generate the class from the buffer and remove it to allow the GC to clean up properly
	 */
	@SuppressWarnings("unchecked") @Override public void generateContent()
	{
		try
		{
			this.content = gson.fromJson(reader, (Class<T>)outClass);
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

		try
		{
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
