package net.callumtaylor.asynchttp.obj;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Simple wrapper for sending requests using input streams. Useful for uploading large files in a buffered way
 * without having to completely read the file into memory.
 *
 * @author Callum Taylor
 */
public class InputStreamBody
{
	public static RequestBody create(final MediaType mediaType, final InputStream inputStream)
	{
		return new RequestBody()
		{
			@Override public MediaType contentType()
			{
				return mediaType;
			}

			@Override public long contentLength()
			{
				try
				{
					return inputStream.available();
				}
				catch (IOException e)
				{
					return 0;
				}
			}

			@Override public void writeTo(BufferedSink sink) throws IOException
			{
				Source source = null;

				try
				{
					source = Okio.source(inputStream);
					sink.writeAll(source);
				}
				finally
				{
					Util.closeQuietly(source);
				}
			}
		};
	}
}
