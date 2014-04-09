#AsyncHttpClient Example Custom Processor

Here is an example custom processor. It's important to remember that you'll be dealing with a buffer of bytes when the response is being read, so you need to make sure to read it into the appropriate format. In this example, because we know the response is going to be JSON, we read the response into a `StringBuffer` and then parse the json after the total amount has been read from the stream.

###Example custom processor

```java
SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");

JsonElement encodedResponse = client.get("api/v1/", new Processor<JsonElement>()
{
	private StringBuffer stringBuffer;

	@Override public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalProcessed, long totalLength)
	{
 		if (stringBuffer == null)
 		{
 			// make sure we're initialising our buffer to make sure we dont run out of memory
			int total = (int)(totalLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : totalLength);
			stringBuffer = new StringBuffer(Math.max(8192, total));
		}

		if (chunk != null)
		{
			try
			{
				stringBuffer.append(new String(chunk, 0, chunkLength, "UTF-8").);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
 		}
	}

	@Override public JsonElement getContent()
	{
		try
		{
 			return new JsonParser().parse(stringBuffer.toString());
 		}
 		catch (Exception e)
 		{
 			return null;
 		}
 		finally
 		{
 			stringBuffer = null;
 		}
	}
});
```