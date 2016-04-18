package server.utils;

import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

/**
 * Created by zfh on 16-3-17.
 */
public class LoggerGenerator {
    private Logger logger;

    public LoggerGenerator(String loggerName) {
        logger = Logger.getLogger(loggerName);
        logger.setLevel(Level.ALL);
        try {
            FileHandler fileHandler = new FileHandler(loggerName + ".log");
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new LogFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Logger generate() {
        return logger;
    }

    class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return "[" + new Date().toString() + "]" +
                    "[" + record.getLevel() + "]" +
                    record.getClass() + record.getMessage() + "\n";
        }
    }
}

