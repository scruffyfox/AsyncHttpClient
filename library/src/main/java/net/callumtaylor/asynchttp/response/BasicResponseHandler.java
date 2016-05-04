package net.callumtaylor.asynchttp.response;

public class BasicResponseHandler extends ResponseHandler<Void>
{
	/**
	 * Dummy implementation method
	 */
	@Override public void generateContent()
	{
	}

	/**
	 * @return {@link Void}
	 */
	@Override public Void getContent()
	{
		return null;
	}
}
