package nxcloud.foundation.core.data.jpa.entity

interface LongPK

interface LongIdPK : LongPK {

    var id: Long

}