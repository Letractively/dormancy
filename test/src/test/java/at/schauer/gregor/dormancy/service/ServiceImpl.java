package at.schauer.gregor.dormancy.service;

import at.schauer.gregor.dormancy.Dormancy;
import at.schauer.gregor.dormancy.container.Team;
import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.Employee;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gregor Schauer
 */
@Transactional
public class ServiceImpl implements Service {
	@Inject
	SessionFactory sessionFactory;
	@Inject
	Dormancy dormancy;

	@Override
	public void doNothing() {
	}

	@Override
	public Serializable save(Serializable obj) {
		sessionFactory.getCurrentSession().saveOrUpdate(obj);
		sessionFactory.getCurrentSession().flush();
		return sessionFactory.getCurrentSession().getIdentifier(obj);
	}

	@Override
	public Application loadApp(Long id) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Application.class);
		criteria.setFetchMode("employees", FetchMode.JOIN);
		criteria.add(Restrictions.eq("id", id));
		return (Application) criteria.uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T load(Class<T> type, Long id) {
		return (T) sessionFactory.getCurrentSession().load(type, id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Application> list() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Application.class);
		return criteria.list();
	}

	@Override
	public Team next(Team team) {
		team.setEmployees(new ArrayList<Employee>(Collections.singletonList(load(Employee.class, team.getEmployees().iterator().next().getId() + 1))));
		return team;
	}

	@Override
	public Team prev(Team team) {
		team.setEmployees(new ArrayList<Employee>(Collections.singletonList(load(Employee.class, team.getEmployees().iterator().next().getId() - 1))));
		return team;
	}

	@Override
	public Team pass(Team team) {
		team.setEmployees(new ArrayList<Employee>(Collections.singletonList(load(Employee.class, team.getEmployees().iterator().next().getId()))));
		return team;
	}
}
