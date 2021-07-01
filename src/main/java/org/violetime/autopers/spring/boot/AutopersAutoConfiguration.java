package org.violetime.autopers.spring.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.violetime.autopers.cache.AutopersCacheRunnable;
import org.violetime.autopers.creator.AutoWriteFactory;
import org.violetime.autopers.creator.AutopersApplication;
import org.violetime.autopers.context.LoadContext;
import org.violetime.autopers.context.LoadDataType;
import org.violetime.autopers.context.LoadObjectXml;
import org.violetime.autopers.context.LoadPlatform;
import org.violetime.autopers.database.DataBaseFactory;
import org.violetime.autopers.mapping.AutopersMapping;
import org.violetime.autopers.mapping.AutopersMappingClass;
import org.violetime.autopers.session.AutopersSessionPool;
import org.violetime.autopers.session.AutopersSessionPoolThread;
import org.violetime.autopers.units.AutopersCodeName;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * org.springframework.boot.autoconfigure.EnableAutoConfiguration=org.violetime.autopers.spring.boot.AutopersAutoConfiguration
 */
@Component
@Configuration
@ConditionalOnProperty(prefix = "autopers", value = "enabled", matchIfMissing = true)
public class AutopersAutoConfiguration implements ApplicationRunner {
    private static final Logger logger = Logger.getLogger("Autopers");

    @Autowired
    private AutopersProperties properties;

    public AutopersProperties getProperties() {
        return properties;
    }

    public void setProperties(AutopersProperties properties) {
        this.properties = properties;
    }
    @Override
    public void run(ApplicationArguments args) throws Exception {

        logger.info("--------------Autopers Loadding-------------------");

        String path=ResourceUtils.getURL("classpath:").getPath();

        InputStream contextFile=null;

        InputStream dataFile=null;

        InputStream platformFile=null;



        if(path.contains("jar!")){
             logger.info("jar 启动");
            contextFile=new ClassPathResource("autopersContext.xml").getInputStream();
            dataFile=new ClassPathResource("autopersDataType.xml").getInputStream();
            platformFile=new ClassPathResource("autopersPlatform.xml").getInputStream();

        }else{
            logger.info("maven 启动");
            contextFile=new FileInputStream( ResourceUtils.getFile("classpath:autopersContext.xml"));
            dataFile=new FileInputStream(ResourceUtils.getFile("classpath:autopersDataType.xml"));
            platformFile=new FileInputStream(ResourceUtils.getFile("classpath:autopersPlatform.xml"));

        }


        if(properties.getModel().toLowerCase().equals("project")){
            AutopersSessionPool.isProjectModel=true;
        }

        LoadPlatform platform = new LoadPlatform();
        platform.load(platformFile);

        LoadDataType loadDataType = new LoadDataType();
        HashMap<String, String> dataTypeMap = loadDataType.load(dataFile);

        LoadContext context = new LoadContext();
        context.load(contextFile);

        AutoWriteFactory.getAutoWriteObjectXml().setDataTypeMap(dataTypeMap);




        AutopersSessionPool.init();
        AutopersSessionPoolThread autopersSessionPoolThread = new AutopersSessionPoolThread();
        autopersSessionPoolThread.start();
        AutopersCacheRunnable cacheRunnable=new AutopersCacheRunnable();
        cacheRunnable.run();
        DataBaseFactory.initDataBaseSourceTables();
        logger.info("--------------Autopers Bean-------------------");
        List<InputStream> mappings=new ArrayList<>();
        DataBaseFactory.getDataBaseSourceMap().forEach((key,surce)->{
            surce.getTables().forEach((table)->{
                if(table.getTableName().split("\\$").length<2){
                    if(path.contains("jar!")){
                        try {
                            InputStream is =new ClassPathResource("mapping/"+ AutopersCodeName.className(table.getTableName())+".xml").getInputStream();
                            mappings.add(is);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }else{
                        try {
                            InputStream is =new FileInputStream(ResourceUtils.getFile("classpath:mapping/"+AutopersCodeName.className(table.getTableName())+".xml"));
                            mappings.add(is);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        });
        LoadObjectXml objectXml = new LoadObjectXml();
         objectXml.autoLoad(mappings);

//        SpringContextUtil
//        ConfigurableApplicationContext run = SpringApplication.run(Application.class);
//        DefaultListableBeanFactory autowireCapableBeanFactory = (DefaultListableBeanFactory) run.getAutowireCapableBeanFactory();
//        for (AutopersMappingClass mappingClass : AutopersMapping.getMappingClass()) {
//            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(mappingClass.getClassPath());
//            AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
//            beanDefinition.setLazyInit(true);
//            beanDefinition.getPropertyValues().add("mappingClass", mappingClass);
//            beanDefinition.setBeanClass(AutopersFactoryBean.class);
//            beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
//            arg0.registerBeanDefinition(mappingClass.getClassPath(), beanDefinition);
//            logger.info( mappingClass.getClassPath().substring( mappingClass.getClassPath().lastIndexOf(".")+1)+" bean registered successfully");
//        }




        logger.info("--------------Autopers Loaded-------------------");



    }
}
