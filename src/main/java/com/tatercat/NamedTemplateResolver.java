package com.tatercat;

import org.springframework.core.io.Resource;

import java.util.Iterator;

/**
 * @Author guk
 * @Date 12/2/20
 **/
public interface NamedTemplateResolver {

    Iterator<Void> doInTemplateResource(Resource resource, final NamedTemplateCallback callback) throws Exception;
}
