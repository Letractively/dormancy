package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.Dormancy;
import org.springframework.beans.PropertyAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Map;

public abstract class AbstractPropertyAccessorPersister<C, PA extends PropertyAccessor> extends GenericEntityPersister<C> {
	@Inject
	public AbstractPropertyAccessorPersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
	}

	@Nullable
	@Override
	public <T extends C> C clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (dbObj == null || tree.containsKey(dbObj)) {
			return (C) tree.get(dbObj);
		}
		T trObj = createObject(dbObj);
		PA dbPropertyAccessor = getPropertyAccessor(dbObj);
		PA trPopertyAccessor = getPropertyAccessor(trObj);
		for (String name : getPropertyNames(dbPropertyAccessor)) {
			if (dbPropertyAccessor.isReadableProperty(name) && trPopertyAccessor.isWritableProperty(name)) {
				Object dbValue = dbPropertyAccessor.getPropertyValue(name);
				Object trValue = dormancy.clone_(dbValue, tree);
				trPopertyAccessor.setPropertyValue(name, trValue);
			}
		}
		return trObj;
	}

	@Nullable
	@Override
	public <T extends C> C merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || tree.containsKey(trObj)) {
			return (C) tree.get(trObj);
		}
		return merge_(trObj, createObject(trObj), tree);
	}

	@Nullable
	@Override
	public <T extends C> C merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || dbObj == null || tree.containsKey(trObj)) {
			return (C) tree.get(trObj);
		}
		PA dbPropertyAccessor = getPropertyAccessor(dbObj);
		PA trPropertyAccessor = getPropertyAccessor(trObj);
		for (String name : getPropertyNames(trPropertyAccessor)) {
			if (trPropertyAccessor.isReadableProperty(name) && dbPropertyAccessor.isWritableProperty(name)) {
				Object trValue = trPropertyAccessor.getPropertyValue(name);
				Object dbValue = dormancy.merge_(trValue, tree);
				dbPropertyAccessor.setPropertyValue(name, dbValue);
			}
		}
		return dbObj;
	}

	@Nonnull
	protected abstract PA getPropertyAccessor(@Nonnull Object target);

	@Nonnull
	protected abstract String[] getPropertyNames(PA propertyAccessor);
}
