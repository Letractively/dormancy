package at.schauer.gregor.dormancy.test;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.Dormancy;
import at.schauer.gregor.dormancy.container.Team;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.entity.Employee;
import at.schauer.gregor.dormancy.persister.CollectionPersister;
import at.schauer.gregor.dormancy.persister.TeamPersister;
import org.hibernate.collection.PersistentSet;
import org.junit.Test;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * @author Gregor Schauer
 */
public class HibernateEndpointDormancyTest extends AbstractDormancyTest implements ApplicationContextAware {
	@Test
	public void testPersisterTypeHibernateEndpoint() {
		dormancy.getPersisterMap().clear();
		dormancy.getPersisterMap().put(TeamPersister.class, new TeamPersister(dormancy));
		dormancy.getPersisterMap().put(List.class, new CollectionPersister<List>(dormancy));

		sessionFactory.getCurrentSession().save(new Book(UUID.randomUUID().toString()));
		Team team = new Team(service.load(Employee.class, 1L));

		Team pass = service.next(team);
		assertSame(LinkedHashSet.class, pass.getEmployees().get(0).getEmployees().getClass());

		pass = service.prev(team);
		assertSame(PersistentSet.class, pass.getEmployees().get(0).getEmployees().getClass());
	}

	@Test
	public void testPersisterNameHibernateEndpoint() {
		dormancy.getPersisterMap().clear();
		dormancy.getPersisterMap().put(TeamPersister.class, new TeamPersister(dormancy));
		dormancy.getPersisterMap().put(List.class, new CollectionPersister<List>(dormancy));

		sessionFactory.getCurrentSession().save(new Book(UUID.randomUUID().toString()));
		Team team = new Team(service.load(Employee.class, 1L));

		Team pass = service.pass(team);
		assertSame(LinkedHashSet.class, pass.getEmployees().get(0).getEmployees().getClass());
		assertNotSame(team.getEmployees().get(0), pass.getEmployees().get(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnavailablePersisterHibernateEndpoint() {
		dormancy.getPersisterMap().clear();
		sessionFactory.getCurrentSession().save(new Book(UUID.randomUUID().toString()));

		service.next(new Team(service.load(Employee.class, 1L)));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		GenericApplicationContext ctx = GenericApplicationContext.class.cast(applicationContext);
		ConstructorArgumentValues values = new ConstructorArgumentValues();
		values.addIndexedArgumentValue(0, ctx.getBean(Dormancy.class));
		ctx.registerBeanDefinition("teamPersister", new RootBeanDefinition(TeamPersister.class, values, null));
	}
}
