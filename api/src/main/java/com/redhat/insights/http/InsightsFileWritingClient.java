/* Copyright (C) Red Hat 2023 */
package com.redhat.insights.http;

import static com.redhat.insights.InsightsErrorCode.ERROR_WRITING_FILE;

import com.redhat.insights.InsightsException;
import com.redhat.insights.InsightsReport;
import com.redhat.insights.config.InsightsConfiguration;
import com.redhat.insights.logging.InsightsLogger;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class InsightsFileWritingClient implements InsightsHttpClient {
  private final InsightsLogger logger;
  private final InsightsConfiguration config;

  public InsightsFileWritingClient(InsightsLogger logger, InsightsConfiguration config) {
    this.logger = logger;
    this.config = config;
  }

  @Override
  public void decorate(InsightsReport report) {
    report.decorate("app.transport.type.file", "rhel");
  }

  @Override
  public void sendInsightsReport(String filename, InsightsReport report) {
    decorate(report);
    String reportJson = report.serialize();
    //    logger.debug(reportJson);

    // Can't reuse upload path - as this may be called as part of fallback
    Path p = Paths.get(config.getArchiveUploadDir(), filename);
    try {
      Files.write(
          p,
          reportJson.getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.WRITE,
          StandardOpenOption.CREATE);
    } catch (IOException iox) {
      throw new InsightsException(ERROR_WRITING_FILE, "Could not write to: " + p, iox);
    }
  }

  @Override
  public boolean isReadyToSend() {
    return (new File(config.getCertFilePath()).exists()
        && new File(config.getKeyFilePath()).exists());
  }
}
