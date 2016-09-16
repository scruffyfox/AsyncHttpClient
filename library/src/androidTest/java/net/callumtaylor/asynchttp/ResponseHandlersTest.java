package net.callumtaylor.asynchttp;

import android.graphics.Bitmap;
import android.test.AndroidTestCase;

import com.google.gson.annotations.SerializedName;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.response.BitmapResponseHandler;
import net.callumtaylor.asynchttp.response.ByteArrayResponseHandler;
import net.callumtaylor.asynchttp.response.GsonResponseHandler;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Callum Taylor
 */
public class ResponseHandlersTest extends AndroidTestCase
{
	@Override protected void setUp() throws Exception
	{
		super.setUp();
	}

	public static class HttpBinResponse
	{
		public Object args;
		public HttpBinHeaders headers;
		public String url;
		public String origin;

		public static class HttpBinHeaders
		{
			@SerializedName("User-Agent") public String userAgent;
		}
	}

	/**
	 * Tests gson response handler correctly serialises
	 * @throws InterruptedException
	 */
	public void testGsonResponseHandler() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

		new AsyncHttpClient("http://httpbin.org/")
			.get("get", new GsonResponseHandler<HttpBinResponse>(HttpBinResponse.class)
			{
				@Override public void onByteChunkReceivedProcessed(long totalProcessed, long totalLength)
				{
					Assert.assertTrue(totalProcessed >= 0);
					Assert.assertTrue(totalLength >= 0);
				}

				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());
					Assert.assertEquals("http://httpbin.org/get", getContent().url);
					Assert.assertNotNull(getContent().origin);
					Assert.assertNotNull(getContent().args);
					Assert.assertNotNull(getContent().headers);

					signal.countDown();
				}
			});

		signal.await(60, TimeUnit.SECONDS);

		if (signal.getCount() != 0)
		{
			Assert.fail();
		}
	}

	/**
	 * Tests json response handler correctly serialises
	 * @throws InterruptedException
	 */
	public void testJsonResponseHandler() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

		new AsyncHttpClient("http://httpbin.org/")
			.get("get", new JsonResponseHandler()
			{
				@Override public void onByteChunkReceivedProcessed(long totalProcessed, long totalLength)
				{
					Assert.assertTrue(totalProcessed >= 0);
					Assert.assertTrue(totalLength >= 0);
				}

				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());

					Assert.assertEquals("http://httpbin.org/get", getContent().getAsJsonObject().get("url").getAsString());
					Assert.assertNotNull(getContent().getAsJsonObject().get("origin").getAsString());
					Assert.assertNotNull(getContent().getAsJsonObject().get("args"));
					Assert.assertNotNull(getContent().getAsJsonObject().get("headers"));

					signal.countDown();
				}
			});

		signal.await(60, TimeUnit.SECONDS);

		if (signal.getCount() != 0)
		{
			Assert.fail();
		}
	}

	/**
	 * Tests auto 302 redirect
	 * @throws InterruptedException
	 */
	public void testGetBitmapResponse() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);
		
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

		signal.await(60, TimeUnit.SECONDS);

		if (signal.getCount() != 0)
		{
			Assert.fail();
		}
	}

	/**
	 * Tests null response for byte response handler
	 * @throws InterruptedException
	 */
	public void testGetNullBytes() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

		new AsyncHttpClient("http://httpbin.org/")
			.get("status/404", new ByteArrayResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNull(getContent());
					Assert.assertEquals(404, getConnectionInfo().responseCode);

					signal.countDown();
				}
			});

		signal.await(60, TimeUnit.SECONDS);

		if (signal.getCount() != 0)
		{
			Assert.fail();
		}
	}
}
