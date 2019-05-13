// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 02/08/2017 21:14:04
// ******************************************************* 
package bts.actions.execution;

/** ExecutionAction class created from MMPM action ComputeRandomClosePosition. */
public class ComputeRandomClosePosition extends
		jbt.execution.task.leaf.action.ExecutionAction {
	/**
	 * Value of the parameter "initialPosition" in case its value is specified
	 * at construction time. null otherwise.
	 */
	private float[] initialPosition;
	/**
	 * Location, in the context, of the parameter "initialPosition" in case its
	 * value is not specified at construction time. null otherwise.
	 */
	private java.lang.String initialPositionLoc;

	/**
	 * Constructor. Constructs an instance of ComputeRandomClosePosition that is
	 * able to run a bts.actions.ComputeRandomClosePosition.
	 * 
	 * @param initialPosition
	 *            value of the parameter "initialPosition", or null in case it
	 *            should be read from the context. If null,
	 *            <code>initialPositionLoc<code> cannot be null.
	 * @param initialPositionLoc
	 *            in case <code>initialPosition</code> is null, this variable
	 *            represents the place in the context where the parameter's
	 *            value will be retrieved from.
	 */
	public ComputeRandomClosePosition(
			bts.actions.ComputeRandomClosePosition modelTask,
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent, float[] initialPosition,
			java.lang.String initialPositionLoc) {
		super(modelTask, executor, parent);

		this.initialPosition = initialPosition;
		this.initialPositionLoc = initialPositionLoc;
	}

	/**
	 * Returns the value of the parameter "initialPosition", or null in case it
	 * has not been specified or it cannot be found in the context.
	 */
	public float[] getInitialPosition() {
		if (this.initialPosition != null) {
			return this.initialPosition;
		} else {
			return (float[]) this.getContext().getVariable(
					this.initialPositionLoc);
		}
	}

	protected void internalSpawn() {
		/*
		 * Do not remove this first line unless you know what it does and you
		 * need not do it.
		 */
		this.getExecutor().requestInsertionIntoList(
				jbt.execution.core.BTExecutor.BTExecutorList.TICKABLE, this);
		/* TODO: this method's implementation must be completed. */
		System.out.println(this.getClass().getCanonicalName() + " spawned");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */
		return jbt.execution.core.ExecutionTask.Status.SUCCESS;
	}

	protected void internalTerminate() {
		/* TODO: this method's implementation must be completed. */
	}

	protected void restoreState(jbt.execution.core.ITaskState state) {
		/* TODO: this method's implementation must be completed. */
	}

	protected jbt.execution.core.ITaskState storeState() {
		/* TODO: this method's implementation must be completed. */
		return null;
	}

	protected jbt.execution.core.ITaskState storeTerminationState() {
		/* TODO: this method's implementation must be completed. */
		return null;
	}
}