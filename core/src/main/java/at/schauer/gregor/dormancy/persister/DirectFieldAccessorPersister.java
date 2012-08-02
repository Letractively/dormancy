package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.Dormancy;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class DirectFieldAccessorPersister<C> extends AbstractPropertyAccessorPersister<C, ConfigurablePropertyAccessor> {
	@Inject
	public DirectFieldAccessorPersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
	}

	@Nonnull
	@Override
	protected ConfigurablePropertyAccessor getPropertyAccessor(@Nonnull Object target) {
		return PropertyAccessorFactory.forDirectFieldAccess(target);
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	protected String[] getPropertyNames(ConfigurablePropertyAccessor propertyAccessor) {
		Map<String, Field> fieldMap = (Map<String, Field>) ReflectionTestUtils.getField(propertyAccessor, "fieldMap");
		Set<String> names = fieldMap.keySet();
		return names.toArray(new String[names.size()]);
	}
}
