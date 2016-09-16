package net.callumtaylor.asynchttp;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.response.StringResponseHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Headers;

/**
 * // TODO: Add class description
 *
 * @author Callum Taylor
 */
public class AsyncCacheTest extends AndroidTestCase
{
	@Override protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * Tests automatic cache controlling
	 */
	public void testGetCacheControl() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);
		final long current = System.currentTimeMillis();

		AsyncHttpClient.cache = new Cache(getContext().getCacheDir(), 1024 * 1024 * 1);

		AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/", 20000);
		client.get("cache/60", Headers.of("Request", "1"), new StringResponseHandler()
		{
			@Override public void onFinish()
			{
				final String firstResponse = getContent();
				final int req1 = AsyncHttpClient.cache.requestCount();
				final int net1 = AsyncHttpClient.cache.networkCount();

				if (System.currentTimeMillis() - current < 60000)
				{
					AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/", 20000);
					client.get("cache/60", Headers.of("Request", "2"), new StringResponseHandler()
					{
						@Override public void onFinish()
						{
							final String secondResponse = getContent();
							final int req2 = AsyncHttpClient.cache.requestCount();
							final int net2 = AsyncHttpClient.cache.networkCount();

							// Second response should not have first response header
							Assert.assertNull(getConnectionInfo().responseHeaders.get("Request"));

							Assert.assertNotNull(firstResponse);
							Assert.assertNotNull(secondResponse);
							Assert.assertEquals(firstResponse, secondResponse);

							Assert.assertTrue(net1 == net2);
							Assert.assertTrue(req1 < req2);
							Assert.assertTrue(req2 > net2);

							signal.countDown();
						}
					});
				}
				else
				{
					signal.countDown();
				}
			}
		});

		signal.await(60, TimeUnit.SECONDS);

		// Clean up
		AsyncHttpClient.cache = null;

		if (signal.getCount() > 0)
		{
			Assert.fail();
		}
	}

	/**
	 * Tests that caching is not used if cache is set to null
	 */
	public void testGetNoCacheControl() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);
		final long current = System.currentTimeMillis();

		AsyncHttpClient.cache = null;

		AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/", 20000);
		client.get("cache/60", Headers.of("Request", "1"), new StringResponseHandler()
		{
			@Override public void onFinish()
			{
				final String firstResponse = getContent();

				if (System.currentTimeMillis() - current < 60000)
				{
					AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/", 20000);
					client.get("cache/60", Headers.of("Request", "2"), new StringResponseHandler()
					{
						@Override public void onFinish()
						{
							final String secondResponse = getContent();

							Assert.assertNotNull(firstResponse);
							Assert.assertNotNull(secondResponse);

							// Responses should be different
							Assert.assertTrue(!firstResponse.equals(secondResponse));
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

		if (signal.getCount() != 0)
		{
			Assert.fail();
		}
	}
}
