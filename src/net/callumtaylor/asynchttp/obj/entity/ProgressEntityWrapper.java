package net.callumtaylor.asynchttp.obj.entity;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

public class ProgressEntityWrapper extends HttpEntityWrapper
{
	private final ProgressListener listener;

	public ProgressEntityWrapper(HttpEntity e, ProgressListener listener)
	{
		super(e);
		this.listener = listener;
	}

	@Override public long getContentLength()
	{
		return this.wrappedEntity.getContentLength();
	}

	@Override public void writeTo(final OutputStream outstream) throws IOException
	{
		super.writeTo(new CountingOutputStream(outstream, this.listener));
	}

	public static interface ProgressListener
	{
		public void onBytesTransferred(byte[] buffer, int len, long transferred);
	}

	public static class CountingOutputStream extends FilterOutputStream
	{
		private final ProgressListener listener;
		private long transferred;

		public CountingOutputStream(final OutputStream out, final ProgressListener listener)
		{
			super(out);
			this.listener = listener;
			this.transferred = 0;
		}

		@Override public void write(byte[] buffer) throws IOException
		{
			super.write(buffer);
		}

		@Override public void write(byte[] b, int off, int len) throws IOException
		{
			out.write(b, off, len);
			this.transferred += len;
			this.listener.onBytesTransferred(b, len, this.transferred);
		}

		@Override public void write(int b) throws IOException
		{
			out.write(b);
			this.transferred++;
			this.listener.onBytesTransferred(null, 1, this.transferred);
		}
	}
}