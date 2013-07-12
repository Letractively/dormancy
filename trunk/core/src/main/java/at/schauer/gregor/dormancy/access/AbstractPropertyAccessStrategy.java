package at.schauer.gregor.dormancy.access;

import org.apache.commons.collections.MapUtils;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Determines how to access entity properties based on various criteria defined by the persistence provider.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public abstract class AbstractPropertyAccessStrategy {
	protected Map<String, AccessMode> propertyAccessTypeMap = new HashMap<String, AccessMode>();

	/**
	 * The access type.
	 */
	public enum AccessMode {
		PROPERTY, FIELD
	}

	protected AccessMode defaultAccessMode;

	/**
	 * Returns the default access mode for the entity type.
	 *
	 * @return the access mode
	 */
	@Nullable
	public AccessMode getDefaultAccessMode() {
		return defaultAccessMode;
	}

	/**
	 * Sets the default access mode for the entity type.
	 *
	 * @param defaultAccessMode the mode to set
	 */
	protected void setDefaultAccessMode(@Nullable AccessMode defaultAccessMode) {
		this.defaultAccessMode = defaultAccessMode;
	}

	/**
	 * Returns the {@link AccessMode} for the named property.
	 *
	 * @param propertyName the name of the property
	 * @return the access mode to use
	 */
	@Nonnull
	public AccessMode getAccessMode(@Nonnull String propertyName) {
		AccessMode accessMode = (AccessMode) MapUtils.getObject(propertyAccessTypeMap, propertyName, null);
		Assert.notNull(accessMode, "Cannot find property named '" + propertyName + "'");
		return accessMode;
	}
}
