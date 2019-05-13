// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/08/2017 21:14:04
// ******************************************************* 
package bts.conditions;

/** ModelCondition class created from MMPM condition HighDanger. */
public class HighDanger extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of HighDanger. */
	public HighDanger(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.conditions.execution.HighDanger task that is able to run
	 * this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.conditions.execution.HighDanger(this, executor, parent);
	}
}