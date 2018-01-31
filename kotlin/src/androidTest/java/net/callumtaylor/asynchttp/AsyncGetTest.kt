package net.callumtaylor.asynchttp

import android.support.test.runner.AndroidJUnit4
import android.util.Log
import junit.framework.Assert
import net.callumtaylor.asynchttp.obj.Request
import net.callumtaylor.asynchttp.processor.StringProcessor
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * // TODO: Add class description
 */
@RunWith(AndroidJUnit4::class)
class AsyncGetTest
{
	/**
	 * Tests a basic GET request
	 */
	@Test
	fun testGet()
	{
		val signal = CountDownLatch(1)

		AsyncHttpClient("http://httpbin.org/get")
			.get<String>(
				processor = object : StringProcessor()
				{
					override fun onChunkProcessed(request: Request, length: Long, total: Long)
					{
						Log.v("asynchttp", "download progress ${length} out of ${total}")
					}
				},
				response = { response ->
					Log.v("asynchttp", response.body);

					Assert.assertNotNull(response.body)
					signal.countDown()
				}
			)

		signal.await(60, TimeUnit.SECONDS)

		if (signal.count != 0L)
		{
			Assert.fail()
		}
	}
}
