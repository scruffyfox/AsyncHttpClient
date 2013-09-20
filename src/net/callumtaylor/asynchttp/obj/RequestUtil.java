package net.callumtaylor.asynchttp.obj;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

public class RequestUtil
{
	/**
	 * Appends a list of KV params on to the end of a URI
	 * @param uri The URI to append to
	 * @param params The params to append
	 * @return The new URI
	 */
	public static String appendParams(String uri, List<NameValuePair> params)
	{
		try
		{
			if (params != null)
			{
				URIBuilder builder = new URIBuilder(uri);
				for (NameValuePair p : params)
				{
					builder.addParameter(p.getName(), p.getValue());
				}

				return builder.build().toString();
			}
		}
		catch (Exception e){}

		return uri;
	}
}