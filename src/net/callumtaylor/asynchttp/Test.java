package net.callumtaylor.asynchttp;

import net.callumtaylor.asynchttp.processor.StringProcessor;

public class Test
{
	public static void main(String[] args)
	{
		SyncHttpClient<String> test = new SyncHttpClient<String>("http://google.com");
		String blah = test.get("", new StringProcessor());
		System.out.print(blah);
	}
}