/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.core.io;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.util.Assert;

/**
 * {@link java.beans.PropertyEditor Editor} for {@link Resource}
 * descriptors, to automatically convert <code>String</code> locations (e.g.
 * <code>"file:C:/myfile.txt"</code> or
 * <code>"classpath:myfile.txt"</code>) to <code>Resource</code>
 * properties instead of using a <code>String</code> location property.
 *
 * <p>The path may contain <code>${...}</code> placeholders, to be resolved
 * as system properties: e.g. <code>${user.dir}</code>.
 *
 * <p>Delegates to a {@link ResourceLoader} to do the heavy lifting,
 * by default a {@link DefaultResourceLoader}.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see Resource
 * @see ResourceLoader
 * @see DefaultResourceLoader
 * @see org.springframework.util.SystemPropertyUtils#resolvePlaceholders
 * @see System#getProperty(String)
 */
public class ResourceEditor extends PropertyEditorSupport {

	private final ResourceLoader resourceLoader;


	/**
	 * Creates a new instance of the {@link ResourceEditor} class
	 * using a {@link DefaultResourceLoader}.
	 */
	public ResourceEditor() {
		this(new DefaultResourceLoader());
	}

	/**
	 * Creates a new instance of the {@link ResourceEditor} class
	 * using the given {@link ResourceLoader}.
	 * @param resourceLoader the <code>ResourceLoader</code> to use
	 * @throws IllegalArgumentException if the supplied <code>ResourceLoader</code> is <code>null</code> 
	 */
	public ResourceEditor(ResourceLoader resourceLoader) {
		Assert.notNull(resourceLoader, "The 'resourceLoader' parameter cannot be null.");
		this.resourceLoader = resourceLoader;
	}


	public void setAsText(String text) {
		if (StringUtils.hasText(text)) {
			String locationToUse = resolvePath(text).trim();
			setValue(this.resourceLoader.getResource(locationToUse));
		}
		else {
			setValue(null);
		}
	}

	/**
	 * Resolve the given path, replacing placeholders with
	 * corresponding system property values if necessary.
	 * @param path the original file path
	 * @return the resolved file path
	 * @see org.springframework.util.SystemPropertyUtils#resolvePlaceholders
	 */
	protected String resolvePath(String path) {
		return SystemPropertyUtils.resolvePlaceholders(path);
	}


	public String getAsText() {
		Resource value = (Resource) getValue();
		try {
			// Try to determine URL for resource.
			return (value != null ? value.getURL().toExternalForm() : "");
		}
		catch (IOException ex) {
			// Couldn't determine resource URL - return null to indicate
			// that there is no appropriate text representation.
			return null;
		}
	}

}
