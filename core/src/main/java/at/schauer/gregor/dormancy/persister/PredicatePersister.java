/*
 * Copyright 2012 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.schauer.gregor.dormancy.persister;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Evaluates a {@link Predicate} for determining the {@link EntityPersister} to use for processing certain objects.<br/>
 * If a {@link AbstractEntityPersister} is {@code null}, the {@link NoOpPersister} is used instead.
 *
 * @author Gregor Schauer
 * @see Predicate
 * @since 1.0.2
 */
public class PredicatePersister<C, P extends Predicate> extends AbstractEntityPersister<C> {
	protected AbstractEntityPersister<C> predicateDelegate;
	protected AbstractEntityPersister<C> fallbackDelegate;
	protected P predicate;

	public PredicatePersister() {
		this(null, null);
	}

	public PredicatePersister(@Nullable AbstractEntityPersister<C> predicateDelegate, @Nullable P predicate) {
		this(predicateDelegate, null, predicate);
	}

	public PredicatePersister(@Nullable AbstractEntityPersister<C> predicateDelegate, @Nullable AbstractEntityPersister<C> fallbackDelegate, @Nullable P predicate) {
		this.predicate = predicate;
		this.predicateDelegate = predicateDelegate;
		this.fallbackDelegate = fallbackDelegate;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (dbObj == null || tree.containsKey(dbObj)) {
			return (C) tree.get(dbObj);
		}
		return evaluate(dbObj) ? getPredicateDelegate().clone_(dbObj, tree) : getFallbackDelegate().clone_(dbObj, tree);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || tree.containsKey(trObj)) {
			return (C) tree.get(trObj);
		}
		return evaluate(trObj) ? getPredicateDelegate().merge_(trObj, tree) : getFallbackDelegate().merge_(trObj, tree);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || dbObj == null || tree.containsKey(trObj)) {
			return (C) tree.get(trObj);
		}
		return evaluate(trObj) ? getPredicateDelegate().merge_(trObj, dbObj, tree) : getFallbackDelegate().merge_(trObj, dbObj, tree);
	}

	protected <T extends C> boolean evaluate(T obj) {
		return predicate != null ? predicate.evaluate(obj) : PredicateUtils.truePredicate().evaluate(obj);
	}

	/**
	 * Sets the AbstractEntityPersister to use if the Predicate evaluates to {@code true}.
	 *
	 * @param predicateDelegate the predicate AbstractEntityPersister
	 */
	public void setPredicateDelegate(@Nonnull AbstractEntityPersister<C> predicateDelegate) {
		this.predicateDelegate = predicateDelegate;
	}

	/**
	 * Returns the AbstractEntityPersister used if the Predicate evaluates to {@code true}.
	 *
	 * @return the predicate AbstractEntityPersister
	 */
	@Nonnull
	public AbstractEntityPersister<C> getPredicateDelegate() {
		return predicateDelegate != null ? predicateDelegate : (predicateDelegate = NoOpPersister.getInstance());
	}

	/**
	 * Sets the AbstractEntityPersister to use if the Predicate evaluates to {@code false}.
	 *
	 * @param fallbackDelegate the fallback AbstractEntityPersister
	 */
	public void setFallbackDelegate(@Nonnull AbstractEntityPersister<C> fallbackDelegate) {
		this.fallbackDelegate = fallbackDelegate;
	}

	/**
	 * Returns the AbstractEntityPersister used if the Predicate evaluates to {@code false}.
	 *
	 * @return the fallback AbstractEntityPersister
	 */
	@Nonnull
	public AbstractEntityPersister<C> getFallbackDelegate() {
		return fallbackDelegate != null ? fallbackDelegate : (fallbackDelegate = NoOpPersister.getInstance());
	}

	/**
	 * Sets the Predicate to use.
	 *
	 * @param predicate the Predicate
	 */
	public void setPredicate(@Nonnull P predicate) {
		this.predicate = predicate;
	}

	/**
	 * Returns the used Predicate.
	 *
	 * @return the Predicate
	 */
	@Nullable
	public P getPredicate() {
		return predicate;
	}
}
