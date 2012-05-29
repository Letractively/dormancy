package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.Dormancy;
import at.schauer.gregor.dormancy.container.Team;
import at.schauer.gregor.dormancy.entity.Employee;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * @author Gregor Schauer
 */
public class TeamPersister extends AbstractContainerPersister<Team> {
	@Inject
	public TeamPersister(Dormancy dormancy) {
		super(dormancy);
	}

	@Override
	public <T extends Team> T clone_(T dbObj, @Nonnull Map<Object, Object> tree) {
		List<Employee> clone = dormancy.clone_(dbObj.getEmployees(), tree);
		dbObj.setEmployees(clone);
		return dbObj;
	}

	@Override
	public <T extends Team> T merge_(T trObj, @Nonnull Map<Object, Object> tree) {
		List<Employee> merge = dormancy.merge_(trObj.getEmployees(), tree);
		trObj.setEmployees(merge);
		return trObj;
	}

	@Override
	public <T extends Team> T merge_(T trObj, T dbObj, @Nonnull Map<Object, Object> tree) {
		List<Employee> merge = dormancy.merge_(trObj.getEmployees(), dbObj.getEmployees(), tree);
		trObj.setEmployees(merge);
		return trObj;
	}

	@Nonnull
	@Override
	protected Team createContainer(@Nonnull Team container) {
		return new Team();
	}
}
