package net.callumtaylor.asynchttp.obj;

import com.google.gson.JsonElement;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Convenience class for creating request body for json
 *
 * @author Callum Taylor
 */
public class JsonBody
{
	public static RequestBody create(JsonElement json)
	{
		return RequestBody.create(MediaType.parse("application/json"), json.toString());
	}
}
