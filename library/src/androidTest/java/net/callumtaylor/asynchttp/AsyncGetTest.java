package net.callumtaylor.asynchttp;

import android.graphics.Bitmap;
import android.test.AndroidTestCase;

import com.google.gson.JsonElement;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.obj.ClientTaskImpl;
import net.callumtaylor.asynchttp.response.BitmapResponseHandler;
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
	 * Tests custom user agent is sent with request
	 * @throws InterruptedException
	 */
	public void testCustomUserAgent() throws InterruptedException
	{
		AsyncHttpClient.userAgent = "custom-user-agent";

		new AsyncHttpClient("http://httpbin.org/")
			.get("user-agent", new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());
					Assert.assertEquals(getContent().getAsJsonObject().get("user-agent").getAsString(), "custom-user-agent");

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.SECONDS);
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

		signal.await(1500, TimeUnit.SECONDS);
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

					Assert.assertEquals(totalLength, 16384);
				}

				@Override public void onByteChunkReceived(byte[] chunk, int chunkLength, long totalProcessed, long totalLength)
				{
					super.onByteChunkReceived(chunk, chunkLength, totalProcessed, totalLength);

					// End should have been reached
					if (chunk == null)
					{
						Assert.assertEquals(chunkLength, totalProcessed);
						Assert.assertEquals(totalProcessed, totalLength);
					}

					Assert.assertTrue(chunkLength > 0);
					Assert.assertEquals(totalLength, 16384);
				}

				@Override public void onByteChunkReceivedProcessed(long totalProcessed, long totalLength)
				{
					Assert.assertTrue(totalProcessed >= 0);
					Assert.assertEquals(totalLength, 16384);
				}

				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.SECONDS);
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

		signal.await(1500, TimeUnit.SECONDS);
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
					Assert.assertEquals(getConnectionInfo().responseCode, 404);

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.SECONDS);
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

		signal.await(1500, TimeUnit.SECONDS);
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

		signal.await(1500, TimeUnit.SECONDS);
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

		signal.await(1500, TimeUnit.SECONDS);
	}

	/**
	 * Tests auto 302 redirect
	 * @throws InterruptedException
	 */
	public void testGetRedirectJson() throws InterruptedException
	{
		AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/");
		client.setAllowAllSsl(true);
		client.get("absolute-redirect/1", new JsonResponseHandler()
		{
			@Override public void onFinish()
			{
				Assert.assertNotNull(getContent());
				Assert.assertTrue(getContent() instanceof JsonElement);

				signal.countDown();
			}
		});

		signal.await(1500, TimeUnit.SECONDS);
	}

	/**
	 * Tests auto 302 redirect
	 * @throws InterruptedException
	 */
	public void testGetBitmapResponse() throws InterruptedException
	{
		AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/");
		client.setAllowAllSsl(true);
		client.get("/image/png", new BitmapResponseHandler()
		{
			@Override public void onFinish()
			{
				Assert.assertNotNull(getContent());
				Assert.assertTrue(getContent() instanceof Bitmap);

				signal.countDown();
			}
		});

		signal.await(1500, TimeUnit.SECONDS);
	}
}
