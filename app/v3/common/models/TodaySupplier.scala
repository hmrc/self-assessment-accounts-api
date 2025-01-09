package v3.common.models

import java.time.{LocalDate, ZoneOffset}
import javax.inject.Singleton

@Singleton
class TodaySupplier {
  def today(): LocalDate = LocalDate.now(ZoneOffset.UTC)
}
