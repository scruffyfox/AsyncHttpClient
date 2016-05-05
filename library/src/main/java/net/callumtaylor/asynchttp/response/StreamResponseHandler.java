package net.callumtaylor.asynchttp.response;

import net.callumtaylor.asynchttp.obj.ClientTaskImpl;
import net.callumtaylor.asynchttp.obj.Packet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Simple response handler that uses an input stream reader rather than appending an string buffer
 */
public abstract class StreamResponseHandler<E> extends ResponseHandler<E>
{
	protected InputStreamReader reader;

	@Override public void onReceiveStream(InputStream stream, final ClientTaskImpl client, final long totalLength) throws Exception
	{
		if (reader == null)
		{
			reader = new InputStreamReader(new BufferedInputStream(stream, 8192)
			{
				private long total = 0;

				@Override public synchronized int read(byte[] buffer, int byteOffset, int byteCount) throws IOException
				{
					int len = super.read(buffer, byteOffset, byteCount);

					onByteChunkReceived(buffer, len, total, totalLength);
					client.transferProgress(new Packet(total, totalLength, true));

					total += byteCount;

					return len;
				}
			});
		}

		if (!client.isCancelled())
		{
			getConnectionInfo().responseLength = totalLength;

			// we fake the content length, because it can be -1
			onByteChunkReceived(null, totalLength, totalLength, totalLength);

			client.transferProgress(new Packet(totalLength, totalLength, true));
		}
	}
}
