package com.spectralogic.dsl.helpers

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy
import ch.qos.logback.core.rolling.TriggeringPolicy
import ch.qos.logback.core.util.FileSize
import com.spectralogic.dsl.Tool
import org.slf4j.LoggerFactory

import java.nio.file.FileSystems

class LogRecorder {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(Tool.class)
    private final static String LOG_FORMAT_PATTERN = "%d{yyyy-MM-dd HH:mm:ss} %-5level %msg%n"
    private final static String LOG_ARCHIVE_FILE_PATTERN = "spectra%i.log"
    private final static String LOG_FILE_NAME = "spectra.log"

    static configureLogging(Level level) {
        def loggerContext = LOGGER.getLoggerContext()
        loggerContext.reset()
        LOGGER.setLevel(level)

        if (level != Level.OFF && !Globals.logDir.empty) {
            final fileAppender = new RollingFileAppender<>()
            final sizeBasedRollingPolicy = new FixedWindowRollingPolicy()
            final sizeBasedTriggeringPolicy = new SizeBasedTriggeringPolicy<>()

            fileAppender.setContext(loggerContext)
            sizeBasedTriggeringPolicy.setContext(loggerContext)
            sizeBasedRollingPolicy.setContext(loggerContext)
            fileAppender.setRollingPolicy(sizeBasedRollingPolicy)
            sizeBasedRollingPolicy.setParent(fileAppender)
            sizeBasedRollingPolicy.setMinIndex(0)
            sizeBasedRollingPolicy.setMaxIndex(5)

            def logFilePath = FileSystems.getDefault().getPath(Globals.logDir, LOG_FILE_NAME)
            fileAppender.setFile(logFilePath.toString())
            sizeBasedRollingPolicy.setFileNamePattern(Globals.logDir + LOG_ARCHIVE_FILE_PATTERN)
            sizeBasedRollingPolicy.start()

            sizeBasedTriggeringPolicy.setMaxFileSize(FileSize.valueOf("10MB"))
            sizeBasedTriggeringPolicy.start()

            final PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder()
            fileEncoder.setContext(loggerContext)
            fileEncoder.setPattern(LOG_FORMAT_PATTERN)
            fileEncoder.start()

            fileAppender.setTriggeringPolicy((TriggeringPolicy) sizeBasedTriggeringPolicy)
            fileAppender.setRollingPolicy(sizeBasedRollingPolicy)
            fileAppender.setEncoder(fileEncoder)
            fileAppender.setName("LOGFILE")
            sizeBasedRollingPolicy.start()

            LOGGER.addAppender(fileAppender)
            fileAppender.start()

            LOGGER.info("Logging enabled.")
        }
    }

    static String loggerStatus() {
        if (Globals.logDir.empty) {
            return Globals.getString('set_log_message')
        } else {
            def status = LOGGER.level == Level.OFF ? Globals.getString('disabled') : Globals.getString('enabled')
            return "Logging to ${Globals.logDir} is ${status}."
        }
    }

}
