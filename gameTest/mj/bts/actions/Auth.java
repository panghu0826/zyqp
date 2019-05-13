// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/11/2017 09:59:25
// ******************************************************* 
package bts.actions;

/** ModelAction class created from MMPM action Auth. */
public class Auth extends jbt.model.task.leaf.action.ModelAction {
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
	 * Constructor. Constructs an instance of Auth.
	 * 
	 * @param userId
	 *            value of the parameter "userId", or null in case it should be
	 *            read from the context. If null, <code>userIdLoc</code> cannot
	 *            be null.
	 * @param userIdLoc
	 *            in case <code>userId</code> is null, this variable represents
	 *            the place in the context where the parameter's value will be
	 *            retrieved from.
	 * @param token
	 *            value of the parameter "token", or null in case it should be
	 *            read from the context. If null, <code>tokenLoc</code> cannot
	 *            be null.
	 * @param tokenLoc
	 *            in case <code>token</code> is null, this variable represents
	 *            the place in the context where the parameter's value will be
	 *            retrieved from.
	 */
	public Auth(jbt.model.core.ModelTask guard, java.lang.Integer userId,
			java.lang.String userIdLoc, java.lang.String token,
			java.lang.String tokenLoc) {
		super(guard);
		this.userId = userId;
		this.userIdLoc = userIdLoc;
		this.token = token;
		this.tokenLoc = tokenLoc;
	}

	/** Returns a bts.actions.execution.Auth task that is able to run this task. */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.actions.execution.Auth(this, executor, parent,
				this.userId, this.userIdLoc, this.token, this.tokenLoc);
	}
}