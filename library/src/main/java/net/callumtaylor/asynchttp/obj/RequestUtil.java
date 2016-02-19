package net.callumtaylor.asynchttp.obj;

import java.util.List;

import org.apache.http.NameValuePair;

import android.net.Uri;
import android.os.Build;

public class RequestUtil
{
	/**
	 * Creates a user agent string for the device
	 * @return
	 */
	public static String getDefaultUserAgent()
	{
		StringBuilder result = new StringBuilder(64);
		result.append("Dalvik/");
		result.append(System.getProperty("java.vm.version"));
		result.append(" (Linux; U; Android ");

		String version = Build.VERSION.RELEASE;
		result.append(version.length() > 0 ? version : "1.0");

		// add the model for the release build
		if ("REL".equals(Build.VERSION.CODENAME))
		{
			String model = Build.MODEL;
			if (model.length() > 0)
			{
				result.append("; ");
				result.append(model);
			}
		}

		String id = Build.ID;
		if (id.length() > 0)
		{
			result.append(" Build/");
			result.append(id);
		}

		result.append(")");
		return result.toString();
	}

	/**
	 * Appends a list of KV params on to the end of a URI
	 * @param uri The URI to append to
	 * @param params The params to append
	 * @return The new URI
	 */
	public static Uri appendParams(Uri uri, List<NameValuePair> params)
	{
		try
		{
			if (params != null)
			{
				Uri.Builder builder = uri.buildUpon();
				for (NameValuePair p : params)
				{
					builder.appendQueryParameter(p.getName(), p.getValue());
				}

				return builder.build();
			}
		}
		catch (Exception e){}

		return uri;
	}
}