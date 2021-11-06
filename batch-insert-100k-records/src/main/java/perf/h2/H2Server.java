package perf.h2;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * H2ConsoleAutoConfiguration will not be auto executed for spring webflux & netty
 * (reactor based), as it is only available to servlet-based-application, such as
 * tomcat, jetty, or undertow. That is why this class is needed to manually start
 * h2 server internally
 */
@Slf4j
@ConditionalOnProperty(name = "app.h2.port")
@Component
public class H2Server {
    @Value("${app.h2.port}")
    private int h2ConsolePort;

    private Server h2Server;

    @EventListener(ContextRefreshedEvent.class)
    public void start() throws SQLException {
        log.info("starting h2 console at port: {}", h2ConsolePort);
        h2Server = Server.createWebServer("-webPort", String.valueOf(h2ConsolePort), "-tcpAllowOthers").start();
        log.info("h2 console url: {}", h2Server.getURL());
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
        log.info("stopping h2 console at port: {}", h2ConsolePort);
        h2Server.stop();
    }
}
