package at.schauer.gregor.dormancy.closure;

/**
 * @author Gregor Schauer
 */
public class DormancyMergeClosure extends DormancyClosure {
	@Override
	public void execute(Object input) {
		result = dormancy.merge_(input, tree);
	}
}
