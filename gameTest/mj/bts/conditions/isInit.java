// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/11/2017 11:36:27
// ******************************************************* 
package bts.conditions;

/** ModelCondition class created from MMPM condition isInit. */
public class isInit extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of isInit. */
	public isInit(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.conditions.execution.isInit task that is able to run this
	 * task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.conditions.execution.isInit(this, executor, parent);
	}
}