package com.tatercat;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @Author guk
 * @Date 12/2/20
 **/
@Order(Ordered.HIGHEST_PRECEDENCE)
public interface EntityAssembler<T> {

    /**
     * 组装
     *
     * @param bean bean
     */
    void assemble(T bean);

    /**
     * 批量组装
     *
     * @param beans beans
     */
    void massemble(Iterable<T> beans);
}
