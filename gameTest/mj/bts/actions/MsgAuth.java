// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/12/2017 09:25:34
// ******************************************************* 
package bts.actions;

/** ModelAction class created from MMPM action MsgAuth. */
public class MsgAuth extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of MsgAuth. */
	public MsgAuth(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a bts.actions.execution.MsgAuth task that is able to run this
	 * task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.actions.execution.MsgAuth(this, executor, parent);
	}
}