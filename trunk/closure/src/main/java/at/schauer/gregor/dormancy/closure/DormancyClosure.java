package at.schauer.gregor.dormancy.closure;

import at.schauer.gregor.dormancy.Dormancy;

import java.util.Map;

/**
 * @author Gregor Schauer
 */
public abstract class DormancyClosure extends ResultClosure<Object> {
	protected Dormancy dormancy;
	protected Map<Object, Object> tree;
}
