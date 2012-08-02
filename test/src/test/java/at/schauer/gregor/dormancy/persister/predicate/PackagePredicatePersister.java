package at.schauer.gregor.dormancy.persister.predicate;

import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.entity.Employee;
import at.schauer.gregor.dormancy.persister.AbstractEntityPersister;
import at.schauer.gregor.dormancy.persister.DirectFieldAccessorPersister;
import at.schauer.gregor.dormancy.persister.PersisterTest;
import at.schauer.gregor.dormancy.persister.PredicatePersister;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Date;

import static at.schauer.gregor.dormancy.AbstractDormancyTest.describe;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class PackagePredicatePersister extends PersisterTest<PredicatePersister<Object, PackagePredicate>> {
	Application app = new Application("app", null, Collections.<Employee>emptySet(), "secret");

	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		AbstractEntityPersister<Object> delegate = new DirectFieldAccessorPersister<Object>(dormancy);
		PackagePredicate predicate = new PackagePredicate(Application.class.getPackage().getName());
		persister = new PredicatePersister<Object, PackagePredicate>(delegate, predicate);
	}

	@Test
	public void testMatch() {
		Application clone = (Application) persister.clone(app);
		assertNotSame(app, clone);
		assertEquals(describe(app), describe(clone));

		Application merge = (Application) persister.merge(app);
		assertNotSame(app, merge);
		assertEquals(describe(app), describe(merge));
	}

	@Test
	public void testNotMatch() {
		Date date = new Date();
		Date clone = (Date) persister.clone(date);
		assertSame(date, clone);

		Date merge = (Date) persister.merge(date);
		assertSame(date, merge);
	}
}
