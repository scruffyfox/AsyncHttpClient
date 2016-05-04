package net.callumtaylor.asynchttp;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Callum Taylor
 */
public class AsyncTest extends AndroidTestCase
{
	final CountDownLatch signal = new CountDownLatch(1);

	@Override protected void setUp() throws Exception
	{
		super.setUp();
	}

	public void testGet() throws InterruptedException
	{
		new AsyncHttpClient("http://httpbin.org/")
			.get("get", new JsonResponseHandler()
			{
				@Override public void onFinish(boolean failed)
				{
					super.onFinish(failed);

					Assert.assertNotNull(getContent());

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.SECONDS);
	}
}
