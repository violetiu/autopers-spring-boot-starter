package org.violetime.autopers.spring.boot;


import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Configuration;
import org.violetime.autopers.mapping.AutopersMappingClass;
import org.violetime.autopers.objects.AutopersObjectsFactory;

/**
 * spring创建代理类工厂。
 * @author taoyo
 *
 * @param <T>
 */
public class AutopersFactoryBean<T> implements FactoryBean<T> {
    public AutopersMappingClass getMappingClass() {
        return mappingClass;
    }

    public void setMappingClass(AutopersMappingClass mappingClass) {

        this.mappingClass = mappingClass;
    }

    private AutopersMappingClass mappingClass;

    private Class<?> cls;

    private Class<?> getCls(){

        if(cls==null&&mappingClass!=null)
            try {
                cls=Class.forName(mappingClass.getClassPath()) ;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.err.println("请检查无效的mapping文件！");
                e.printStackTrace();
            }
        return cls;

    }

    @Override
    public T getObject() throws Exception {
        // TODO Auto-generated method stub
        T obj=(T) AutopersObjectsFactory.newInstanceObject(getCls());
        return obj;
    }

    @Override
    public Class<?> getObjectType() {
        // TODO Auto-generated method stub
        return getCls();
    }

    @Override
    public boolean isSingleton() {
        // TODO Auto-generated method stub
        return true;
    }

}
