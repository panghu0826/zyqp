// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 02/12/2017 09:25:41
// ******************************************************* 
package bts.actions.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buding.test.Player;

/** ExecutionAction class created from MMPM action MsgAuth. */
public class MsgAuth extends jbt.execution.task.leaf.action.ExecutionAction {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor. Constructs an instance of MsgAuth that is able to run a
	 * bts.actions.MsgAuth.
	 */
	public MsgAuth(bts.actions.MsgAuth modelTask,
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
		logger.info("开始认证消息服务器");
		Player player = (Player)getContext().getVariable("player");
		player.authMsg();
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */
		Player player = (Player)getContext().getVariable("player");
		if(player.isMsgAuth() == false) {		
			logger.info("认证消息服务器成功");
			return jbt.execution.core.ExecutionTask.Status.SUCCESS;
		}
		logger.info("消息服务器认证中......");
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