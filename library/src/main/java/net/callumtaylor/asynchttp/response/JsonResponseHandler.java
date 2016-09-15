package net.callumtaylor.asynchttp.response;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;

import java.io.IOException;

/**
 * Basic Json response handler using the Gson library.
 *
 * * This is <b>not</b> the same as {@link JSONArrayResponseHandler} and {@link JSONObjectResponseHandler}
 */
public class JsonResponseHandler extends StreamResponseHandler<JsonElement>
{
	private JsonElement content;

	/**
	 * Generate the json object from the buffer and remove it to allow the GC to clean up properly
	 */
	@Override public void generateContent()
	{
		this.content = new JsonParser().parse(reader);

		try
		{
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		if (this.content == JsonNull.INSTANCE)
		{
			this.content = null;
		}
	}

	/**
	 * @return The data represented as a GSON JsonElement primitive type
	 */
	@Override public JsonElement getContent()
	{
		return this.content;
	}
}
