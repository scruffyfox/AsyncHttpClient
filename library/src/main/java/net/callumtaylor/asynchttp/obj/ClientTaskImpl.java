package net.callumtaylor.asynchttp.obj;

/**
 *
 */
public interface ClientTaskImpl<F>
{
	public boolean isCancelled();
	public void cancel();
	public void onPreExecute();
	public F doInBackground();
	public void onPostExecute();
	public void onProgressUpdate(Packet... values);
	public void postPublishProgress(Packet... values);
}
