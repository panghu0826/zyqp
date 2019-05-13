// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 02/11/2017 09:59:25
// ******************************************************* 
package bts.actions.execution;

/** ExecutionAction class created from MMPM action Auth. */
public class Auth extends jbt.execution.task.leaf.action.ExecutionAction {
	/**
	 * Value of the parameter "userId" in case its value is specified at
	 * construction time. null otherwise.
	 */
	private java.lang.Integer userId;
	/**
	 * Location, in the context, of the parameter "userId" in case its value is
	 * not specified at construction time. null otherwise.
	 */
	private java.lang.String userIdLoc;
	/**
	 * Value of the parameter "token" in case its value is specified at
	 * construction time. null otherwise.
	 */
	private java.lang.String token;
	/**
	 * Location, in the context, of the parameter "token" in case its value is
	 * not specified at construction time. null otherwise.
	 */
	private java.lang.String tokenLoc;

	/**
	 * Constructor. Constructs an instance of Auth that is able to run a
	 * bts.actions.Auth.
	 * 
	 * @param userId
	 *            value of the parameter "userId", or null in case it should be
	 *            read from the context. If null,
	 *            <code>userIdLoc<code> cannot be null.
	 * @param userIdLoc
	 *            in case <code>userId</code> is null, this variable represents
	 *            the place in the context where the parameter's value will be
	 *            retrieved from.
	 * @param token
	 *            value of the parameter "token", or null in case it should be
	 *            read from the context. If null,
	 *            <code>tokenLoc<code> cannot be null.
	 * @param tokenLoc
	 *            in case <code>token</code> is null, this variable represents
	 *            the place in the context where the parameter's value will be
	 *            retrieved from.
	 */
	public Auth(bts.actions.Auth modelTask,
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent, java.lang.Integer userId,
			java.lang.String userIdLoc, java.lang.String token,
			java.lang.String tokenLoc) {
		super(modelTask, executor, parent);

		this.userId = userId;
		this.userIdLoc = userIdLoc;
		this.token = token;
		this.tokenLoc = tokenLoc;
	}

	/**
	 * Returns the value of the parameter "userId", or null in case it has not
	 * been specified or it cannot be found in the context.
	 */
	public java.lang.Integer getUserId() {
		if (this.userId != null) {
			return this.userId;
		} else {
			return (java.lang.Integer) this.getContext().getVariable(
					this.userIdLoc);
		}
	}

	/**
	 * Returns the value of the parameter "token", or null in case it has not
	 * been specified or it cannot be found in the context.
	 */
	public java.lang.String getToken() {
		if (this.token != null) {
			return this.token;
		} else {
			return (java.lang.String) this.getContext().getVariable(
					this.tokenLoc);
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
		System.out.println(String.format("开始认证游戏服务器(%s,%s)", getUserId(), getToken()));
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */
		System.out.println(this.getClass().getCanonicalName() + " tick");
		getContext().setVariable("auth", true);
		System.out.println("认证成功,auth=" + true);
		return jbt.execution.core.ExecutionTask.Status.SUCCESS;
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