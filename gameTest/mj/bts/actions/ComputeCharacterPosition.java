// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/08/2017 21:14:04
// ******************************************************* 
package bts.actions;

/** ModelAction class created from MMPM action ComputeCharacterPosition. */
public class ComputeCharacterPosition extends
		jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of ComputeCharacterPosition. */
	public ComputeCharacterPosition(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.actions.execution.ComputeCharacterPosition task that is
	 * able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.actions.execution.ComputeCharacterPosition(this,
				executor, parent);
	}
}