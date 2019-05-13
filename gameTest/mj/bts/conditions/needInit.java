// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/11/2017 11:38:21
// ******************************************************* 
package bts.conditions;

/** ModelCondition class created from MMPM condition needInit. */
public class needInit extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of needInit. */
	public needInit(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.conditions.execution.needInit task that is able to run this
	 * task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.conditions.execution.needInit(this, executor, parent);
	}
}