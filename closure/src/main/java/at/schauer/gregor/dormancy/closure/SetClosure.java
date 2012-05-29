package at.schauer.gregor.dormancy.closure;

import java.util.Set;

/**
 * @author Gregor Schauer
 */
public abstract class SetClosure extends CollectionClosure<Set, Set> {
	public SetClosure() {
		super();
	}

	public SetClosure(Set src) {
		super(src);
	}

	@Override
	public abstract void createCollection(Set src);
}
