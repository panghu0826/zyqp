// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/12/2017 09:25:41
// ******************************************************* 
package bts.actions;

/** ModelAction class created from MMPM action GameAuth. */
public class GameAuth extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of GameAuth. */
	public GameAuth(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.actions.execution.GameAuth task that is able to run this
	 * task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.actions.execution.GameAuth(this, executor, parent);
	}
}