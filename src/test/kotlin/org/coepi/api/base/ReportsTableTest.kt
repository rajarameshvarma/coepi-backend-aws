package org.coepi.api.base

import com.amazonaws.services.dynamodbv2.datamodeling.*

@DynamoDBTable(tableName = "Report")
data class ReportsTableTest(

        @get:DynamoDBHashKey(attributeName = "did")
        var did: String? = null,

        @get:DynamoDBRangeKey(attributeName = "reportTimestamp")
        var reportTimestamp: String? = null
)
