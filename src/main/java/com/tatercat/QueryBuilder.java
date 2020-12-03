package com.tatercat;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author guk
 * @Date 12/2/20
 **/
public class QueryBuilder {

    private static final Pattern ORDERBY_PATTERN_1 = Pattern
            .compile("order\\s+by.+?$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static Map<Class, ResultTransformer> transformerCache = new ConcurrentHashMap<>();

    public static <C> Query transform(Query query, Class<C> clazz) {
        ResultTransformer transformer;
        if (Map.class.isAssignableFrom(clazz)) {
            transformer = Transformers.ALIAS_TO_ENTITY_MAP;
        } else if (Number.class.isAssignableFrom(clazz) || clazz.isPrimitive() || String.class.isAssignableFrom(clazz) ||
                Date.class.isAssignableFrom(clazz)) {
            transformer = transformerCache.computeIfAbsent(clazz, SmartTransformer::new);
        } else {
            transformer = transformerCache.computeIfAbsent(clazz, BeanTransformerAdapter::new);
        }
        return query.setResultTransformer(transformer);
    }

    public static SQLQuery toSQLQuery(EntityManager em, String nativeQuery, Object beanOrMap) {
        Session session = em.unwrap(Session.class);
        SQLQuery query = session.createSQLQuery(nativeQuery);
        setParams(query, beanOrMap);
        return query;
    }

    private static String wrapCountQuery(String query) {
        return "select count(*) from (" + query + ") as ctmp";
    }

    private static String cleanOrderBy(String query) {
        Matcher matcher = ORDERBY_PATTERN_1.matcher(query);
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (matcher.find()) {
            String part = matcher.group(i);
            if (canClean(part)) {
                matcher.appendReplacement(sb, "");
            } else {
                matcher.appendReplacement(sb, part);
            }
            i++;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static boolean canClean(String orderByPart) {
        return orderByPart != null && (!orderByPart.contains(")")
                ||
                StringUtils.countOccurrencesOf(orderByPart, ")") == StringUtils.countOccurrencesOf(orderByPart, "("));
    }

    public static String toCountQuery(String query) {
        return wrapCountQuery(cleanOrderBy(query));
    }

    public static void setParams(Query query, Object beanOrMap) {
        String[] nps = query.getNamedParameters();
        if (nps != null) {
            Map<String, Object> params = toParams(beanOrMap);
            for (String key : nps) {
                Object arg = params.get(key);
                if (arg == null) {
                    query.setParameter(key, null);
                } else if (arg.getClass().isArray()) {
                    query.setParameterList(key, (Object[]) arg);
                } else if (arg instanceof Collection) {
                    query.setParameterList(key, ((Collection) arg));
                } else if (arg.getClass().isEnum()) {
                    query.setParameter(key, ((Enum) arg).ordinal());
                } else {
                    query.setParameter(key, arg);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toParams(Object beanOrMap) {
        Map<String, Object> params;
        if (beanOrMap instanceof Map) {
            params = (Map<String, Object>) beanOrMap;
        } else {
            params = toMap(beanOrMap);
        }
        if (!CollectionUtils.isEmpty(params)) {
            Iterator<String> keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!isValidValue(params.get(key))) {
                    keys.remove();
                }
            }
        }
        return params;
    }

    public static boolean isValidValue(Object object) {
        if (object == null) {
            return false;
        }
        /*if (object instanceof Number && ((Number) object).longValue() == 0) {
            return false;
		}*/
        return !(object instanceof Collection && CollectionUtils.isEmpty((Collection<?>) object));
    }

    public static Map<String, Object> toMap(Object bean) {
        if (bean == null) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> description = new HashMap<String, Object>();
            if (bean instanceof DynaBean) {
                DynaProperty[] descriptors = ((DynaBean) bean).getDynaClass().getDynaProperties();
                for (DynaProperty descriptor : descriptors) {
                    String name = descriptor.getName();
                    description.put(name, BeanUtils.getProperty(bean, name));
                }
            } else {
                PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(bean);
                for (PropertyDescriptor descriptor : descriptors) {
                    String name = descriptor.getName();
                    if (PropertyUtils.getReadMethod(descriptor) != null) {
                        description.put(name, PropertyUtils.getNestedProperty(bean, name));
                    }
                }
            }
            return description;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
