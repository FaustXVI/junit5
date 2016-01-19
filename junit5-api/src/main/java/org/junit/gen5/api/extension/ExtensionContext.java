/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.gen5.commons.util.Preconditions;

/**
 * {@code ExtensionContext} encapsulates the <em>context</em> in which the
 * current test or container is being executed.
 *
 * <p>{@link Extension Extensions} are provided an instance of
 * {@code ExtensionContext} to perform their work.
 *
 * @since 5.0
 */
public interface ExtensionContext {

	/**
	 * Publish an {@code entry} which is then being reported
	 * via {@code org.junit.gen5.engine.EngineExecutionListener}
	 *
	 * @param entry The entry to be published.
	 */
	void publishReportEntry(Map<String, String> entry);

	/**
	 * Get the parent extension context if there is one.
	 */
	Optional<ExtensionContext> getParent();

	/**
	 * Get the unique id of the current test or container.
	 */
	String getUniqueId();

	/**
	 * Get the name for the current test or container.
	 *
	 * <p>The <em>name</em> is typically a technical name of the underlying
	 * artifact &mdash; for example, the fully qualified name of a Java class,
	 * the canonical absolute path to a file in the file system, etc.
	 *
	 * @see #getDisplayName()
	 */
	String getName();

	/**
	 * Get the display name for the current test or container.
	 *
	 * <p>Display names are typically used for test reporting in IDEs and
	 * build tools and may contain spaces, special characters, and even emoji.
	 */
	String getDisplayName();

	/**
	 * Get the {@link Class} associated with the current test or container.
	 */
	Class<?> getTestClass();

	/**
	 * Get the {@link AnnotatedElement} on which the extension was registered.
	 */
	AnnotatedElement getElement();

	/**
	 * Get a {@link Store} with the default {@link Namespace}.
	 *
	 * @see #getStore(Namespace)
	 */
	default Store getStore() {
		return getStore(Namespace.DEFAULT);
	}

	/**
	 * Get a {@link Store} for a self constructed {@link Namespace}.
	 *
	 * @return The store in which to put and get objects for other invocations
	 * of the same extension or different ones.
	 */
	Store getStore(Namespace namespace);

	/**
	 * The {@code Store} provides methods for extensions to save and retrieve data.
	 */
	interface Store {
		/**
		 * Get an object that has been stored using a {@code key}.
		 *
		 * <p>If no value has been saved in the current {@link ExtensionContext} for this {@code key},
		 * the ancestors are asked for a value with the same {@code key} in the store's {@code Namespace}.
		 *
		 * @param key the key
		 * @return the value
		 */
		Object get(Object key);

		/**
		 * Store a {@code value} for later retrieval using a {@code key}. {@code null} is a valid value.
		 *
		 * <p>A stored {@code value} is visible in offspring {@link ExtensionContext}s
		 * for the store's {@code Namespace} unless they overwrite it.
		 *
		 * @param key the key
		 * @param value the value
		 */
		void put(Object key, Object value);

		/**
		 * Get an object that has been stored using a {@code key}. If no value has been store using that {@code key}
		 * the value will be computed by the {@code defaultCreator} and be stored.
		 *
		 * @param key the key
		 * @param defaultCreator the function called to create the value
		 * @return the value
		 */
		Object getOrComputeIfAbsent(Object key, Function<Object, Object> defaultCreator);

		/**
		 * Remove a value that was previously stored using {@code key} so that {@code key} can be used anew.
		 *
		 * <p>The key will only be removed in the current {@link ExtensionContext} not in ancestors.
		 *
		 * @param key the key
		 * @return the previous value or {@code null} if no value was present
		 * for the specified key
		 */
		Object remove(Object key);
	}

	/**
	 * Instances of this class are used to give saved data in extensions a scope, so that
	 * extensions won't accidentally mix up data across each other or across different invocations
	 * within their lifecycle.
	 */
	class Namespace {

		/**
		 * Get default namespace which allows access to stored data from all extensions.
		 */
		public static Namespace DEFAULT = Namespace.of(new Object());

		/**
		 * Create a namespace which restricts access to data to all users which use the same
		 * {@code parts} for creating a namespace. The order of the  {@code parts} is not significant.
		 * Internally the {@code parts} are compared using {@code Object.equals(Object)}.
		 */
		public static Namespace of(Object... parts) {
			Preconditions.notNull(parts, "There must be at least one reference object to create a namespace");

			return new Namespace(parts);
		}

		private final Set<?> parts;

		private Namespace(Object... parts) {
			this.parts = new HashSet<>(Arrays.asList(parts));
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Namespace namespace = (Namespace) o;
			return parts.equals(namespace.parts);
		}

		@Override
		public int hashCode() {
			return parts.hashCode();
		}
	}

}