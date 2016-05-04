package net.callumtaylor.asynchttp.test;

import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

import junit.framework.TestSuite;

import net.callumtaylor.asynchttp.AsyncDeleteTest;
import net.callumtaylor.asynchttp.AsyncGetTest;
import net.callumtaylor.asynchttp.AsyncPatchTest;
import net.callumtaylor.asynchttp.AsyncPostTest;
import net.callumtaylor.asynchttp.AsyncPutTest;

/**
 * Test suite
 */
public class Runner extends InstrumentationTestRunner
{
	@Override
	public TestSuite getAllTests()
	{
		InstrumentationTestSuite suite = new InstrumentationTestSuite(this);

		suite.addTestSuite(AsyncGetTest.class);
		suite.addTestSuite(AsyncPostTest.class);
		suite.addTestSuite(AsyncPutTest.class);
		suite.addTestSuite(AsyncDeleteTest.class);
		suite.addTestSuite(AsyncPatchTest.class);

		return suite;
	}

	@Override
	public ClassLoader getLoader()
	{
		return Runner.class.getClassLoader();
	}
}
