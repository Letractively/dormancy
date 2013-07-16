/*
 * Copyright 2013 Gregor Schauer
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
package at.schauer.gregor.dormancy.persister.predicate;

import org.apache.commons.collections.Predicate;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * {@link Predicate} implementation that returns {@code true} if the class of the input is located within a certain
 * package location stored in this {@link Predicate}.<br/>
 *
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class PackagePredicate implements Predicate, Serializable {
	protected String packageName;

	public PackagePredicate(@Nonnull String packageName) {
		this.packageName = packageName.endsWith(".") ? packageName : packageName + '.';
	}

	@Override
	public boolean evaluate(@Nonnull Object object) {
		return object.getClass().getName().startsWith(packageName);
	}
}
