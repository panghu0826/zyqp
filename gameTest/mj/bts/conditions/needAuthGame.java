// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/12/2017 09:25:42
// ******************************************************* 
package bts.conditions;

/** ModelCondition class created from MMPM condition needAuthGame. */
public class needAuthGame extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of needAuthGame. */
	public needAuthGame(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.conditions.execution.needAuthGame task that is able to run
	 * this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.conditions.execution.needAuthGame(this, executor, parent);
	}
}