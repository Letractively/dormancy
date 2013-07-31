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
package at.dormancy.access;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.Id;

/**
 * Uses JPA annotations for determining how to access entity properties.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
@SuppressWarnings("unchecked")
public class JpaPropertyAccessStrategy extends AnnotationPropertyAccessStrategy {
	static {
		accessAnnotations = new Class[]{Access.class};
		idAnnotations = new Class[]{Id.class};
	}

	public JpaPropertyAccessStrategy(@Nonnull Class<?> entityType) {
		super(entityType);
	}
}
