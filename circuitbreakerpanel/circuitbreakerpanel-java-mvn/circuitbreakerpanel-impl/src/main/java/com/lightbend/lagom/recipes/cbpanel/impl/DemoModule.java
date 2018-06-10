package com.lightbend.lagom.recipes.cbpanel.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.lagom.recipes.cbpanel.api.DemoService;
import com.lightbend.lagom.recipes.cbpanel.impl.repository.Repository;
import com.lightbend.lagom.recipes.cbpanel.impl.repository.RepositoryImpl;

public class DemoModule extends AbstractModule implements ServiceGuiceSupport {
    
    @Override
    protected void configure() {
        bindService(DemoService.class, DemoServiceImpl.class);
        bind(Repository.class).to(RepositoryImpl.class);
    }
}
