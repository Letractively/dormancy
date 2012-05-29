package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.entity.CollectionEntity;
import org.hibernate.Session;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 */
public class MapPersisterTest extends PersisterTest<MapPersister> {
	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		persister = new MapPersister(dormancy);
		persister.setSessionFactory(sessionFactory);
	}

	@Test
	public void testEntity() {
		Session session = sessionFactory.getCurrentSession();
		CollectionEntity a = (CollectionEntity) session.get(CollectionEntity.class, 1L);
		assertEquals(true, AbstractDormancyTest.isManaged(a.getLongMap(), session));
		assertEquals(false, AbstractDormancyTest.isManaged(a.getLongMap().keySet().iterator().next(), session));
		assertEquals(false, AbstractDormancyTest.isManaged(a.getLongMap().values().iterator().next(), session));

		Map clone = (Map) persister.clone(a.getLongMap());
		assertEquals(false, AbstractDormancyTest.isManaged(clone, session));
		assertEquals(false, AbstractDormancyTest.isManaged(clone.keySet().iterator().next(), session));
		assertEquals(false, AbstractDormancyTest.isManaged(clone.values().iterator().next(), session));

		Map merge = (Map) persister.merge(clone);
		assertEquals(false, AbstractDormancyTest.isManaged(merge, session));
		assertEquals(false, AbstractDormancyTest.isManaged(merge.keySet().iterator().next(), session));
		assertEquals(false, AbstractDormancyTest.isManaged(merge.values().iterator().next(), session));

		merge = (Map) persister.merge(clone, a.getLongMap());
		assertEquals(true, AbstractDormancyTest.isManaged(merge, session));
		assertEquals(false, AbstractDormancyTest.isManaged(merge.keySet().iterator().next(), session));
		assertEquals(false, AbstractDormancyTest.isManaged(merge.values().iterator().next(), session));
	}

	@Test
	public void testDeleteFromMap() {
		dormancy.getConfig().setDeleteRemovedEntities(true);
		Session session = sessionFactory.getCurrentSession();

		CollectionEntity a = (CollectionEntity) session.get(CollectionEntity.class, 1L);
		Long bookId = a.getBookMap().values().iterator().next().getId();

		a = dormancy.clone(a);
		assertEquals(1, a.getBookMap().size());

		a.getBookMap().clear();
		a = dormancy.merge(a);
		assertEquals(0, a.getBookMap().size());

		a = (CollectionEntity) session.get(CollectionEntity.class, 1L);
		assertEquals(0, a.getBookMap().size());

		assertEquals(null, session.get(Book.class, bookId));
	}
}
