// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/11/2017 09:59:25
// ******************************************************* 
package bts.conditions;

/** ModelCondition class created from MMPM condition needLogin. */
public class needLogin extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of needLogin. */
	public needLogin(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.conditions.execution.needLogin task that is able to run
	 * this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.conditions.execution.needLogin(this, executor, parent);
	}
}