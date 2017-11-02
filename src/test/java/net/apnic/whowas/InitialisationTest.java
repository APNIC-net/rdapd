package net.apnic.whowas;

import net.apnic.whowas.loaders.RipeDbLoader;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class InitialisationTest {

    @Test
    public void initialDataIsLoadedBeforeServletIsStarted() {
        ConcurrentLinkedQueue<String> events = new ConcurrentLinkedQueue<>();

        System.setProperty("server.port", "0");
        System.setProperty("management.port", "-1");

        SpringApplication springApplication = new SpringApplication(App.class);
        springApplication.addInitializers(configurableApplicationContext ->
                configurableApplicationContext.addBeanFactoryPostProcessor(beanFactory ->
                        beanFactory.registerResolvableDependency(
                                RipeDbLoader.class, new RipeDbLoader(new JdbcTemplate()) {
                                    @Override
                                    public void loadWith(RevisionConsumer consumer) {
                                        sleep(1000);
                                        events.add("Data loaded");
                                    }
                                }
                        )
                )
        );
        springApplication.addListeners(
                (ApplicationListener<EmbeddedServletContainerInitializedEvent>) applicationEvent ->
                        events.add("Servlet container started")
        );

        try(ConfigurableApplicationContext context = springApplication.run()) {
            assertThat("Data was loaded before tomcat was initialised",
                    new ArrayList<>(events).get(0), is("Data loaded"));
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
