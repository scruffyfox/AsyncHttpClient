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

import okhttp3.Cache;
import okhttp3.Headers;

/**
 * @author Callum Taylor
 */
public class AsyncGetTest extends AndroidTestCase
{
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
		final CountDownLatch signal = new CountDownLatch(1);

		new AsyncHttpClient("http://httpbin.org/")
			.get("get", new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());

					signal.countDown();
				}
			});

		signal.await(60, TimeUnit.SECONDS);
	}

	/**
	 * Tests the response handler publish methods are called in chunks
	 * @throws InterruptedException
	 */
	public void testGetProgress() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

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

		signal.await(60, TimeUnit.SECONDS);
	}

	/**
	 * Tests response parses correctly from json
	 * @throws InterruptedException
	 */
	public void testGetJson() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

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

		signal.await(60, TimeUnit.SECONDS);
	}

	/**
	 * Tests 404 response
	 * @throws InterruptedException
	 */
	public void testGet404() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

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

		signal.await(60, TimeUnit.SECONDS);
	}

	/**
	 * Tests gzip response
	 * @throws InterruptedException
	 */
	public void testGetGzipJson() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

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

		signal.await(60, TimeUnit.SECONDS);
	}

	/**
	 * Tests SSL connection response
	 * @throws InterruptedException
	 */
	public void testGetSslJson() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

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

		signal.await(60, TimeUnit.SECONDS);
	}

	/**
	 * Tests unsafe SSL connection response
	 * @throws InterruptedException
	 */
	public void testGetUnsafeSslJson() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

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

		signal.await(60, TimeUnit.SECONDS);
	}

	/**
	 * Tests auto 302 redirect
	 * @throws InterruptedException
	 */
	public void testGetRedirectJson() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

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

		signal.await(60, TimeUnit.SECONDS);
	}

	/**
	 * Tests no 302 redirect
	 * @throws InterruptedException
	 */
	public void testGetNoRedirect() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

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

		signal.await(60, TimeUnit.SECONDS);
	}

	/**
	 * Tests client timeout
	 * @throws InterruptedException
	 */
	public void testGetTimeout() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

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

		signal.await(60, TimeUnit.SECONDS);
	}

	/**
	 * Tests automatic cache controlling
	 */
	public void testGetCacheControl() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);
		final long current = System.currentTimeMillis();

		AsyncHttpClient.cache = new Cache(getContext().getCacheDir(), 1024 * 1024 * 1);

		AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/");
		client.get("cache/10", null, Headers.of("Timestamp", current + ""), new StringResponseHandler()
		{
			@Override public void onFinish()
			{
				final String firstResponse = getContent();

				if (System.currentTimeMillis() - current < 10000)
				{
					AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/");
					client.get("cache/10", null, Headers.of("Timestamp", System.currentTimeMillis() + ""), new JsonResponseHandler()
					{
						@Override public void onFinish()
						{
							String responseTimestamp = getContent().getAsJsonObject().get("headers").getAsJsonObject().get("Timestamp").getAsString();
							assertEquals("" + current, responseTimestamp);

							signal.countDown();
						}
					});
				}
				else
				{
					Assert.fail();
					signal.countDown();
				}
			}
		});

		signal.await(60, TimeUnit.SECONDS);
	}

	/**
	 * Tests that caching is not used if cache is set to null
	 */
	public void testGetCacheNotControl() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);
		final long current = System.currentTimeMillis();

		AsyncHttpClient.cache = null;

		AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/");
		client.get("cache/10", null, Headers.of("Timestamp", current + ""), new StringResponseHandler()
		{
			@Override public void onFinish()
			{
				final long latest = System.currentTimeMillis();

				if (latest - current < 10000)
				{
					AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/");
					client.get("cache/10", null, Headers.of("Timestamp", latest + ""), new JsonResponseHandler()
					{
						@Override public void onFinish()
						{
							String responseTimestamp = getContent().getAsJsonObject().get("headers").getAsJsonObject().get("Timestamp").getAsString();

							assertEquals("" + latest, responseTimestamp);
							assertTrue(current != latest);

							signal.countDown();
						}
					});
				}
				else
				{
					Assert.fail();
					signal.countDown();
				}
			}
		});

		signal.await(60, TimeUnit.SECONDS);
	}
}
