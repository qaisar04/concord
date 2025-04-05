package kz.concord.concord_mongo_autoconfigure.monitoring;

import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MongoLoggingCommandListener implements CommandListener {

    private final MongoLogLevel level;

    private void logMessage(String message) {
        switch (level) {
            case TRACE -> log.trace(message);
            case DEBUG -> log.debug(message);
            case INFO -> log.info(message);
            case WARN -> log.warn(message);
            case ERROR -> log.error(message);
        }
    }

    @Override
    public void commandStarted(CommandStartedEvent event) {
        logMessage("[Mongo] Command started: %s -> %s".formatted(event.getCommandName(), event.getCommand().toJson()));
    }

    @Override
    public void commandSucceeded(CommandSucceededEvent event) {
        logMessage("[Mongo] Command succeeded: %s in %dms".formatted(
                event.getCommandName(), event.getElapsedTime(TimeUnit.MILLISECONDS)));
    }

    @Override
    public void commandFailed(CommandFailedEvent event) {
        logMessage("[Mongo] Command failed: %s -> %s".formatted(
                event.getCommandName(), event.getThrowable().getMessage()));
    }
}
