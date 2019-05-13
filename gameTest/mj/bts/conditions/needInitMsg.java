// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/12/2017 10:39:23
// ******************************************************* 
package bts.conditions;

/** ModelCondition class created from MMPM condition needInitMsg. */
public class needInitMsg extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of needInitMsg. */
	public needInitMsg(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.conditions.execution.needInitMsg task that is able to run
	 * this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.conditions.execution.needInitMsg(this, executor, parent);
	}
}