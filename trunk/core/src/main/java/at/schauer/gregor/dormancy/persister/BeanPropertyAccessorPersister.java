package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.Dormancy;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.beans.PropertyDescriptor;

public class BeanPropertyAccessorPersister<C> extends AbstractPropertyAccessorPersister<C, BeanWrapper> {
	@Inject
	public BeanPropertyAccessorPersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
	}

	@Nonnull
	@Override
	protected BeanWrapper getPropertyAccessor(@Nonnull Object target) {
		return PropertyAccessorFactory.forBeanPropertyAccess(target);
	}

	@Nonnull
	@Override
	protected String[] getPropertyNames(BeanWrapper propertyAccessor) {
		PropertyDescriptor[] descriptors = propertyAccessor.getPropertyDescriptors();
		String[] names = new String[descriptors.length];
		for (int i = 0; i < descriptors.length; i++) {
			PropertyDescriptor descriptor = descriptors[i];
			names[i] = descriptor.getName();
		}
		return names;
	}
}
