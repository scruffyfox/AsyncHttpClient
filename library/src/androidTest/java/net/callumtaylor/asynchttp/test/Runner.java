package net.callumtaylor.asynchttp.test;

import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

import junit.framework.TestSuite;

import net.callumtaylor.asynchttp.AsyncTest;

/**
 * Test suite
 */
public class Runner extends InstrumentationTestRunner
{
	@Override
	public TestSuite getAllTests()
	{
		InstrumentationTestSuite suite = new InstrumentationTestSuite(this);

		suite.addTestSuite(AsyncTest.class);

		return suite;
	}

	@Override
	public ClassLoader getLoader()
	{
		return Runner.class.getClassLoader();
	}
}
