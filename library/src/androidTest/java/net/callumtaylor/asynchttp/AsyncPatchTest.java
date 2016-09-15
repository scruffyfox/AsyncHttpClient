package net.callumtaylor.asynchttp;

import android.test.AndroidTestCase;

import com.google.gson.JsonElement;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * @author Callum Taylor
 */
public class AsyncPatchTest extends AndroidTestCase
{
	@Override protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * Tests a basic PATCH request
	 * @throws InterruptedException
	 */
	public void testPatch() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

		new AsyncHttpClient("http://httpbin.org/")
			.patch("patch", new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());

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
	 * Tests the response handler publish methods are called in chunks
	 * @throws InterruptedException
	 */
	public void testPatchProgress() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

		byte[] patchData = new byte[16384];
		for (int index = 0; index < patchData.length; index++)
		{
			patchData[index] = (byte)index;
		}

		RequestBody patchBody = MultipartBody.create(MediaType.parse("application/octet-stream"), patchData);

		new AsyncHttpClient("http://httpbin.org/")
			.patch("patch", patchBody, new JsonResponseHandler()
			{
				@Override public void onByteChunkSent(byte[] chunk, long chunkLength, long totalProcessed, long totalLength)
				{
					Assert.assertNotNull(chunk);
					Assert.assertTrue(chunkLength > 0);
					Assert.assertEquals(16384, totalLength);
				}

				@Override public void onByteChunkSentProcessed(long totalProcessed, long totalLength)
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

		if (signal.getCount() != 0)
		{
			Assert.fail();
		}
	}

	/**
	 * Tests response parses correctly from json
	 * @throws InterruptedException
	 */
	public void testPatchJson() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

		RequestBody patchBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

		new AsyncHttpClient("http://httpbin.org/")
			.patch("patch", patchBody, new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());
					Assert.assertTrue(getContent() instanceof JsonElement);

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
	 * Tests 404 response
	 * @throws InterruptedException
	 */
	public void testGet404() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

		new AsyncHttpClient("http://httpbin.org/")
			.patch("status/404", new JsonResponseHandler()
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

	/**
	 * Tests SSL connection response
	 * @throws InterruptedException
	 */
	public void testPatchSslJson() throws InterruptedException
	{
		final CountDownLatch signal = new CountDownLatch(1);

		RequestBody patchBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

		new AsyncHttpClient("https://httpbin.org/")
			.patch("patch", patchBody, new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());
					Assert.assertTrue(getContent() instanceof JsonElement);

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
