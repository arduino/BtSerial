import java.text.SimpleDateFormat;

//get the phone time NOW in UTC (Greenwich Mean Time), and return it as a String formatted like "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
String getNowUTC() {
  SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  timeFormat.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
  Date currentTime = new Date();
  return timeFormat.format(currentTime);
}
