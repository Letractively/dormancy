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
package at.schauer.gregor.dormancy.scenario.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import net.floatz.gwt.client.resource.Floatz;

/**
 * @author Gregor Schauer
 */
public interface ResourceBundle extends ClientBundle {
	ResourceBundle INSTANCE = GWT.create(ResourceBundle.class);

	Floatz floatz();
}