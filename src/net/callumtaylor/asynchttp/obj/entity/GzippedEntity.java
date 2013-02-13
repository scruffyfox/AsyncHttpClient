package net.callumtaylor.asynchttp.obj.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

/**
 * Creates compressed stream from a {@link HttpEntity} object.
 *
 * This sets the content type as "application/x-gzip" and the encoding as "x-gzip, gzip"
 */
public class GzippedEntity implements HttpEntity
{
	private HttpEntity mainEntity;

	public GzippedEntity(HttpEntity e) throws UnsupportedEncodingException
	{
		mainEntity = e;
	}

	@Override public Header getContentType()
	{
		return new BasicHeader("Content-Type", "application/x-gzip");
	}

	@Override public Header getContentEncoding()
	{
		return new BasicHeader("Content-Encoding", "x-gzip, gzip");
	}

	@Override public void consumeContent() throws IOException
	{
		mainEntity.consumeContent();
	}

	@Override public GZIPInputStream getContent() throws IOException, IllegalStateException
	{
		GZIPInputStream zipInputStream = null;

		try
		{
			InputStream inputStream = mainEntity.getContent();
			ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
			GZIPOutputStream gzipOutput = new GZIPOutputStream(bytesOutput);

			try
			{
				byte[] buffer = new byte[8192];
				int length = 0;

				while ((length = inputStream.read(buffer)) != -1)
				{
					gzipOutput.write(buffer, 0, length);
				}
			}
			finally
			{
				try
				{
					inputStream.close();
					gzipOutput.close();
				}
				catch (IOException ignore){}
			}

			zipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytesOutput.toByteArray()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return zipInputStream;
	}

	@Override public long getContentLength()
	{
		return mainEntity.getContentLength();
	}

	@Override public boolean isChunked()
	{
		return mainEntity.isChunked();
	}

	@Override public boolean isRepeatable()
	{
		return mainEntity.isRepeatable();
	}

	@Override public boolean isStreaming()
	{
		return mainEntity.isStreaming();
	}

	@Override public void writeTo(OutputStream outstream) throws IOException
	{
		mainEntity.writeTo(outstream);
	}
}