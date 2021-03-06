/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.web.example.jetty;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.binder.AnnotatedBindingBuilder;
import java.util.concurrent.ExecutorService;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtext.web.example.jetty.AbstractEntitiesWebModule;
import org.eclipse.xtext.web.server.persistence.FileResourceHandler;
import org.eclipse.xtext.web.server.persistence.IResourceBaseProvider;
import org.eclipse.xtext.web.server.persistence.IServerResourceHandler;

/**
 * Use this class to register additional components to be used within the web application.
 */
@FinalFieldsConstructor
@SuppressWarnings("all")
public class EntitiesWebModule extends AbstractEntitiesWebModule {
  private final IResourceBaseProvider resourceBaseProvider;
  
  public void configureResourceBaseProvider(final Binder binder) {
    if ((this.resourceBaseProvider != null)) {
      AnnotatedBindingBuilder<IResourceBaseProvider> _bind = binder.<IResourceBaseProvider>bind(IResourceBaseProvider.class);
      _bind.toInstance(this.resourceBaseProvider);
    }
  }
  
  public Class<? extends IServerResourceHandler> bindIServerResourceHandler() {
    return FileResourceHandler.class;
  }
  
  public EntitiesWebModule(final Provider<ExecutorService> executorServiceProvider, final IResourceBaseProvider resourceBaseProvider) {
    super(executorServiceProvider);
    this.resourceBaseProvider = resourceBaseProvider;
  }
}
