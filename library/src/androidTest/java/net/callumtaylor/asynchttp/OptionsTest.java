package net.callumtaylor.asynchttp;

import android.test.AndroidTestCase;

import com.google.gson.JsonElement;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.obj.NameValuePair;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;

/**
 * @author Callum Taylor
 */
public class OptionsTest extends AndroidTestCase
{
	final CountDownLatch signal = new CountDownLatch(1);

	@Override protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * Tests custom user agent is sent with request
	 */
	public void testSyncCustomUserAgent()
	{
		SyncHttpClient.userAgent = "custom-user-agent";

		JsonElement response = new SyncHttpClient<JsonElement>("http://httpbin.org/")
			.get("user-agent", new JsonResponseHandler());

		Assert.assertNotNull(response);
		Assert.assertEquals("custom-user-agent", response.getAsJsonObject().get("user-agent").getAsString());
	}

	/**
	 * Tests custom user agent is sent with request
	 * @throws InterruptedException
	 */
	public void testAsyncCustomUserAgent() throws InterruptedException
	{
		AsyncHttpClient.userAgent = "custom-user-agent";

		new AsyncHttpClient("http://httpbin.org/")
			.get("user-agent", new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());
					Assert.assertEquals("custom-user-agent", getContent().getAsJsonObject().get("user-agent").getAsString());

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests custom headers is correctly sent with request
	 */
	public void testSyncHeaders()
	{
		Headers headers = Headers.of("Header", "value");

		JsonElement response = new SyncHttpClient<JsonElement>("http://httpbin.org/")
			.get("headers", null, headers, new JsonResponseHandler());

		Assert.assertNotNull(response);
		Assert.assertTrue(response.getAsJsonObject().get("headers").getAsJsonObject().has("Header"));
	}

	/**
	 * Tests custom headers is correctly sent with request
	 * @throws InterruptedException
	 */
	public void testAsyncHeaders() throws InterruptedException
	{
		Headers headers = Headers.of("Header", "value");

		new AsyncHttpClient("http://httpbin.org/")
			.get("headers", null, headers, new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());
					Assert.assertTrue(getContent().getAsJsonObject().get("headers").getAsJsonObject().has("Header"));

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests custom query parameters is correctly sent with request
	 */
	public void testSyncParams()
	{
		List<NameValuePair> params = new ArrayList<>();
		params.add(new NameValuePair("key", "value"));

		JsonElement response = new SyncHttpClient<JsonElement>("http://httpbin.org/")
			.get("get", params, new JsonResponseHandler());

		Assert.assertNotNull(response);
		Assert.assertEquals("http://httpbin.org/get?key=value", response.getAsJsonObject().get("url").getAsString());
	}

	/**
	 * Tests custom query parameters is correctly sent with request
	 * @throws InterruptedException
	 */
	public void testAsyncParams() throws InterruptedException
	{
		List<NameValuePair> params = new ArrayList<>();
		params.add(new NameValuePair("key", "value"));

		new AsyncHttpClient("http://httpbin.org/")
			.get("get", params, new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());
					Assert.assertEquals("http://httpbin.org/get?key=value", getContent().getAsJsonObject().get("url").getAsString());

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}
}
