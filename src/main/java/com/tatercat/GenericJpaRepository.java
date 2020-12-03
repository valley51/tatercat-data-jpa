package com.tatercat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author guk
 * @Date 12/1/20
 **/
@NoRepositoryBean
public interface GenericJpaRepository <T, ID extends Serializable> extends JpaRepository<T, ID> {

    Map<ID, T> mget(Collection<ID> ids);

    //for cache
    Map<ID, T> mgetOneByOne(Collection<ID> ids);

    //for cache
    List<T> findAllOneByOne(Collection<ID> ids);
}
