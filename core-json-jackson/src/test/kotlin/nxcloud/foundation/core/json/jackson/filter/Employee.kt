package nxcloud.foundation.core.json.jackson.filter

import nxcloud.foundation.core.json.annotation.NXJsonSceneFilter

@NXJsonSceneFilter(["test"])
class Employee(
    var name: String,

    @NXJsonSceneFilter(["test"])
    var age: Int,

    var interests: List<String>,
)