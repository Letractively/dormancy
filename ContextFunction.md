# Using `ContextFunction`s #
Instead of implementing custom `EntityPersister`s, which are very similar to each other, `ContextFunction`s can be uses as a lightweight alternative.
They take a single `FunctionContext`, which holds the value(s) as well as the current execution state, applies a sequence of operations to it and returns the resulting `FunctionContext`.
Due to the fact that `ContextFunction`s are stateless, they can be executed chained, iteratively and/or recursively.
The processing just depends on the given `FunctionContext` and therefore may vary significantly.
However, there are two recommendations, which are _generall expected_ but not required:
  * Its execution does not cause any observable side effects.
  * The returned context is the same instance as the given.

## Simple Example ##
For every invocation, the `ConstantValueFunction` sets the value of the `FunctionContext` to a predefined value.
This can be used for modifying objects before serializing them to the client e.g., setting a certain reference to `null` so that the client is not aware of the previously referenced object.
```
public class ConstantValueFunction<E> implements ContextFunction<E> {
	protected E value;

	public ConstantValueFunction() {
		this(null);
	}

	public ConstantValueFunction(@Nullable E value) {
		this.value = value;
	}

	@Nullable
	@Override
	public FunctionContext<E> apply(@Nullable FunctionContext<E> input) {
		input.setObj(value);
		return input;
	}
}
```

## Advanced Example ##
The `CollectionFunction` takes a `Collection` and creates an empty `Collection` of the most equivalent type.
Afterwards, it iterates over the given collection and applies another function to every element within the collection.
The results are collected and returned after all elements have been processed successfully.
```
public class CollectionFunction<E extends Collection<D>, D> extends DelegateFunction<E, D> {
	@Nullable
	@Override
	public FunctionContext<E> apply(@Nullable FunctionContext<E> input) {
		E result = createCollection(input.getObj());
		FunctionContext<D> elemContext = new FunctionContext<D>(null, input.getTree());
		for (D elem : input.getObj()) {
			elemContext.setObj(elem);
			elemContext = delegate.apply(elemContext);
			result.add(elemContext.getObj());
		}
		input.setObj(result);
		return input;
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	public E createCollection(@Nonnull Collection src) {
		return (E) CollectionFactory.createApproximateCollection(src, src.size());
	}
}
```