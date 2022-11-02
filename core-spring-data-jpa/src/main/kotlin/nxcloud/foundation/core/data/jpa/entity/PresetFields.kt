package nxcloud.foundation.core.data.jpa.entity

import java.time.LocalDateTime

interface CreatedTimeField {

    var createdTime: LocalDateTime

}

interface UpdatedTimeField {

    var updatedTime: LocalDateTime

}

interface DeletedField {

    var deleted: Long

}