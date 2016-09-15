package net.callumtaylor.asynchttp;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.response.StringResponseHandler;

import okhttp3.Cache;
import okhttp3.Headers;

/**
 * // TODO: Add class description
 *
 * @author Callum Taylor
 */
public class SyncCacheTest extends AndroidTestCase
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
		final long current = System.currentTimeMillis();

		SyncHttpClient.cache = new Cache(getContext().getCacheDir(), 1024 * 1024 * 1);

		SyncHttpClient<String> client = new SyncHttpClient<String>("http://httpbin.org/", 20000);
		String firstResponse = client.get("cache/60", Headers.of("Request", "1"), new StringResponseHandler());

		final int req1 = SyncHttpClient.cache.requestCount();
		final int net1 = SyncHttpClient.cache.networkCount();

		if (System.currentTimeMillis() - current < 60000)
		{
			SyncHttpClient<String> client2 = new SyncHttpClient<String>("http://httpbin.org/", 20000);
			String secondResponse = client2.get("cache/60", Headers.of("Request", "2"), new StringResponseHandler());

			final int req2 = SyncHttpClient.cache.requestCount();
			final int net2 = SyncHttpClient.cache.networkCount();

			// Second response should not have first response header
			Assert.assertNull(client2.getConnectionInfo().responseHeaders.get("Request"));

			Assert.assertNotNull(firstResponse);
			Assert.assertNotNull(secondResponse);
			Assert.assertEquals(firstResponse, secondResponse);

			Assert.assertTrue(net1 == net2);
			Assert.assertTrue(req1 < req2);
			Assert.assertTrue(req2 > net2);
		}
		else
		{
			Assert.fail();
		}

		// Clean up
		AsyncHttpClient.cache = null;
	}

	/**
	 * Tests that caching is not used if cache is set to null
	 */
	public void testGetNoCacheControl() throws InterruptedException
	{
		final long current = System.currentTimeMillis();

		SyncHttpClient.cache = null;

		SyncHttpClient<String> client = new SyncHttpClient<String>("http://httpbin.org/", 20000);
		String firstResponse = client.get("cache/60", Headers.of("Request", "1"), new StringResponseHandler());

		if (System.currentTimeMillis() - current < 60000)
		{
			SyncHttpClient<String> client2 = new SyncHttpClient<String>("http://httpbin.org/", 20000);
			final String secondResponse = client2.get("cache/60", Headers.of("Request", "2"), new StringResponseHandler());

			Assert.assertNotNull(firstResponse);
			Assert.assertNotNull(secondResponse);

			// Responses should be different
			Assert.assertTrue(!firstResponse.equals(secondResponse));
		}
		else
		{
			Assert.fail();
		}
	}
}
