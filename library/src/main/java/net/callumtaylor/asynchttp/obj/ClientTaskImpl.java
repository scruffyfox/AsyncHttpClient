package net.callumtaylor.asynchttp.obj;

import android.support.annotation.Nullable;

/**
 * Client task implementation interface used for outlining the methods called throughout the lifecycle of a request
 */
public interface ClientTaskImpl<F>
{
	/**
	 * Returns if the request has been cancelled
	 * @return True if the request has been cancelled
	 */
	public boolean isCancelled();

	/**
	 * Initiate a cancellation of the request
	 */
	public void cancel();

	/**
	 * Called before the request has been executed
	 */
	public void preExecute();

	/**
	 * Executes the request task
	 * @return The response from the task. Can be null
	 */
	@Nullable
	public F executeTask();

	/**
	 * Called after the execution of the task
	 */
	public void postExecute();

	/**
	 * Called when a packet transfer has been made
	 * @param packet The data-wrapper with information about the transfer request
	 */
	public void transferProgress(Packet packet);
}
