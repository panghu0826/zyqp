// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 02/11/2017 20:47:56
// ******************************************************* 
package bts.actions.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buding.test.Player;

/** ExecutionAction class created from MMPM action Login. */
public class Login extends jbt.execution.task.leaf.action.ExecutionAction {
	private Logger logger = LoggerFactory.getLogger(getClass());
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
	 * Value of the parameter "password" in case its value is specified at
	 * construction time. null otherwise.
	 */
	private java.lang.String password;
	/**
	 * Location, in the context, of the parameter "password" in case its value
	 * is not specified at construction time. null otherwise.
	 */
	private java.lang.String passwordLoc;

	/**
	 * Constructor. Constructs an instance of Login that is able to run a
	 * bts.actions.Login.
	 * 
	 * @param userName
	 *            value of the parameter "userName", or null in case it should
	 *            be read from the context. If null,
	 *            <code>userNameLoc<code> cannot be null.
	 * @param userNameLoc
	 *            in case <code>userName</code> is null, this variable
	 *            represents the place in the context where the parameter's
	 *            value will be retrieved from.
	 * @param password
	 *            value of the parameter "password", or null in case it should
	 *            be read from the context. If null,
	 *            <code>passwordLoc<code> cannot be null.
	 * @param passwordLoc
	 *            in case <code>password</code> is null, this variable
	 *            represents the place in the context where the parameter's
	 *            value will be retrieved from.
	 */
	public Login(bts.actions.Login modelTask,
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent, java.lang.String userName,
			java.lang.String userNameLoc, java.lang.String password,
			java.lang.String passwordLoc) {
		super(modelTask, executor, parent);

		this.userName = userName;
		this.userNameLoc = userNameLoc;
		this.password = password;
		this.passwordLoc = passwordLoc;
	}

	/**
	 * Returns the value of the parameter "userName", or null in case it has not
	 * been specified or it cannot be found in the context.
	 */
	public java.lang.String getUserName() {
		if (this.userName != null) {
			return this.userName;
		} else {
			return (java.lang.String) this.getContext().getVariable(
					this.userNameLoc);
		}
	}

	/**
	 * Returns the value of the parameter "password", or null in case it has not
	 * been specified or it cannot be found in the context.
	 */
	public java.lang.String getPassword() {
		if (this.password != null) {
			return this.password;
		} else {
			return (java.lang.String) this.getContext().getVariable(
					this.passwordLoc);
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
		logger.info(String.format("开始登录大厅服务器(%s,%s)", getUserName(), getPassword()));
		Player obj = (Player)getContext().getVariable("player");
		obj.login(getUserName(), getPassword());
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */
		Player obj = (Player)getContext().getVariable("player");
		if(obj.isLogin()) {
			logger.info("大厅服务器登录成功");
			return jbt.execution.core.ExecutionTask.Status.SUCCESS;	
		}
		logger.info("大厅服务器登录中");
		return jbt.execution.core.ExecutionTask.Status.RUNNING;
//		String token = "t12323";
//		getContext().setVariable("token", token);
//		getContext().setVariable("userId", 12323);
//		System.out.println("登录成功,token=" + token);
		
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