package at.schauer.gregor.dormancy.closure;

import java.util.Collection;

/**
 * @author Gregor Schauer
 */
public abstract class CollectionClosure<S extends Collection, T extends Collection> extends DelegateClosure<T> {
	public CollectionClosure() {
	}

	public CollectionClosure(S src) {
		createCollection(src);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute(Object input) {
		Collection collection = Collection.class.cast(input);
		for (Object element : collection) {
			delegate.execute(element);
			getResult().add(delegate.getResult());
		}
	}

	public abstract void createCollection(S src);
}
