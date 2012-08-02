package at.schauer.gregor.dormancy.persister.predicate;

import org.apache.commons.collections.Predicate;

import javax.annotation.Nonnull;

public class PackagePredicate implements Predicate {
	protected String packageName;

	public PackagePredicate(@Nonnull String packageName) {
		this.packageName = packageName.endsWith(".") ? packageName : packageName + '.';
	}

	@Override
	public boolean evaluate(@Nonnull Object object) {
		return object.getClass().getName().startsWith(packageName);
	}
}
