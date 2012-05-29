package at.schauer.gregor.dormancy.closure;

/**
 * @author Gregor Schauer
 */
public class DelegateClosure<T> extends ResultClosure<T> {
	protected ResultClosure<? extends T> delegate;

	@Override
	public void execute(Object input) {
		delegate.execute(input);
		result = delegate.getResult();
	}
}
