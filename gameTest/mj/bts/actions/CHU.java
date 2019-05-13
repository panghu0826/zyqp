// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/11/2017 09:59:25
// ******************************************************* 
package bts.actions;

/** ModelAction class created from MMPM action CHU. */
public class CHU extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of CHU. */
	public CHU(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/** Returns a bts.actions.execution.CHU task that is able to run this task. */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.actions.execution.CHU(this, executor, parent);
	}
}