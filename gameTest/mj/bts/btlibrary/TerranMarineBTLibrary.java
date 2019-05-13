// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 02/12/2017 11:29:28
// ******************************************************* 
package bts.btlibrary;

/**
 * BT library that includes the trees read from the following files:
 * <ul>
 * <li>C:/workspace3/ddz.git/mj/poker_Server/gameTest/mj\StandardPatrol.xbt</li>
 * <li>C:/workspace3/ddz.git/mj/poker_Server/gameTest/mj\TerranMarine.xbt</li>
 * <li>C:/workspace3/ddz.git/mj/poker_Server/gameTest/mj\Poker.xbt</li>
 * <li>C:/workspace3/ddz.git/mj/poker_Server/gameTest/mj\Login.xbt</li>
 * <li>C:/workspace3/ddz.git/mj/poker_Server/gameTest/mj\Gaming.xbt</li>
 * </ul>
 */
public class TerranMarineBTLibrary implements jbt.execution.core.IBTLibrary {
	/**
	 * Tree generated from file
	 * C:/workspace3/ddz.git/mj/poker_Server/gameTest/mj\StandardPatrol.xbt.
	 */
	private static jbt.model.core.ModelTask StandardPatrol;
	/**
	 * Tree generated from file
	 * C:/workspace3/ddz.git/mj/poker_Server/gameTest/mj\TerranMarine.xbt.
	 */
	private static jbt.model.core.ModelTask TerranMarine;
	/**
	 * Tree generated from file
	 * C:/workspace3/ddz.git/mj/poker_Server/gameTest/mj\Poker.xbt.
	 */
	private static jbt.model.core.ModelTask Poker;
	/**
	 * Tree generated from file
	 * C:/workspace3/ddz.git/mj/poker_Server/gameTest/mj\Login.xbt.
	 */
	private static jbt.model.core.ModelTask Login;
	/**
	 * Tree generated from file
	 * C:/workspace3/ddz.git/mj/poker_Server/gameTest/mj\Gaming.xbt.
	 */
	private static jbt.model.core.ModelTask Gaming;

	/* Static initialization of all the trees. */
	static {
		StandardPatrol = new jbt.model.task.composite.ModelSequence(
				null,
				new bts.actions.ComputeCharacterPosition(null),
				new jbt.model.task.decorator.ModelRepeat(
						null,
						new jbt.model.task.composite.ModelSequence(
								null,
								new bts.actions.ComputeRandomClosePosition(
										null, null, "CharacterPosition"),
								new bts.actions.AttackMove(null, null, "target"))));

		TerranMarine = new jbt.model.task.decorator.ModelRepeat(
				null,
				new jbt.model.task.composite.ModelDynamicPriorityList(
						null,
						new bts.actions.Attack(new bts.conditions.HighDanger(
								null), null, "ss"),
						new jbt.model.task.composite.ModelSequence(
								new bts.conditions.LowDanger(null),
								new bts.actions.ComputeClosestBasePosition(null),
								new bts.actions.Move(
										new bts.conditions.LowDanger(null),
										null, "abc")),
						new jbt.model.task.leaf.ModelSubtreeLookup(null,
								"StandardPatrol")));

		Poker = new jbt.model.task.decorator.ModelRepeat(null,
				new jbt.model.task.composite.ModelDynamicPriorityList(null,
						new bts.actions.Init(new bts.conditions.needInit(null),
								null, "serverIp", null, "serverPort"),
						new jbt.model.task.leaf.ModelSubtreeLookup(
								new bts.conditions.needAuth(null), "Login"),
						new jbt.model.task.leaf.ModelSubtreeLookup(null,
								"Gaming")));

		Login = new jbt.model.task.composite.ModelDynamicPriorityList(
				null,
				new bts.actions.Login(new bts.conditions.needLogin(null), null,
						"userName", null, "password"),
				new bts.actions.InitMsg(new bts.conditions.needInitMsg(null)),
				new bts.actions.MsgAuth(new bts.conditions.needAuthMsg(null)),
				new bts.actions.InitGame(new bts.conditions.needInitGame(null)),
				new bts.actions.GameAuth(new bts.conditions.needAuthGame(null)));

		Gaming = new jbt.model.task.composite.ModelDynamicPriorityList(null,
				new bts.actions.Enroll(new bts.conditions.needEnroll(null)),
				new bts.actions.Ready(new bts.conditions.needReady(null)),
				new bts.actions.CHU(null));

	}

	/**
	 * Returns a behaviour tree by its name, or null in case it cannot be found.
	 * It must be noted that the trees that are retrieved belong to the class,
	 * not to the instance (that is, the trees are static members of the class),
	 * so they are shared among all the instances of this class.
	 */
	public jbt.model.core.ModelTask getBT(java.lang.String name) {
		if (name.equals("StandardPatrol")) {
			return StandardPatrol;
		}
		if (name.equals("TerranMarine")) {
			return TerranMarine;
		}
		if (name.equals("Poker")) {
			return Poker;
		}
		if (name.equals("Login")) {
			return Login;
		}
		if (name.equals("Gaming")) {
			return Gaming;
		}
		return null;
	}

	/**
	 * Returns an Iterator that is able to iterate through all the elements in
	 * the library. It must be noted that the iterator does not support the
	 * "remove()" operation. It must be noted that the trees that are retrieved
	 * belong to the class, not to the instance (that is, the trees are static
	 * members of the class), so they are shared among all the instances of this
	 * class.
	 */
	public java.util.Iterator<jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask>> iterator() {
		return new BTLibraryIterator();
	}

	private class BTLibraryIterator
			implements
			java.util.Iterator<jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask>> {
		static final long numTrees = 5;
		long currentTree = 0;

		public boolean hasNext() {
			return this.currentTree < numTrees;
		}

		public jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask> next() {
			this.currentTree++;

			if ((this.currentTree - 1) == 0) {
				return new jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask>(
						"StandardPatrol", StandardPatrol);
			}

			if ((this.currentTree - 1) == 1) {
				return new jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask>(
						"TerranMarine", TerranMarine);
			}

			if ((this.currentTree - 1) == 2) {
				return new jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask>(
						"Poker", Poker);
			}

			if ((this.currentTree - 1) == 3) {
				return new jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask>(
						"Login", Login);
			}

			if ((this.currentTree - 1) == 4) {
				return new jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask>(
						"Gaming", Gaming);
			}

			throw new java.util.NoSuchElementException();
		}

		public void remove() {
			throw new java.lang.UnsupportedOperationException();
		}
	}
}
