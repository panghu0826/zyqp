// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 02/12/2017 10:39:23
// ******************************************************* 
package bts.actions.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buding.test.Player;

/** ExecutionAction class created from MMPM action InitGame. */
public class InitGame extends jbt.execution.task.leaf.action.ExecutionAction {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor. Constructs an instance of InitGame that is able to run a
	 * bts.actions.InitGame.
	 */
	public InitGame(bts.actions.InitGame modelTask,
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
		logger.info("初始化游戏服");
		Player p = (Player)getContext().getVariable("player");
		p.initGame();
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */
		Player p = (Player)getContext().getVariable("player");
		if(p.isGameInit()) {
			return jbt.execution.core.ExecutionTask.Status.SUCCESS;	
		}
		logger.info("游戏服初始化中....");
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