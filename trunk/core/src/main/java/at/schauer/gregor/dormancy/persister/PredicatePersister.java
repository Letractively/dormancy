package at.schauer.gregor.dormancy.persister;

import org.apache.commons.collections.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class PredicatePersister<C, P extends Predicate> extends AbstractEntityPersister<C> {
	protected AbstractEntityPersister<C> delegate;
	protected P predicate;

	public PredicatePersister() {
	}

	public PredicatePersister(AbstractEntityPersister<C> delegate, P predicate) {
		this.delegate = delegate;
		this.predicate = predicate;
	}

	@Override
	public <T extends C> C clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (dbObj == null || tree.containsKey(dbObj)) {
			return (C) tree.get(dbObj);
		}
		return predicate.evaluate(dbObj) ? delegate.clone_(dbObj, tree) : dbObj;
	}

	@Override
	public <T extends C> C merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || tree.containsKey(trObj)) {
			return (C) tree.get(trObj);
		}
		return predicate.evaluate(trObj) ? delegate.merge_(trObj, tree) : trObj;
	}

	@Override
	public <T extends C> C merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || dbObj == null || tree.containsKey(trObj)) {
			return (C) tree.get(trObj);
		}
		return predicate.evaluate(trObj) ? delegate.merge_(trObj, dbObj, tree) : trObj;
	}

	public void setDelegate(AbstractEntityPersister<C> delegate) {
		this.delegate = delegate;
	}

	public void setPredicate(P predicate) {
		this.predicate = predicate;
	}

	public P getPredicate() {
		return predicate;
	}
}
