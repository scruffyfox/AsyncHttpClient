package net.callumtaylor.asynchttp;

import android.test.AndroidTestCase;

import com.google.gson.JsonElement;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.obj.ClientTaskImpl;
import net.callumtaylor.asynchttp.response.BasicResponseHandler;
import net.callumtaylor.asynchttp.response.ByteArrayResponseHandler;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import net.callumtaylor.asynchttp.response.StringResponseHandler;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Callum Taylor
 */
public class AsyncGetTest extends AndroidTestCase
{
	final CountDownLatch signal = new CountDownLatch(1);

	@Override protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * Tests a basic GET request
	 * @throws InterruptedException
	 */
	public void testGet() throws InterruptedException
	{
		new AsyncHttpClient("http://httpbin.org/")
			.get("get", new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests the response handler publish methods are called in chunks
	 * @throws InterruptedException
	 */
	public void testGetProgress() throws InterruptedException
	{
		new AsyncHttpClient("http://httpbin.org/")
			.get("bytes/16384", new ByteArrayResponseHandler()
			{
				@Override public void onReceiveStream(InputStream stream, ClientTaskImpl client, long totalLength) throws SocketTimeoutException, Exception
				{
					super.onReceiveStream(stream, client, totalLength);

					Assert.assertEquals(16384, totalLength);
				}

				@Override public void onByteChunkReceived(byte[] chunk, long chunkLength, long totalProcessed, long totalLength)
				{
					super.onByteChunkReceived(chunk, chunkLength, totalProcessed, totalLength);

					// End should have been reached
					if (chunk == null)
					{
						Assert.assertEquals(chunkLength, totalProcessed);
						Assert.assertEquals(totalProcessed, totalLength);
					}

					Assert.assertTrue(chunkLength > 0);
					Assert.assertEquals(16384, totalLength);
				}

				@Override public void onByteChunkReceivedProcessed(long totalProcessed, long totalLength)
				{
					Assert.assertTrue(totalProcessed >= 0);
					Assert.assertEquals(16384, totalLength);
				}

				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests response parses correctly from json
	 * @throws InterruptedException
	 */
	public void testGetJson() throws InterruptedException
	{
		new AsyncHttpClient("http://httpbin.org/")
			.get("get", new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());
					Assert.assertTrue(getContent() instanceof JsonElement);

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests 404 response
	 * @throws InterruptedException
	 */
	public void testGet404() throws InterruptedException
	{
		new AsyncHttpClient("http://httpbin.org/")
			.get("status/404", new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNull(getContent());
					Assert.assertEquals(404, getConnectionInfo().responseCode);

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests gzip response
	 * @throws InterruptedException
	 */
	public void testGetGzipJson() throws InterruptedException
	{
		new AsyncHttpClient("http://httpbin.org/")
			.get("gzip", new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());
					Assert.assertTrue(getContent() instanceof JsonElement);

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests SSL connection response
	 * @throws InterruptedException
	 */
	public void testGetSslJson() throws InterruptedException
	{
		new AsyncHttpClient("https://httpbin.org/")
			.get("get", new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());
					Assert.assertTrue(getContent() instanceof JsonElement);

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests unsafe SSL connection response
	 * @throws InterruptedException
	 */
	public void testGetUnsafeSslJson() throws InterruptedException
	{
		AsyncHttpClient client = new AsyncHttpClient("https://cruxoft.com/");
		client.setAllowAllSsl(true);
		client.get("get", new StringResponseHandler()
		{
			@Override public void onFinish()
			{
				Assert.assertNotNull(getContent());

				signal.countDown();
			}
		});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests auto 302 redirect
	 * @throws InterruptedException
	 */
	public void testGetRedirectJson() throws InterruptedException
	{
		AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/");
		client.setAllowRedirect(true);
		client.get("absolute-redirect/1", new JsonResponseHandler()
		{
			@Override public void onFinish()
			{
				Assert.assertNotNull(getContent());
				Assert.assertTrue(getContent() instanceof JsonElement);
				Assert.assertEquals(200, getConnectionInfo().responseCode);

				signal.countDown();
			}
		});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests no 302 redirect
	 * @throws InterruptedException
	 */
	public void testGetNoRedirect() throws InterruptedException
	{
		AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/");
		client.setAllowRedirect(false);
		client.get("status/302", new BasicResponseHandler()
		{
			@Override public void onFinish()
			{
				Assert.assertEquals(302, getConnectionInfo().responseCode);

				signal.countDown();
			}
		});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests client timeout
	 * @throws InterruptedException
	 */
	public void testGetTimeout() throws InterruptedException
	{
		AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/", 1000);
		client.get("delay/2", new BasicResponseHandler()
		{
			@Override public void onFinish()
			{
				Assert.assertNull(getContent());
				Assert.assertEquals(0, getConnectionInfo().responseCode);

				signal.countDown();
			}
		});

		signal.await(3000, TimeUnit.MILLISECONDS);
	}
}
