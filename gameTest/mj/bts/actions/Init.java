// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/11/2017 11:36:26
// ******************************************************* 
package bts.actions;

/** ModelAction class created from MMPM action Init. */
public class Init extends jbt.model.task.leaf.action.ModelAction {
	/**
	 * Value of the parameter "serverIp" in case its value is specified at
	 * construction time. null otherwise.
	 */
	private java.lang.String serverIp;
	/**
	 * Location, in the context, of the parameter "serverIp" in case its value
	 * is not specified at construction time. null otherwise.
	 */
	private java.lang.String serverIpLoc;
	/**
	 * Value of the parameter "serverPort" in case its value is specified at
	 * construction time. null otherwise.
	 */
	private java.lang.Integer serverPort;
	/**
	 * Location, in the context, of the parameter "serverPort" in case its value
	 * is not specified at construction time. null otherwise.
	 */
	private java.lang.String serverPortLoc;

	/**
	 * Constructor. Constructs an instance of Init.
	 * 
	 * @param serverIp
	 *            value of the parameter "serverIp", or null in case it should
	 *            be read from the context. If null, <code>serverIpLoc</code>
	 *            cannot be null.
	 * @param serverIpLoc
	 *            in case <code>serverIp</code> is null, this variable
	 *            represents the place in the context where the parameter's
	 *            value will be retrieved from.
	 * @param serverPort
	 *            value of the parameter "serverPort", or null in case it should
	 *            be read from the context. If null, <code>serverPortLoc</code>
	 *            cannot be null.
	 * @param serverPortLoc
	 *            in case <code>serverPort</code> is null, this variable
	 *            represents the place in the context where the parameter's
	 *            value will be retrieved from.
	 */
	public Init(jbt.model.core.ModelTask guard, java.lang.String serverIp,
			java.lang.String serverIpLoc, java.lang.Integer serverPort,
			java.lang.String serverPortLoc) {
		super(guard);
		this.serverIp = serverIp;
		this.serverIpLoc = serverIpLoc;
		this.serverPort = serverPort;
		this.serverPortLoc = serverPortLoc;
	}

	/** Returns a bts.actions.execution.Init task that is able to run this task. */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.actions.execution.Init(this, executor, parent,
				this.serverIp, this.serverIpLoc, this.serverPort,
				this.serverPortLoc);
	}
}