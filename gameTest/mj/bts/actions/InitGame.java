// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/12/2017 10:39:23
// ******************************************************* 
package bts.actions;

/** ModelAction class created from MMPM action InitGame. */
public class InitGame extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of InitGame. */
	public InitGame(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.actions.execution.InitGame task that is able to run this
	 * task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.actions.execution.InitGame(this, executor, parent);
	}
}