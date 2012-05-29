package at.schauer.gregor.dormancy.closure;

/**
 * @author Gregor Schauer
 */
public class DormancyCloneClosure extends DormancyClosure {
	@Override
	public void execute(Object input) {
		result = dormancy.clone_(input, tree);
	}
}
