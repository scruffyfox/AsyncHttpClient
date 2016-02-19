package net.callumtaylor.asynchttp.obj.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;

import android.net.Uri;

/**
 * Creates a request parameter entity for posting KV pairs to a server.
 *
 * Content is generated as URI encoded keyvalue pair and sent as "application/x-www-form-urlencoded"
 */
public class RequestEntity implements HttpEntity
{
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	/**
	 * Adds a new KV pair to the entity
	 *
	 * @param key The key to add
	 * @param value The value to add
	 */
	public void add(String key, String value)
	{
		try
		{
			if (out.size() > 0)
			{
				out.write('&');
			}

			out.write((Uri.encode(key) + "=" + Uri.encode(value)).getBytes());
		}
		catch (Exception e){}
	}

	/**
	 * Adds a new KV pair to the entity
	 *
	 * @param kv The KV pair object
	 */
	public void add(NameValuePair kv)
	{
		add(kv.getName(), kv.getValue());
	}

	@Override public void consumeContent() throws IOException
	{
	}

	@Override public InputStream getContent() throws IOException, IllegalStateException
	{
		return new ByteArrayInputStream(out.toByteArray());
	}

	@Override public Header getContentEncoding()
	{
		return null;
	}

	@Override public long getContentLength()
	{
		return out.size();
	}

	@Override public Header getContentType()
	{
		return new BasicHeader("Content-Type", "application/x-www-form-urlencoded");
	}

	@Override public boolean isChunked()
	{
		return false;
	}

	@Override public boolean isRepeatable()
	{
		return false;
	}

	@Override public boolean isStreaming()
	{
		return false;
	}

	@Override public void writeTo(OutputStream outstream) throws IOException
	{
		outstream.write(out.toByteArray());
	}
}