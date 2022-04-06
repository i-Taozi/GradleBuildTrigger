/*
 * Copyright (c) 1998-2015 Caucho Technology -- all rights reserved
 *
 * This file is part of Baratine(TM)
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Baratine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Baratine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Baratine; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.v5.amp.manager;

import com.caucho.v5.amp.ServiceRefAmp;
import com.caucho.v5.amp.ServicesAmp;
import com.caucho.v5.amp.ensure.EnsureDriverAmp;
import com.caucho.v5.amp.ensure.MethodEnsureAmp;
import com.caucho.v5.amp.inbox.OutboxAmpDirect;
import com.caucho.v5.amp.inbox.OutboxAmpExecutorFactory;
import com.caucho.v5.amp.inbox.OutboxAmpImpl;
import com.caucho.v5.amp.inbox.QueueFullHandler;
import com.caucho.v5.amp.journal.JournalAmp;
import com.caucho.v5.amp.journal.JournalDriverAmp;
import com.caucho.v5.amp.message.DebugQueryMap;
import com.caucho.v5.amp.message.SystemMessage;
import com.caucho.v5.amp.proxy.ProxyFactoryAmp;
import com.caucho.v5.amp.proxy.ProxyFactoryAmpImpl;
import com.caucho.v5.amp.proxy.ProxyHandleAmp;
import com.caucho.v5.amp.remote.ServiceNodeBase;
import com.caucho.v5.amp.service.SchemeLocal;
import com.caucho.v5.amp.service.ServiceBuilderAmp;
import com.caucho.v5.amp.service.ServiceBuilderImpl;
import com.caucho.v5.amp.service.ServiceConfig;
import com.caucho.v5.amp.service.ServiceRefChild;
import com.caucho.v5.amp.service.ServiceRefPin;
import com.caucho.v5.amp.spi.InboxAmp;
import com.caucho.v5.amp.spi.MessageAmp;
import com.caucho.v5.amp.spi.OutboxAmp;
import com.caucho.v5.amp.spi.RegistryAmp;
import com.caucho.v5.amp.spi.ServiceManagerBuilderAmp;
import com.caucho.v5.amp.spi.ShutdownModeAmp;
import com.caucho.v5.amp.stub.MethodAmp;
import com.caucho.v5.amp.stub.ShimConverter;
import com.caucho.v5.amp.stub.StubAmp;
import com.caucho.v5.amp.stub.StubAmpSystem;
import com.caucho.v5.amp.stub.StubClassFactoryAmp;
import com.caucho.v5.amp.stub.StubClassFactoryAmpImpl;
import com.caucho.v5.amp.stub.StubGenerator;
import com.caucho.v5.inject.InjectorAmp;
import com.caucho.v5.inject.type.TypeRef;
import com.caucho.v5.lifecycle.Lifecycle;
import com.caucho.v5.loader.EnvLoader;
import com.caucho.v5.util.L10N;
import io.baratine.convert.Convert;
import io.baratine.inject.Key;
import io.baratine.service.Api;
import io.baratine.service.Result;
import io.baratine.service.ResultFuture;
import io.baratine.service.Service;
import io.baratine.service.ServiceExceptionQueueFull;
import io.baratine.service.ServiceRef;
import io.baratine.spi.MessageApi;
import io.baratine.vault.Vault;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Baratine core service manager.
 */
public class ServicesAmpImpl implements ServicesAmp, AutoCloseable
{
  private static final L10N L = new L10N(ServicesAmpImpl.class);
  private static final Logger log
    = Logger.getLogger(ServicesAmpImpl.class.getName());

  private static final Logger initLog = Logger.getLogger("com.baratine.init-log");
  
  private final String _name;
  private final ClassLoader _classLoader;
  
  // private final ModuleAmp _module;
  
  private final RegistryAmp _registry;
  
  //private final InboxFactoryAmp _inboxFactory;
  private final ProxyFactoryAmp _proxyFactory;
  private final StubClassFactoryAmp _stubFactory;
  private final ShimFactory _shims = new ShimFactory();
  private final JournalDriverAmp _journalDriver;
  private final EnsureDriverAmp _ensureDriver;
  // private final ContextSessionFactory _channelFactory;
  
  private final StubGenerator []_stubGenerators;
  
  private final Supplier<InjectorAmp> _injectManager;
  
  private final InboxAmp _inboxSystem;
  //private final OutboxAmp _systemOutbox;

  private final OutboxAmp _systemContext;
  private final SystemMessage _systemMessage;
  
  // private OutboxAmp _systemOutboxDynamic;
  
  private final ServiceNode _podNode;
  
  private final AtomicLong _remoteMessageWriteCount
    = new AtomicLong();
  
  private final AtomicLong _remoteMessageReadCount
    = new AtomicLong();
  
  private boolean _isDebug;
  
  private QueueFullHandler _queueFullHandler;

  private boolean _isAutoStart = true;
  private String _selfServer;
  
  private final ArrayList<ServiceRef> _autoStart = new ArrayList<>();
  private final Lifecycle _lifecycle = new Lifecycle();
  private String _debugId;
  private DebugQueryMap _debugQueryMap;
  
  private final Supplier<OutboxAmp> _outboxFactory;
  
  private ConcurrentHashMap<Class<?>,String> _addressMap
    = new ConcurrentHashMap<>();
  
  private Throwable _configException;
  private long _journalDelay = -1;
  
  
  public ServicesAmpImpl(ServiceManagerBuilderAmp builder)
  {
    initLog.log(Level.FINE, () -> L.l("new ServicesAmpImpl(${0})", builder));

    _name = builder.name();
    _debugId = builder.debugId();
    
    _classLoader = builder.classLoader();
    
    EnvLoader.addCloseListener(this, _classLoader);
    
    _isDebug = builder.isDebug();
    
    if (_isDebug) {
      int capacity = 4096;
      
      _debugQueryMap = new DebugQueryMap(capacity, builder.debugQueryTimeout());
    }
    
    _registry = new RegistryImpl(this);
    
    //_inboxFactory = new InboxFactoryQueue(this);
    
    _proxyFactory = new ProxyFactoryAmpImpl(this);
    _stubFactory = new StubClassFactoryAmpImpl(this);
    _journalDriver = builder.journalDriver();
    _ensureDriver = builder.ensureDriver();

    //_channelFactory = new ContextSessionFactory(this);
    
    _stubGenerators = builder.stubGenerators();
    
    ServiceNode podNode = builder.podNode();
    
    if (podNode == null) {
      podNode = new ServiceNodeBase();
    }
    
    _podNode = podNode;
    
    _inboxSystem = createSystemInbox();
    Objects.requireNonNull(_inboxSystem);
    
    _systemMessage = new SystemMessage(_inboxSystem);
    // _systemContext.setContextMessage(_systemMessage);
    
    // XXX: 
    //_systemOutbox = new OutboxAmpDirect(_systemInbox, _systemMessage);
    _systemContext = new OutboxAmpDirect(_inboxSystem, _systemMessage);
    
    Supplier<Executor> systemExecutor = builder.systemExecutor();
    
    if (systemExecutor != null) {
      _outboxFactory = new OutboxAmpExecutorFactory(this, systemExecutor, _inboxSystem);
    }
    else {
      _outboxFactory = ()->createOutbox();
    }
        
    QueueFullHandler queueFullHandler = builder.queueFullHandler();
    
    if (queueFullHandler == null) {
      queueFullHandler = new QueueFullHandlerAmp(); 
    }
    _queueFullHandler = queueFullHandler;
    
    //ServiceRefRoot root = new ServiceRefRoot(this);
    // _broker.bind("/", root);
    _registry.bind("local://", new SchemeLocal(this));
    
    _injectManager = ()->(InjectorAmp) builder.injector().get();
  }

  /*
  @Override
  public void setSystemExecutorFactory(Supplier<Executor> factory)
  {
    // _systemContextFactory = factory;
    
    if (factory != null) {
      //Consumer<Runnable> executor = factory.get();
      //Objects.requireNonNull(executor);
      
      _outboxFactory = new OutboxAmpExecutorFactory(this, factory, _systemInbox);
    }
    else {
      _outboxFactory = _systemOutboxFactory;
    }
  }
  */
  
  private OutboxAmp createOutbox()
  {
    OutboxAmpImpl outbox = new OutboxAmpImpl();
    outbox.inbox(_inboxSystem);

    // outbox.open();
    
    return outbox;
  }
  
  /*
  @Override
  public ModuleAmp getModule()
  {
    return _module;
  }
  */
  
  /*
  @Override
  public RampSystem getSystem()
  {
    return getModule().getSystem();
  }
  */
  
  @Override
  public String getName()
  {
    return _name;
  }
  
  @Override
  public String getDebugId()
  {
    return _debugId;
  }
  
  @Override
  public ClassLoader classLoader()
  {
    return _classLoader;
  }
  
  @Override
  public boolean isAutoStart()
  {
    return _isAutoStart;
  }
  
  /*
  @Override
  public String getSelfServer()
  {
    return _selfServer;
  }
  
  @Override
  public void setSelfServer(String selfServer)
  {
    _selfServer = selfServer;
  }
  */
  
  @Override
  public RegistryAmp registry()
  {
    return _registry;
  }
  
  @Override
  public final InboxAmp inboxSystem()
  {
    //Objects.requireNonNull(_systemInbox);
    
    return _inboxSystem;
  }

  @Override
  public InjectorAmp injector()
  {
    return _injectManager.get();
  }
  
  @Override
  public final ServiceRefAmp currentService()
  {
    OutboxAmp outbox = OutboxAmp.current();

    return outbox.inbox().serviceRef();
  }
  
  @Override
  public final MessageAmp currentMessage()
  {
    OutboxAmp outbox = OutboxAmp.current();
    
    return outbox.message();
    // return ContextMessageAmp.get();
  }
  
  /**
   * Run a task that returns a future in an outbox context.
   */
  @Override
  public <T> T run(long timeout, TimeUnit unit,
                   Consumer<Result<T>> task)
  {
    try (OutboxAmp outbox = OutboxAmp.currentOrCreate(this)) {
      ResultFuture<T> future = new ResultFuture<>();
      
      task.accept(future);
      
      return future.get(timeout, unit);
    }
  }

  @Override
  public final OutboxAmp outboxSystem()
  {
    return _systemContext;
  }
  
  /*
  private final MessageAmp systemMessage()
  {
    return _systemMessage;
  }
  */
  
  @Override
  public ServiceNode node()
  {
    return _podNode;
  }
  
  @Override
  public QueueFullHandler queueFullHandler()
  {
    return _queueFullHandler;
  }
  
  @Override
  public ServiceRefAmp service(String address)
  {
    Objects.requireNonNull(address);
    
    if (address.isEmpty()) {
      throw new IllegalArgumentException(); 
    }
    
    Thread thread = Thread.currentThread();
    ClassLoader loader = thread.getContextClassLoader();
    
    try (OutboxAmp outbox = OutboxAmp.currentOrCreate(this)) {
      Object oldContext = outbox.getAndSetContext(inboxSystem());

      thread.setContextClassLoader(classLoader());

      try {
        ServiceRefAmp serviceRef = registry().service(address);
        
        return serviceRef;
      } finally {
        outbox.getAndSetContext(oldContext);
      }
    } finally {
      thread.setContextClassLoader(loader);
    }
  }
  
  @Override
  public <T> T service(Class<T> api)
  {
    Objects.requireNonNull(api);

    return service(address(api)).as(api);
  }
  
  @Override
  public <T> T service(Class<T> api, String id)
  {
    Objects.requireNonNull(api);
    
    String address = address(api);

    if (id.startsWith("/")) {
      return service(address + id).as(api);
    }
    else {
      return service(address + "/" + id).as(api);
    }
  }
  
  @Override
  public String address(Class<?> api)
  {
    String address = _addressMap.get(api);
    
    if (address == null) {
      address = serviceAddress(api);
      
      address = address(api, address);

      _addressMap.putIfAbsent(api, address);
    }
    
    return address;
  }
  
  @Override
  public String address(Class<?> type, Class<?> api)
  {
    Objects.requireNonNull(type);
    
    if (api == null) {
      api = findApi(type);
    }
    
    String address = serviceAddress(type);

    if (! address.isEmpty()) {
      return address(api, address);
    }
    else {
      return address(api);
    }
  }
  
  private String serviceAddress(Class<?> type)
  {
    Service service = type.getAnnotation(Service.class);
    
    if (service != null && ! service.value().isEmpty()) {
      return service.value();
    }
    
    for (Annotation ann : type.getAnnotations()) {
      service = ann.annotationType().getAnnotation(Service.class);
      
      if (service != null && ! service.value().isEmpty()) {
        return service.value();
      }
    }
    
    return "";
  }
  
  private Class<?> findApi(Class<?> type)
  {
    for (Class<?> api : type.getInterfaces()) {
      if (api.isAnnotationPresent(Api.class)
          || api.isAnnotationPresent(Service.class)) {
        return api;
      }
    }
    
    return type;
  }
  
  /**
   * Calculate address from an API with an address default
   */
  @Override
  public String address(Class<?> api, String address)
  {
    Objects.requireNonNull(address);

    if (address.isEmpty()) {
      address = addressDefault(api);
    }
    
    int slash = address.indexOf("/");
    //int colon = address.indexOf(":");

    if (address.endsWith(":") && slash < 0) {
      address += "//";
    }

    int p = address.indexOf("://");
    int q = -1;

    if (p > 0) {
      q = address.indexOf('/', p + 3);
    }
    
    if (address.indexOf('{') > 0) {
      return addressBraces(api, address);
    }

    boolean isPrefix
      = address.startsWith("session:") || address.startsWith("pod:"); 

    if (address.isEmpty()
        || p > 0 && q < 0 && isPrefix) {
      if (Vault.class.isAssignableFrom(api)) {
        TypeRef itemRef = TypeRef.of(api).to(Vault.class).param("T");
        
        Class<?> assetClass = itemRef.rawClass();

        address = address + "/" + apiAddress(assetClass);
      }
      else {
        address = address + "/" + apiAddress(api);
      }
    }
    
    return address;
  }
  
  private String apiAddress(Class<?> api)
  {
    Class<?> serviceApi = serviceApi(api);
    
    if (serviceApi != null) {
      return serviceApi.getSimpleName();
    }
    else {
      return api.getSimpleName();
    }
  }
  
  private Class<?> serviceApi(Class<?> api)
  {
    if (api == null) {
      return null;
    }
    
    if (api.isAnnotationPresent(Service.class)) {
      return api;
    }

    Annotation[] annotations = api.getAnnotations();
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().isAnnotationPresent(Service.class)) {
        return api;
      }
    }

    Class<?> serviceApi = serviceApi(api.getSuperclass());
    
    if (serviceApi != null) {
      return serviceApi;
    }

    for (Class<?> iface : api.getInterfaces()) {
      serviceApi = serviceApi(iface);
      
      if (serviceApi != null) {
        return serviceApi;
      }
    }
    
    return null;
  }
  
  private String addressDefault(Class<?> api)
  {
    if (api == null) {
      return "";
    }
    
    Service service = api.getAnnotation(Service.class);
    
    if (service != null) {
      return service.value();
    }
    
    for (Annotation ann : api.getAnnotations()) {
      service = ann.annotationType().getAnnotation(Service.class);
      
      if (service != null) {
        return service.value();
      }
    }
    
    String value = addressDefault(api.getSuperclass());
    
    if (! value.isEmpty()) {
      return value;
    }
    
    for (Class<?> iface : api.getInterfaces()) {
      value = addressDefault(iface);
      
      if (! value.isEmpty()) {
        return value;
      }
    }
    
    return "";
  }
  
  private String addressBraces(Class<?> api, String address)
  {
    StringBuilder sb = new StringBuilder();
    
    int i = 0;
    for (; i < address.length(); i++) {
      char ch = address.charAt(i);
      
      if (ch != '{') {
        sb.append(ch);
        continue;
      }
      
      int j = address.indexOf('}', i);
      
      if (j < 0) {
        throw new IllegalArgumentException(address);
      }
      
      String var = address.substring(i + 1, j);
      
      i = j;
      
      if ("class".equals(var)) {
        sb.append(api.getSimpleName());
      }
      else {
        throw new IllegalArgumentException(address);
      }
    }
    
    return sb.toString();
  }

  @Override
  public <T> T newProxy(ServiceRefAmp service, 
                           Class<T> api)
  {
    return proxyFactory().createProxy(service, api);
  }

  /**
   * newService() creates a new service from a bean
   */
  @Override
  public ServiceRefAmp toRef(Object serviceImpl)
  {
    if (serviceImpl instanceof ProxyHandleAmp) {
      ProxyHandleAmp proxy = (ProxyHandleAmp) serviceImpl;
      
      return proxy.__caucho_getServiceRef();
    }
    
    Objects.requireNonNull(serviceImpl);
    
    return new ServiceBuilderImpl<>(this, serviceImpl).ref();
  }

  /**
   * newService() creates a new service from a bean
   */
  @Override
  public ServiceBuilderAmp newService(Object worker)
  {
    Objects.requireNonNull(worker);
    
    return new ServiceBuilderImpl<>(this, worker);
  }

  /**
   * newService() creates a new service from a bean
   */
  @Override
  public <T> ServiceBuilderAmp newService(Class<T> type,
                                          Supplier<? extends T> supplier)
  {
    Objects.requireNonNull(type);
    Objects.requireNonNull(supplier);
    
    return new ServiceBuilderImpl<>(this, type, supplier);
  }

  /**
   * newService() creates a new service from a bean
   */
  @Override
  public <T> ServiceBuilderAmp newService(Class<T> type)
  {
    Objects.requireNonNull(type);
    
    return new ServiceBuilderImpl<>(this, type);
  }

  /**
   * newService() creates a new service from a bean
   */
  @Override
  public ServiceBuilderAmp service(Key<?> key, Class<?> api)
  {
    Objects.requireNonNull(api);
    
    //return newService().service(key, api);
    //throw new UnsupportedOperationException();
    return new ServiceBuilderImpl(this, api, key);
  }

  /**
   * newService() creates a new service using a builder.
   */
  /*
  private <T> ServiceBuilderImpl<T> newService()
  {
    return new ServiceBuilderImpl<>(this);
  }
  */

  /**
   * Publish, reusing the mailbox for an existing service.
   */
  @Override
  public ServiceRefAmp bind(ServiceRefAmp service, String address)
  {
    if (log.isLoggable(Level.FINEST)) {
      log.finest(L.l("bind {0} for {1} in {2}",
                    address, service.api().getType(), this));
    }

    address = toCanonical(address);
    
    registry().bind(address, service);

    return service;
  }
  
  public static String toCanonical(String address)
  {
    // canonical:
    //    "/foo" -> "local:///foo"
    //    "foo:/bar" -> "foo:///bar"
    
    if (address.startsWith("/")) {
      address = "local://" + address;
    }
    else {
      int p = address.indexOf("://");
    
      if (p < 0) {
        p = address.indexOf(":");
        
        if (p > 0) {
          String scheme = address.substring(0, p);
          String tail = address.substring(p + 1);
          
          if (tail.startsWith("/")) {
            address = scheme + "://" + tail;
          }
          else if (! tail.isEmpty()) {
            throw new IllegalArgumentException(L.l("'{0}' is an invalid address",
                                                   address));
          }
        }
      }
    }
    
    return address;
  }
  
  @Override
  public ServiceRefAmp pin(ServiceRefAmp context, 
                           Object worker)
  {
    return pin(context, worker, null);
  }
  
  @Override
  public ServiceRefAmp pin(ServiceRefAmp serviceRef,
                           Object worker,
                           String address)
  {
    Objects.requireNonNull(worker);
    
    if (worker instanceof ProxyHandleAmp) {
      ProxyHandleAmp proxy = (ProxyHandleAmp) worker;
      
      return proxy.__caucho_getServiceRef();
    }
    else if (worker instanceof ServiceRef) {
      return (ServiceRefAmp) worker;
    }
    
    ServiceConfig config = ServiceConfig.NULL;
    
    StubAmp stub;
    
    if (worker instanceof StubAmp) {
      stub = (StubAmp) worker;
    }
    else {
      stub = stubFactory().stub(worker, config);
    }
    
    InboxAmp inbox = serviceRef.inbox();
    
    ServiceRefAmp newServiceRef;
    
    if (address != null) {
      newServiceRef = new ServiceRefChild(address, stub, inbox);
    }
    else {
      newServiceRef = new ServiceRefPin(stub, inbox);
    }
    
    return newServiceRef;
  }
  
  @Override
  public <R> ClassValue<Convert<?,R>> shims(Class<R> type)
  {
    return (ClassValue) _shims.get(type);
  }

  /*
  @Override
  public StubAmp createStub(Object bean, ServiceConfig config)
  {
    return createActor(null, bean, config);
  }

  //@Override
  private StubAmp createActor(String path, 
                               Object bean,
                               ServiceConfig config)
  {
    if (bean instanceof StubAmp) {
      return (StubAmp) bean;
    }
    else {
      return stubFactory().stub(bean, path, path, null, config);
    }
  }
  */
  
  protected InboxAmp createSystemInbox()
  {
    String path = getSystemAddress();

    StubAmpSystem actorSystem = new StubAmpSystem(path, this);
                                      
    //ServiceConfig config = ServiceConfig.Builder.create().build();
    //ServiceRefAmp serviceRef = service(actorSystem, path, config);
    ServiceRefAmp serviceRef = newService(actorSystem).ref();
    
    actorSystem.setInbox(serviceRef.inbox());
    
    serviceRef.bind(path);

    return serviceRef.inbox();
  }
  
  protected String getSystemAddress()
  {
    return "system://";
  }
  
  @Override
  public Supplier<OutboxAmp> outboxFactory()
  {
    return _outboxFactory;
  }
  
  @Override
  public ProxyFactoryAmp proxyFactory()
  {
    return _proxyFactory;
  }
  
  @Override
  public StubClassFactoryAmp stubFactory()
  {
    return _stubFactory;
  }

  public StubGenerator []stubGenerators()
  {
    return _stubGenerators;
  }

  @Override
  public JournalAmp openJournal(String name)
  {
    if (_journalDriver != null) {
      return _journalDriver.open(name);
    }
    else {
      return null;
    }
  }

  public JournalAmp journal(String name)
  {
    return _journalDriver.open(name);
  }

  @Override
  public MethodEnsureAmp ensureMethod(MethodAmp method)
  {
    return _ensureDriver.ensure(method);
  }
  
  @Override
  public void addAutoStart(ServiceRef serviceRef)
  {
    if (_lifecycle.isActive() && isAutoStart()) {
      serviceRef.start();
    }
    else {
      _autoStart.add(serviceRef);
    }
    
    // _lazyStart.add(serviceRef);
  }
  
  @Override
  public void autoStart(boolean isAutoStart)
  {
    _isAutoStart = isAutoStart;
    
    autoStart();
  }
  
  private void autoStart()
  {
    if (_lifecycle.isActive() && isAutoStart()) {
      ArrayList<ServiceRefAmp> services = new ArrayList(_autoStart);
      _autoStart.clear();
      
      for (ServiceRefAmp serviceRef : services) {
        serviceRef.start();
      }
    }
  }
  
  @Override
  public boolean isClosed()
  {
    return _lifecycle.isDestroyed();
  }
  
  @Override
  public void start()
  {
    if (! _lifecycle.toStarting()) {
      return;
    }

    ArrayList<ServiceRef> startList = new ArrayList<>(_autoStart);
    _autoStart.clear();
    
    _lifecycle.toActive();

    _ensureDriver.init(this);
    
    for (ServiceRef service : startList) {
      service.start();
    }
    
    // getModule().getSystem().start();
  }
  
  @Override
  public void close()
  {
    shutdown(ShutdownModeAmp.GRACEFUL);
  }
  
  @Override
  public void shutdown(ShutdownModeAmp mode)
  {
    DebugQueryMap debugQueryMap = _debugQueryMap;
    
    if (debugQueryMap != null) {
      debugQueryMap.close();
    }
    
    _registry.shutdown(mode);
    
    _lifecycle.toDestroy();
  }

  /*
  @Override
  public <T> DisruptorBuilder<T> disruptor(Class<T> api)
  {
    //return new DisruptorBuilderTop<T>(this, api, null);
    throw new UnsupportedOperationException();
  }
  */
  
  @Override
  public boolean isDebug()
  {
    return _isDebug;
  }
  
  public DebugQueryMap getDebugQueryMap()
  {
    return _debugQueryMap;
  }
  
  public void setDebug(boolean isDebug)
  {
    _isDebug = isDebug;
  }
  
  @Override
  public Trace trace()
  {
    return new TraceAmp(this);
  }
  
  /**
   * For serialization
   */
  public Object writeReplace()
  {
    return null;
  }
  
  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _name + "]";
  }

  @Override
  public void addRemoteMessageWrite()
  {
    _remoteMessageWriteCount.incrementAndGet();
  }
  
  @Override
  public long getRemoteMessageWriteCount()
  {
    return _remoteMessageWriteCount.get();
  }

  @Override
  public void addRemoteMessageRead()
  {
    _remoteMessageReadCount.incrementAndGet();
  }

  @Override
  public long getRemoteMessageReadCount()
  {
    return _remoteMessageReadCount.get();
  }

  /*
  @Override
  public ContextSession createContextServiceSession(String path, Class<?> beanClass)
  {
    boolean isJournal = beanClass.isAnnotationPresent(Journal.class);
    
    return _channelFactory.create(path, beanClass, isJournal);
  }
  */
  
  public void setConfigException(Throwable exn)
  {
    _configException = exn;
  }
  
  public Throwable getConfigException()
  {
    return _configException;
  }
  
  private static class QueueFullHandlerAmp implements QueueFullHandler
  {
    @Override
    public void onQueueFull(ServiceRef service, 
                            int queueSize, 
                            long timeout,
                            TimeUnit unit, 
                            MessageApi message)
    {
      throw new ServiceExceptionQueueFull(L.l("full queue {0} with {1} entries after {2}ms at message {3}.",
                                            service,
                                            queueSize,
                                            unit.toMillis(timeout),
                                            message));
      
    }
  }
  
  private class ShimFactory extends ClassValue<ClassValue<Convert<?,?>>>
  {
    @Override
    protected ClassValue<Convert<?, ?>> computeValue(Class<?> type)
    {
      return new ShimFactoryClass<>(type);
    }
  }
  
  private class ShimFactoryClass<T> extends ClassValue<Convert<?,?>>
  {
    private Class<T> _targetType;
    
    ShimFactoryClass(Class<T> targetType)
    {
      _targetType = targetType;
    }

    @Override
    protected Convert<?, ?> computeValue(Class<?> type)
    {
      return new ShimConverter<>(type, _targetType);
    }
  }
}
