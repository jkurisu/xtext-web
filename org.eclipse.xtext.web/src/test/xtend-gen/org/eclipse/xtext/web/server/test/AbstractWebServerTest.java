/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.web.server.test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.web.example.statemachine.StatemachineRuntimeModule;
import org.eclipse.xtext.web.example.statemachine.StatemachineStandaloneSetup;
import org.eclipse.xtext.web.example.statemachine.tests.StatemachineInjectorProvider;
import org.eclipse.xtext.web.server.ISession;
import org.eclipse.xtext.web.server.XtextServiceDispatcher;
import org.eclipse.xtext.web.server.persistence.IResourceBaseProvider;
import org.eclipse.xtext.web.server.test.HashMapSession;
import org.eclipse.xtext.web.server.test.MockServiceContext;
import org.eclipse.xtext.web.server.test.languages.StatemachineWebModule;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.After;
import org.junit.Before;

@SuppressWarnings("all")
public abstract class AbstractWebServerTest {
  public static class TestResourceBaseProvider implements IResourceBaseProvider {
    private final HashMap<String, URI> testFiles = new HashMap<String, URI>();
    
    @Override
    public URI getFileURI(final String resourceId) {
      return this.testFiles.get(resourceId);
    }
  }
  
  private final StatemachineInjectorProvider injectorProvider = new StatemachineInjectorProvider() {
    @Override
    protected Injector internalCreateInjector() {
      return new StatemachineStandaloneSetup() {
        @Override
        public Injector createInjector() {
          final Provider<ExecutorService> _function = () -> {
            ExecutorService _newCachedThreadPool = Executors.newCachedThreadPool();
            final Procedure1<ExecutorService> _function_1 = (ExecutorService it) -> {
              AbstractWebServerTest.this.executorServices.add(it);
            };
            return ObjectExtensions.<ExecutorService>operator_doubleArrow(_newCachedThreadPool, _function_1);
          };
          final StatemachineWebModule webModule = new StatemachineWebModule(_function);
          webModule.setResourceBaseProvider(AbstractWebServerTest.this.resourceBaseProvider);
          Module _runtimeModule = AbstractWebServerTest.this.getRuntimeModule();
          Modules.OverriddenModuleBuilder _override = Modules.override(_runtimeModule);
          Module _with = _override.with(webModule);
          return Guice.createInjector(_with);
        }
      }.createInjectorAndDoEMFRegistration();
    }
  };
  
  private final List<ExecutorService> executorServices = CollectionLiterals.<ExecutorService>newArrayList();
  
  private AbstractWebServerTest.TestResourceBaseProvider resourceBaseProvider;
  
  @Inject
  private XtextServiceDispatcher dispatcher;
  
  protected Module getRuntimeModule() {
    return new StatemachineRuntimeModule();
  }
  
  @Before
  public void setup() {
    AbstractWebServerTest.TestResourceBaseProvider _testResourceBaseProvider = new AbstractWebServerTest.TestResourceBaseProvider();
    this.resourceBaseProvider = _testResourceBaseProvider;
    final Injector injector = this.injectorProvider.getInjector();
    injector.injectMembers(this);
    this.injectorProvider.setupRegistry();
  }
  
  @After
  public void teardown() {
    final Consumer<ExecutorService> _function = (ExecutorService it) -> {
      it.shutdown();
    };
    this.executorServices.forEach(_function);
    this.executorServices.clear();
    this.resourceBaseProvider.testFiles.clear();
    this.injectorProvider.restoreRegistry();
  }
  
  protected File createFile(final String content) {
    try {
      final File file = File.createTempFile("test", ".statemachine");
      String _name = file.getName();
      String _absolutePath = file.getAbsolutePath();
      URI _createFileURI = URI.createFileURI(_absolutePath);
      this.resourceBaseProvider.testFiles.put(_name, _createFileURI);
      final FileWriter writer = new FileWriter(file);
      writer.write(content);
      writer.close();
      file.deleteOnExit();
      return file;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  protected XtextServiceDispatcher.ServiceDescriptor getService(final Map<String, String> parameters) {
    HashMapSession _hashMapSession = new HashMapSession();
    return this.getService(parameters, _hashMapSession);
  }
  
  protected XtextServiceDispatcher.ServiceDescriptor getService(final Map<String, String> parameters, final ISession session) {
    XtextServiceDispatcher.ServiceDescriptor _xblockexpression = null;
    {
      final MockServiceContext serviceContext = new MockServiceContext(parameters, session);
      _xblockexpression = this.dispatcher.getService(serviceContext);
    }
    return _xblockexpression;
  }
}
