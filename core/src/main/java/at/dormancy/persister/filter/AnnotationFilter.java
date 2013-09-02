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

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;

/**
 * Selects {@link java.lang.reflect.Field}s or {@link java.lang.reflect.Method}s annotated with certain annotations.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class AnnotationFilter extends SimpleMemberFilter {
	protected Class<? extends Annotation>[] annotationTypes;

	public AnnotationFilter(@Nonnull Class<? extends Annotation>... annotationTypes) {
		this.annotationTypes = annotationTypes;
	}

	@Override
	protected <M extends AccessibleObject & Member> boolean matchesFilter(@Nonnull M member) {
		for (Class<? extends Annotation> annotationType : annotationTypes) {
			if (member.isAnnotationPresent(annotationType)) {
				return true;
			}
		}
		return false;
	}
}
