#AsyncHttpClient Example Custom Handler

Here is an example custom response handler. It's important to remember that you'll be dealing with a buffer of bytes when the response is being read, so you need to make sure to read it into the appropriate format. In this example, because we know the response is going to be JSON, we read the response into a `StringBuffer` and then parse the json after the total amount has been read from the stream.

###Example custom handler

```java
AsyncHttpClient client = new AyncHttpClient("http://example.com");

client.get("api/v1/", new ResponseHandler<String>()
{
	private StringBuffer stringBuffer;
	private String content;

	@Override public void onByteChunkReceived(byte[] chunk, long chunkLength, long totalProcessed, long totalLength)
	{
		if (stringBuffer == null)
		{
			int total = (int)(totalLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : totalLength);
			stringBuffer = new StringBuffer(Math.max(8192, total));
		}

		if (chunk != null)
		{
			try
			{
				stringBuffer.append(new String(chunk, 0, (int)chunkLength, "UTF-8"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Generate the String from the buffer and remove it to allow the GC to clean up properly
	 */
	@Override public void generateContent()
	{
		this.content = stringBuffer.toString();
		this.stringBuffer = null;
	}

	/**
	 * @return The data represented as a String
	 */
	@Override public String getContent()
	{
		return content;
	}
});
```
