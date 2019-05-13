// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 02/11/2017 11:36:27
// ******************************************************* 
package bts.actions.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buding.test.Player;
import com.buding.test.PlayerImpl;

/** ExecutionAction class created from MMPM action Init. */
public class Init extends jbt.execution.task.leaf.action.ExecutionAction {
	private Logger logger = LoggerFactory.getLogger(getClass());
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
	 * Constructor. Constructs an instance of Init that is able to run a
	 * bts.actions.Init.
	 * 
	 * @param serverIp
	 *            value of the parameter "serverIp", or null in case it should
	 *            be read from the context. If null,
	 *            <code>serverIpLoc<code> cannot be null.
	 * @param serverIpLoc
	 *            in case <code>serverIp</code> is null, this variable
	 *            represents the place in the context where the parameter's
	 *            value will be retrieved from.
	 * @param serverPort
	 *            value of the parameter "serverPort", or null in case it should
	 *            be read from the context. If null,
	 *            <code>serverPortLoc<code> cannot be null.
	 * @param serverPortLoc
	 *            in case <code>serverPort</code> is null, this variable
	 *            represents the place in the context where the parameter's
	 *            value will be retrieved from.
	 */
	public Init(bts.actions.Init modelTask,
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent, java.lang.String serverIp,
			java.lang.String serverIpLoc, java.lang.Integer serverPort,
			java.lang.String serverPortLoc) {
		super(modelTask, executor, parent);

		this.serverIp = serverIp;
		this.serverIpLoc = serverIpLoc;
		this.serverPort = serverPort;
		this.serverPortLoc = serverPortLoc;
	}

	/**
	 * Returns the value of the parameter "serverIp", or null in case it has not
	 * been specified or it cannot be found in the context.
	 */
	public java.lang.String getServerIp() {
		if (this.serverIp != null) {
			return this.serverIp;
		} else {
			return (java.lang.String) this.getContext().getVariable(
					this.serverIpLoc);
		}
	}

	/**
	 * Returns the value of the parameter "serverPort", or null in case it has
	 * not been specified or it cannot be found in the context.
	 */
	public java.lang.Integer getServerPort() {
		if (this.serverPort != null) {
			return this.serverPort;
		} else {
			return (java.lang.Integer) this.getContext().getVariable(
					this.serverPortLoc);
		}
	}

	protected void internalSpawn() {
		/*
		 * Do not remove this first line unless you know what it does and you
		 * need not do it.
		 */
		this.getExecutor().requestInsertionIntoList(
				jbt.execution.core.BTExecutor.BTExecutorList.TICKABLE, this);
		/* TODO: this method's implementation must be completed. */
		System.out.println(this.getClass().getCanonicalName() + " spawned");
		Player p = new PlayerImpl();
		getContext().setVariable("player", p);
		p.init(getServerIp(), getServerPort());
		logger.info("初始化大厅服务器");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */
		System.out.println(this.getClass().getCanonicalName() + " tick");
		Player p = (Player)getContext().getVariable("player");
		if(p.isInit()) {
			return jbt.execution.core.ExecutionTask.Status.SUCCESS;	
		}
		logger.info("大厅服务器初始化中....");
		return jbt.execution.core.ExecutionTask.Status.RUNNING;
	}

	protected void internalTerminate() {
		/* TODO: this method's implementation must be completed. */
	}

	protected void restoreState(jbt.execution.core.ITaskState state) {
		/* TODO: this method's implementation must be completed. */
	}

	protected jbt.execution.core.ITaskState storeState() {
		/* TODO: this method's implementation must be completed. */
		return null;
	}

	protected jbt.execution.core.ITaskState storeTerminationState() {
		/* TODO: this method's implementation must be completed. */
		return null;
	}
}