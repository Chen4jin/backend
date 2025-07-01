package com.api.common;

import java.net.URI;
import java.util.Map;
import software.amazon.awssdk.regions.Region;

public class Constant {
  public static final Region region = Region.US_EAST_1;
  public static final URI cloudFront = URI.create("https://d3bjrjf10s3vbi.cloudfront.net/");
  public static final Map<String, String> mimeToExt =
      Map.of(
          "image/jpeg", ".jpg",
          "image/png", ".png");

  public static Region getRegion() {
    return region;
  }

  public static String getCloudFront() {
    return cloudFront.toString();
  }

  public static Map<String, String> getMimeToExt() {
    return mimeToExt;
  }
}
