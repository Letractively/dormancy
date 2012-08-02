package at.schauer.gregor.dormancy.persister.predicate;

import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.entity.Employee;
import at.schauer.gregor.dormancy.persister.AbstractEntityPersister;
import at.schauer.gregor.dormancy.persister.DirectFieldAccessorPersister;
import at.schauer.gregor.dormancy.persister.PersisterTest;
import at.schauer.gregor.dormancy.persister.PredicatePersister;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.Collections;

import static at.schauer.gregor.dormancy.AbstractDormancyTest.describe;
import static org.junit.Assert.*;

public class AssignablePredicateTest extends PersisterTest<PredicatePersister<Object, AssignablePredicate>> {
	Application app = new Application("app", null, Collections.<Employee>emptySet(), "secret");
	Book book = new Book("Title");

	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		AbstractEntityPersister<Object> delegate = new DirectFieldAccessorPersister<Object>(dormancy);
		AssignablePredicate predicate = new AssignablePredicate(Application.class);
		persister = new PredicatePersister<Object, AssignablePredicate>(delegate, predicate);
	}

	@Test
	public void testGetTypes() {
		AssignablePredicate predicate = persister.getPredicate();
		predicate.setTypes();
		assertArrayEquals(ArrayUtils.EMPTY_CLASS_ARRAY, predicate.getTypes());
		predicate.setTypes(Application.class);
		assertArrayEquals(new Class[] {Application.class}, predicate.getTypes());
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
		Book clone = (Book) persister.clone(book);
		assertSame(book, clone);

		Book merge = (Book) persister.merge(book);
		assertSame(book, merge);
	}
}
