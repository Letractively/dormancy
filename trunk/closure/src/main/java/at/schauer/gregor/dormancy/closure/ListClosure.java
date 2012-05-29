package at.schauer.gregor.dormancy.closure;

import org.springframework.core.CollectionFactory;

import java.util.List;

/**
 * @author Gregor Schauer
 */
public abstract class ListClosure extends CollectionClosure<List, List> {
	public ListClosure() {
	}

	public ListClosure(List src) {
		super(src);
	}

	@Override
	public void createCollection(List src) {
		result = List.class.cast(CollectionFactory.createApproximateCollection(src, src.size()));
	}
}
