// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/08/2017 21:14:04
// ******************************************************* 
package bts.conditions;

/** ModelCondition class created from MMPM condition LowDanger. */
public class LowDanger extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of LowDanger. */
	public LowDanger(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.conditions.execution.LowDanger task that is able to run
	 * this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.conditions.execution.LowDanger(this, executor, parent);
	}
}