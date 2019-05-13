// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/11/2017 09:59:25
// ******************************************************* 
package bts.actions;

/** ModelAction class created from MMPM action Enroll. */
public class Enroll extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of Enroll. */
	public Enroll(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.actions.execution.Enroll task that is able to run this
	 * task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.actions.execution.Enroll(this, executor, parent);
	}
}