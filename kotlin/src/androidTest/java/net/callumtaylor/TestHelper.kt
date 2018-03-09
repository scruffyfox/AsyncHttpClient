package net.callumtaylor

import org.junit.Assert
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object TestHelper
{
	fun createAsyncLatch(count: Int = 1): CountDownLatch
	{
		return CountDownLatch(count)
	}

	fun beginLatch(latch: CountDownLatch, timeout: Long = 60)
	{
		latch.await(timeout, TimeUnit.SECONDS)

		if (latch.count != 0L)
		{
			Assert.fail()
		}
	}

	fun stopLatch(latch: CountDownLatch)
	{
		latch.countDown()
	}
}
