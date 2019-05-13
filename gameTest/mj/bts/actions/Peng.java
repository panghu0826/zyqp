// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/11/2017 09:59:25
// ******************************************************* 
package bts.actions;

/** ModelAction class created from MMPM action Peng. */
public class Peng extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of Peng. */
	public Peng(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/** Returns a bts.actions.execution.Peng task that is able to run this task. */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new bts.actions.execution.Peng(this, executor, parent);
	}
}