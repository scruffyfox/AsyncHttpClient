package net.callumtaylor.asynchttp.obj.entity;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHeader;

/**
 * Creates a form entity to send multiple parts together.
 * Use this when uploading images/files.
 *
 * This code is based on the code from Rafael Sanches' blog.
 * http://blog.rafaelsanches.com/2011/01/29/upload-using-multipart-post-using-httpclient-in-android/
 */
public class MultiPartEntity implements HttpEntity
{
	private final static char[] MULTIPART_CHARS = {'-', '_', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

	private String boundary = null;

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final boolean isSetLast = false;
	private final boolean isSetFirst = false;

	public MultiPartEntity()
	{
		StringBuffer buf = new StringBuffer();
		Random rand = new Random();
		for (int i = 0; i < 30; i++)
		{
			buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
		}

		this.boundary = "AsyncHttpClient-callumtaylor.net-" + buf.toString();

		try
		{
			StringBuffer res = new StringBuffer("\r\n").append("--").append(boundary).append("\r\n");
			out.write(res.toString().getBytes());
		}
		catch (Exception e)
		{

		}
	}

	/**
	 * Adds a basic KV part to the entity
	 *
	 * @param key The new value
	 * @param value The value value
	 */
	public void addPart(String key, String value)
	{
		try
		{
			out.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n").getBytes());
			out.write(value.getBytes());
			out.write(("\r\n--" + boundary + "\r\n").getBytes());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Adds a new {@link HttpEntity} value to the entity. <b>note:</b> if you're
	 * looking to add a {@link FileEntity}, use {@link addFilePart}
	 *
	 * @param key
	 *            The key to add
	 * @param value
	 *            The {@link HttpEntity} part to add
	 */
	public void addPart(String key, HttpEntity value)
	{
		try
		{
			out.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n").getBytes());
			BufferedInputStream stream = new BufferedInputStream(value.getContent(), 8192);
			byte[] buffer = new byte[8192];
			int len = 0;

			while ((len = stream.read(buffer)) > -1)
			{
				out.write(buffer, 0, len);
			}

			out.write(("\r\n--" + boundary + "\r\n").getBytes());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Adds a new file based {@link HttpEntity} to the entity.
	 *
	 * @param key
	 *            The key to add
	 * @param value
	 *            The {@link HttpEntity} part to add
	 */
	public void addFilePart(String key, HttpEntity value)
	{
		try
		{
			StringBuffer fileRes = new StringBuffer();
			fileRes.append("Content-Disposition: form-data; name=\"").append(key)
					.append("\"; filename=\"").append("untitled").append("\"\r\n")
					.append("Content-Type: ").append(value.getContentType().getValue()).append("\r\n\r\n");

			out.write(fileRes.toString().getBytes());

			BufferedInputStream stream = new BufferedInputStream(value.getContent(), 8192);
			byte[] buffer = new byte[8192];
			int len = 0;

			while ((len = stream.read(buffer)) > -1)
			{
				out.write(buffer, 0, len);
			}

			out.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes());
			out.write(("\r\n--" + boundary + "\r\n").getBytes());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Adds a new file based {@link HttpEntity} to the entity.
	 *
	 * @param key
	 *            The key to add
	 * @param value
	 *            The {@link HttpEntity} part to add
	 */
	public void addFilePart(String key, String filename, HttpEntity value)
	{
		try
		{
			StringBuffer fileRes = new StringBuffer();
			fileRes.append("Content-Disposition: form-data; name=\"").append(key)
					.append("\"; filename=\"").append(filename).append("\"\r\n")
					.append("Content-Type: ").append(value.getContentType().getValue()).append("\r\n\r\n");

			out.write(fileRes.toString().getBytes());
			BufferedInputStream stream = new BufferedInputStream(value.getContent(), 8192);
			byte[] buffer = new byte[8192];
			int len = 0;

			while ((len = stream.read(buffer)) > -1)
			{
				out.write(buffer, 0, len);
			}

			out.write(("\r\n--" + boundary + "--\r\n").getBytes());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override public long getContentLength()
	{
		return out.size();
	}

	@Override public Header getContentType()
	{
		return new BasicHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
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

	@Override public Header getContentEncoding()
	{
		return null;
	}

	@Override public void consumeContent() throws IOException, UnsupportedOperationException
	{
		if (isStreaming())
		{
			throw new UnsupportedOperationException("Streaming entity does not implement #consumeContent()");
		}
	}

	@Override public InputStream getContent() throws IOException, UnsupportedOperationException
	{
		return new ByteArrayInputStream(out.toByteArray());
	}
}