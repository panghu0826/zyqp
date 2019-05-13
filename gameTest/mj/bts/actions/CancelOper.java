// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/11/2017 09:59:25
// ******************************************************* 
package bts.actions;

/** ModelAction class created from MMPM action CancelOper. */
public class CancelOper extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of CancelOper. */
	public CancelOper(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.actions.execution.CancelOper task that is able to run this
	 * task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.actions.execution.CancelOper(this, executor, parent);
	}
}