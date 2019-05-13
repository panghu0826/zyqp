// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/12/2017 10:39:23
// ******************************************************* 
package bts.actions;

/** ModelAction class created from MMPM action InitMsg. */
public class InitMsg extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of InitMsg. */
	public InitMsg(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.actions.execution.InitMsg task that is able to run this
	 * task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.actions.execution.InitMsg(this, executor, parent);
	}
}