package net.callumtaylor.asynchttp;

import android.graphics.Bitmap;
import android.test.AndroidTestCase;

import com.google.gson.annotations.SerializedName;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.response.BitmapResponseHandler;
import net.callumtaylor.asynchttp.response.GsonResponseHandler;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Callum Taylor
 */
public class ResponseHandlersTest extends AndroidTestCase
{
	final CountDownLatch signal = new CountDownLatch(1);

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

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests json response handler correctly serialises
	 * @throws InterruptedException
	 */
	public void testJsonResponseHandler() throws InterruptedException
	{
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

		signal.await(1500, TimeUnit.MILLISECONDS);
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

		signal.await(1500, TimeUnit.MILLISECONDS);
	}
}
