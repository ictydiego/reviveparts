package br.unasp.reviveparts.data.db

import androidx.room.TypeConverter
import br.unasp.reviveparts.domain.model.*

class Converters {
    @TypeConverter fun roleToStr(r: Role): String = r.name
    @TypeConverter fun strToRole(s: String): Role = Role.valueOf(s)
    @TypeConverter fun statusToStr(s: OrderStatus): String = s.name
    @TypeConverter fun strToStatus(s: String): OrderStatus = OrderStatus.valueOf(s)
    @TypeConverter fun ptToStr(p: PaymentType): String = p.name
    @TypeConverter fun strToPt(s: String): PaymentType = PaymentType.valueOf(s)
    @TypeConverter fun srcToStr(s: OrderSource): String = s.name
    @TypeConverter fun strToSrc(s: String): OrderSource = OrderSource.valueOf(s)
}
