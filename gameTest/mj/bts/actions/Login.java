// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/11/2017 09:59:24
// ******************************************************* 
package bts.actions;

/** ModelAction class created from MMPM action Login. */
public class Login extends jbt.model.task.leaf.action.ModelAction {
	/**
	 * Value of the parameter "userName" in case its value is specified at
	 * construction time. null otherwise.
	 */
	private java.lang.String userName;
	/**
	 * Location, in the context, of the parameter "userName" in case its value
	 * is not specified at construction time. null otherwise.
	 */
	private java.lang.String userNameLoc;
	/**
	 * Value of the parameter "PASSWD" in case its value is specified at
	 * construction time. null otherwise.
	 */
	private java.lang.String PASSWD;
	/**
	 * Location, in the context, of the parameter "PASSWD" in case its value is
	 * not specified at construction time. null otherwise.
	 */
	private java.lang.String PASSWDLoc;

	/**
	 * Constructor. Constructs an instance of Login.
	 * 
	 * @param userName
	 *            value of the parameter "userName", or null in case it should
	 *            be read from the context. If null, <code>userNameLoc</code>
	 *            cannot be null.
	 * @param userNameLoc
	 *            in case <code>userName</code> is null, this variable
	 *            represents the place in the context where the parameter's
	 *            value will be retrieved from.
	 * @param PASSWD
	 *            value of the parameter "PASSWD", or null in case it should be
	 *            read from the context. If null, <code>PASSWDLoc</code> cannot
	 *            be null.
	 * @param PASSWDLoc
	 *            in case <code>PASSWD</code> is null, this variable represents
	 *            the place in the context where the parameter's value will be
	 *            retrieved from.
	 */
	public Login(jbt.model.core.ModelTask guard, java.lang.String userName,
			java.lang.String userNameLoc, java.lang.String PASSWD,
			java.lang.String PASSWDLoc) {
		super(guard);
		this.userName = userName;
		this.userNameLoc = userNameLoc;
		this.PASSWD = PASSWD;
		this.PASSWDLoc = PASSWDLoc;
	}

	/**
	 * Returns a bts.actions.execution.Login task that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.actions.execution.Login(this, executor, parent,
				this.userName, this.userNameLoc, this.PASSWD, this.PASSWDLoc);
	}
}