package net.callumtaylor.asynchttp.obj;

/**
 *
 */
public interface ClientTaskImpl<F>
{
	public boolean isCancelled();
	public void cancel();
	public void preExecute();
	public F executeTask();
	public void postExecute();

	public void transferProgress(Packet... values);
}
