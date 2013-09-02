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
package at.dormancy.persister.filter;

import at.dormancy.util.ClassLookup;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;

/**
 * Selects {@link java.lang.reflect.Field}s or {@link java.lang.reflect.Method}s annotated with
 * {@link javax.persistence.Transient}.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class TransientFilter extends AnnotationFilter {
	private static final TransientFilter INSTANCE = new TransientFilter();

	@Nonnull
	public static TransientFilter getInstance() {
		return INSTANCE;
	}

	@SuppressWarnings("unchecked")
	private TransientFilter() {
		super(ClassLookup.find("javax.persistence.Transient")
				.orThrow("Cannot find '%s' cannot in the classpath", "javax.persistence.Transient").<Annotation>get());
	}
}
