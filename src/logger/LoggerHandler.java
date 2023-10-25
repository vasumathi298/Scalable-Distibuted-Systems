package logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The `LoggerHandler` class provides a utility to initialize and configure a logger with a specified file handler.
 */
public class LoggerHandler {

  /**
   * Initializes the logger with a file handler and custom log message formatting.
   *
   * @param logger   The `Logger` instance to be initialized.
   * @param fileName The name of the log file where log messages will be written.
   */
  public static void initLogger(Logger logger, String fileName) {
    try {
      // Create a file handler for logging to the specified file.
      FileHandler fileHandler = new FileHandler(fileName);

      // Define a custom log message formatter.
      Formatter customFormatter = new Formatter() {
        @Override
        public String format(LogRecord record) {
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

          String formattedMessage = String.format("[%s] %s - %s%n",
                  dateFormat.format(new Date(record.getMillis())),
                  record.getLevel().getName(),
                  record.getMessage());

          return formattedMessage;
        }
      };

      // Set the custom formatter for the file handler.
      fileHandler.setFormatter(customFormatter);

      // Add the file handler to the logger.
      logger.addHandler(fileHandler);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
