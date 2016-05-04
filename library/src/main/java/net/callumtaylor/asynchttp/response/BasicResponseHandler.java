package net.callumtaylor.asynchttp.response;

/**
 * Basic response handler. Useful for fire-and-forget requests
 */
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
