package net.callumtaylor.asynchttp.obj.entity;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private boolean isSetLast = false;
	private boolean isSetFirst = false;

	public MultiPartEntity()
	{
		StringBuffer buf = new StringBuffer();
		Random rand = new Random();
		for (int i = 0; i < 30; i++)
		{
			buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
		}

		this.boundary = "AsyncHttpClient-callumtaylor.net-" + buf.toString();
	}

	private void writeFirstBoundaryIfNeeds()
	{
		if (!isSetFirst)
		{
			try
			{
				out.write(("--" + boundary + "\r\n").getBytes());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		isSetFirst = true;
	}

	private void writeLastBoundaryIfNeeds()
	{
		if (isSetLast)
		{
			return;
		}

		try
		{
			out.write(("\r\n--" + boundary + "--\r\n").getBytes());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		isSetLast = true;
	}

	/**
	 * Adds a basic KV part to the entity
	 *
	 * @param key The new value
	 * @param value The value value
	 */
	public void addPart(String key, String value)
	{
		writeFirstBoundaryIfNeeds();
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
		writeFirstBoundaryIfNeeds();
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
		writeFirstBoundaryIfNeeds();
		try
		{
			String type = "Content-Type: " + value.getContentType().getValue() + "\r\n";
			out.write(("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + key + "\"\r\n").getBytes());
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
	 * Adds a new part to the entity from a file
	 *
	 * @param key
	 *            The key to add
	 * @param fileName
	 *            The request filename
	 * @param fin
	 *            The file's input stream
	 * @param isLast
	 *            If this is the last added part
	 */
	public void addPart(String key, String fileName, InputStream fin, boolean isLast)
	{
		addPart(key, fileName, fin, "application/octet-stream", isLast);
	}

	/**
	 * Adds a new part to the entity from a file
	 *
	 * @param key
	 *            The key to add
	 * @param fileName
	 *            The request filename
	 * @param fin
	 *            The file's input stream
	 * @param Type
	 *            The type of file which is being added
	 * @param isLast
	 *            If this is the last added part
	 */
	public void addPart(String key, String fileName, InputStream fin, String type, boolean isLast)
	{
		writeFirstBoundaryIfNeeds();
		try
		{
			type = "Content-Type: " + type + "\r\n";
			out.write(("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
			out.write(type.getBytes());
			out.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes());

			byte[] tmp = new byte[8192];
			int len = 0;
			while ((len = fin.read(tmp)) != -1)
			{
				out.write(tmp, 0, len);
			}

			if (!isLast)
			{
				out.write(("\r\n--" + boundary + "\r\n").getBytes());
			}

			out.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				fin.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Adds a new part to the entity from a file
	 *
	 * @param key
	 *            The key to add
	 * @param value
	 *            The file object to add to the entity
	 * @param isLast
	 *            If this is the last added part
	 */
	public void addPart(String key, File value, boolean isLast)
	{
		try
		{
			addPart(key, value.getName(), new FileInputStream(value), isLast);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	@Override public long getContentLength()
	{
		writeLastBoundaryIfNeeds();
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