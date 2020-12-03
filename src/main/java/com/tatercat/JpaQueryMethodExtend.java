package com.tatercat;

import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;

import java.lang.reflect.Method;

/**
 * @Author guk
 * @Date 12/2/20
 **/
public class JpaQueryMethodExtend extends JpaQueryMethod {
    protected JpaQueryMethodExtend(Method method, RepositoryMetadata metadata, ProjectionFactory factory, QueryExtractor extractor) {
        super(method, metadata, factory, extractor);
    }
}
