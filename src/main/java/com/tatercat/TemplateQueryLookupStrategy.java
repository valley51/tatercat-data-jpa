package com.tatercat;

import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.*;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 * @Author guk
 * @Date 12/1/20
 **/
public class TemplateQueryLookupStrategy implements QueryLookupStrategy {

    private QueryLookupStrategy jpaQueryLookupStrategy;

    private final EntityManager entityManager;

    private QueryExtractor extractor;

    private JpaQueryMethodFactory jpaQueryMethodFactory;

    public TemplateQueryLookupStrategy(EntityManager entityManager, Key key,QueryExtractor extractor,
                                       QueryMethodEvaluationContextProvider evaluationContextProvider, EscapeCharacter escape) {
        this.extractor = extractor;
        this.entityManager = entityManager;
        jpaQueryMethodFactory = new DefaultJpaQueryMethodFactory(extractor);
        this.jpaQueryLookupStrategy = JpaQueryLookupStrategy.create(entityManager, jpaQueryMethodFactory, key,evaluationContextProvider,escape);
    }

    public static QueryLookupStrategy create(EntityManager entityManager, Key key,
                                             QueryMethodEvaluationContextProvider evaluationContextProvider,QueryExtractor extractor) {
        return new TemplateQueryLookupStrategy(entityManager, key,extractor,evaluationContextProvider,EscapeCharacter.DEFAULT);
    }


    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
        if (method.getAnnotation(TemplateQuery.class) == null) {
            return jpaQueryLookupStrategy.resolveQuery(method, metadata, factory, namedQueries);
        } else {
            return new FreemarkerTemplateQuery(new JpaQueryMethodExtend(method, metadata, factory, extractor), entityManager);
        }
    }
}
