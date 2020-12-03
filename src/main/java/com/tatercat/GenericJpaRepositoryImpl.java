package com.tatercat;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @Author guk
 * @Date 12/1/20
 **/
public class GenericJpaRepositoryImpl <T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID> implements GenericJpaRepository<T, ID>, Serializable {

    private JpaEntityInformation<T,ID> entityInformation;
    private EntityManager entityManager;

    private boolean isStatusAble = false;

    private Method statusReadMethod;

    private Method statusWriteMethod;

    public GenericJpaRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
        PropertyDescriptor descriptor = findFieldPropertyDescriptor(entityInformation.getJavaType(), Status.class);
        isStatusAble = descriptor != null;
        if (isStatusAble) {
            statusReadMethod = descriptor.getReadMethod();
            statusWriteMethod = descriptor.getWriteMethod();
        }

    }

    @Override
    public Map<ID, T> mget(Collection<ID> ids) {
        return toMap(findAllById(ids));
    }

    @Override
    public Map<ID, T> mgetOneByOne(Collection<ID> ids) {
        return toMap(findAllOneByOne(ids));
    }

    @Override
    public List<T> findAllOneByOne(Collection<ID> ids) {
        List<T> results = new ArrayList<>();
        for (ID id : ids) {
            findById(id).ifPresent(results::add);
        }
        return results;
    }


    private Map<ID, T> toMap(List<T> list) {
        Map<ID, T> result = new LinkedHashMap<>();
        for (T t : list) {
            if (t != null) {
                result.put(entityInformation.getId(t), t);
            }
        }
        return result;
    }

    private PropertyDescriptor findFieldPropertyDescriptor(Class target, Class fieldClass) {
        PropertyDescriptor[] propertyDescriptors = org.springframework.beans.BeanUtils.getPropertyDescriptors(target);
        for (PropertyDescriptor pd : propertyDescriptors) {
            if (pd.getPropertyType() == fieldClass) {
                return pd;
            }
        }
        return null;
    }
}
