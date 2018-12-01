package util;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Util{
  public static Timestamp convertStringToTimestamp(LocalDateTime str_date) {
      DateTimeFormatter formatter;
      formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
       // you can change format of date
      Timestamp timeStampDate = Timestamp.valueOf(str_date);

      return timeStampDate;
  }
}