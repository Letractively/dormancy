package at.schauer.gregor.dormancy.persister.predicate;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssignablePredicate implements Predicate {
	protected Class[] types;

	public AssignablePredicate(@Nullable Class... types) {
		this.types = types;
	}

	@Override
	public boolean evaluate(@Nonnull Object object) {
		Class<?> clazz = object.getClass();
		for (Class<?> supportedType : types) {
			if (ClassUtils.isAssignable(supportedType, clazz)) {
				return true;
			}
		}
		return false;
	}

	@Nonnull
	public Class[] getTypes() {
		return types != null ? types : ArrayUtils.EMPTY_CLASS_ARRAY;
	}

	public void setTypes(@Nullable Class... types) {
		this.types = types;
	}
}
