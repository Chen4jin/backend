package com.api.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations.
 * Provides consistent timestamp formatting across the application.
 */
public final class DateTimeUtil {

  private static final DateTimeFormatter ISO_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd");

  private DateTimeUtil() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Gets the current UTC timestamp in ISO 8601 format.
   *
   * @return formatted timestamp string (e.g., "2024-01-15T10:30:00Z")
   */
  public static String getCurrentTimestamp() {
    return ISO_FORMATTER.format(Instant.now());
  }

  /**
   * Gets the current UTC date formatted as yyyyMMdd.
   *
   * @return formatted date string (e.g., "20240115")
   */
  public static String getCurrentDateCompact() {
    return ZonedDateTime.now(ZoneOffset.UTC).format(DATE_FORMATTER);
  }

  /**
   * Formats an Instant to ISO 8601 format.
   *
   * @param instant the instant to format
   * @return formatted timestamp string
   */
  public static String formatTimestamp(Instant instant) {
    return ISO_FORMATTER.format(instant);
  }

  /**
   * Gets the current year-month string for partitioning.
   *
   * @return year-month string (e.g., "2024-01")
   */
  public static String getCurrentYearMonth() {
    return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM"));
  }
}
