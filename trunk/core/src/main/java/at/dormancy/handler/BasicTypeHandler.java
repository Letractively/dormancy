/*
 * Copyright 2014 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.dormancy.handler;

import at.dormancy.util.DormancyContext;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Primitives;
import org.springframework.beans.BeanUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.util.*;

/**
 * Processes all basic types that do not need special treatment.
 *
 * @author Gregor Schauer
 */
public class BasicTypeHandler<C> implements ObjectHandler<C>, StaticObjectHandler<C> {
	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <R extends C> R createObject(@Nonnull R obj) {
		if (obj.getClass().isArray()) {
			return (R) Array.newInstance(obj.getClass().getComponentType(), Array.getLength(obj));
		}
		return BeanUtils.instantiateClass((Class<? extends R>) obj.getClass());
	}

	@Override
	public <R extends C, O extends R> R disconnect(O dbObj, @Nonnull DormancyContext ctx) {
		ctx.getAdjacencyMap().put(dbObj, dbObj);
		return dbObj;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <O extends C, R extends O> R apply(O trObj, R dbObj, @Nonnull DormancyContext ctx) {
		ctx.getAdjacencyMap().put(trObj, trObj);
		return (R) trObj;
	}

	@Nonnull
	@Override
	public Set<Class<?>> getSupportedTypes() {
		return ImmutableSet.<Class<?>>builder()
				.add(String.class, char[].class, Character[].class, byte[].class, Byte[].class)
				.addAll(Primitives.allPrimitiveTypes()).addAll(Primitives.allWrapperTypes())
				.add(BigInteger.class, BigDecimal.class)
				.add(Date.class, Calendar.class, TimeZone.class, Currency.class, Locale.class)
				.add(Class.class, Enum.class, URL.class, UUID.class)
				.add(Blob.class, Clob.class)
				.build();
	}
}
