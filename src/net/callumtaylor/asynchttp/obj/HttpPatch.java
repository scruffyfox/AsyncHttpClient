package net.callumtaylor.asynchttp.obj;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

public class HttpPatch extends HttpEntityEnclosingRequestBase
{
	public static final String METHOD_NAME = "PATCH";

	@Override public String getMethod()
	{
		return METHOD_NAME;
	}

	public HttpPatch(final String uri)
	{
		super();
		setURI(URI.create(uri));
	}

	public HttpPatch()
	{
		super();
	}
}