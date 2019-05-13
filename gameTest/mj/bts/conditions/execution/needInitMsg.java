// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 02/12/2017 10:39:23
// ******************************************************* 
package bts.conditions.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buding.test.Player;

/** ExecutionCondition class created from MMPM condition needInitMsg. */
public class needInitMsg extends
		jbt.execution.task.leaf.condition.ExecutionCondition {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor. Constructs an instance of needInitMsg that is able to run a
	 * bts.conditions.needInitMsg.
	 */
	public needInitMsg(bts.conditions.needInitMsg modelTask,
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
		Player obj = (Player)getContext().getVariable("player");
		if(obj == null || obj.isMsgInit() == false) {
			logger.info("需要初始化消息服务器");
			return jbt.execution.core.ExecutionTask.Status.SUCCESS; 
		}
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