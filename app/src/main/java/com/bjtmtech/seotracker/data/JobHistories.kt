package com.bjtmtech.seotracker.data

data class JobHistoryData(
//    let it match firestore name convension
    var id: String? = null,
    var customerName: String? = null,
    var startDate: String? = null,
    var stopDate: String? = null,
    var createdDate: String? = null,
    var engineerName: String? = null,
    var jobDuration: String? = null,
    var jobStatus: String? = null,
    var jobType: String? = null,
    var customerCountry: String? = null,
    var visibility: Boolean = false

)

