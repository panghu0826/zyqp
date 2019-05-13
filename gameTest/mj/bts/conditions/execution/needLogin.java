// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 02/11/2017 20:47:57
// ******************************************************* 
package bts.conditions.execution;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import com.buding.test.Player;

/** ExecutionCondition class created from MMPM condition needLogin. */
public class needLogin extends
		jbt.execution.task.leaf.condition.ExecutionCondition {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor. Constructs an instance of needLogin that is able to run a
	 * bts.conditions.needLogin.
	 */
	public needLogin(bts.conditions.needLogin modelTask,
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		super(modelTask, executor, parent);

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
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */
		System.out.println(this.getClass().getCanonicalName() + " tick");
		Player obj = (Player)getContext().getVariable("player");
		if(obj.isLogin() == false) {
			System.out.println("未登录");
			return jbt.execution.core.ExecutionTask.Status.SUCCESS;
		}
//		logger.info("大厅服务器已登录");
		return jbt.execution.core.ExecutionTask.Status.FAILURE;
		
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