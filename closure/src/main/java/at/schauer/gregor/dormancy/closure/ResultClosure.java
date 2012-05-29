package at.schauer.gregor.dormancy.closure;

import org.apache.commons.collections.Closure;

/**
 * @author Gregor Schauer
 */
public abstract class ResultClosure<T> implements Closure {
	T result;

	public T getResult() {
		return result;
	}
}
