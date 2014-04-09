#AsyncHttpClient Example Custom Handler

Here is an example custom response handler. It's important to remember that you'll be dealing with a buffer of bytes when the response is being read, so you need to make sure to read it into the appropriate format. In this example, because we know the response is going to be JSON, we read the response into a `StringBuffer` and then parse the json after the total amount has been read from the stream.

###Example custom handler

```java
AsyncHttpClient client = new AyncHttpClient("http://example.com");

client.get("api/v1/", new AsyncHttpResponseHandler()
{
	private StringBuffer stringBuffer;
	private JsonElement content;

	@Override public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalProcessed, long totalLength)
	{
		if (stringBuffer == null)
		{
			int total = (int)(totalLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : totalLength);
			stringBuffer = new StringBuffer(Math.max(total, 1024 * 8));
		}

		if (chunk != null)
		{
			try
			{
				stringBuffer.append(new String(chunk, 0, chunkLength, "UTF-8"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override public void generateContent()
	{
		try
		{
			this.content = new JsonParser().parse(stringBuffer.toString());
			this.stringBuffer = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Processes the response from the stream.
	 * This is <b>not</b> ran on the UI thread
	 *
	 * @return The data represented as a gson JsonElement primitive type
	 */
	@Override public JsonElement getContent()
	{
		return content
	}
});
```