package at.schauer.gregor.dormancy.service;

import at.schauer.gregor.dormancy.container.Team;
import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.interceptor.PersistenceEndpoint;
import at.schauer.gregor.dormancy.persister.TeamPersister;

import java.io.Serializable;
import java.util.List;

/**
 * @author Gregor Schauer
 */
public interface Service {
	void doNothing();

	Serializable save(Serializable obj);

	Application loadApp(Long id);

	<T extends Serializable> T load(Class<T> type, Long id);

	@SuppressWarnings("unchecked")
	List<Application> list();

	@PersistenceEndpoint(types = TeamPersister.class)
	Team next(Team team);

	Team prev(Team team);

	@PersistenceEndpoint(name = "teamPersister")
	Team pass(Team team);
}
