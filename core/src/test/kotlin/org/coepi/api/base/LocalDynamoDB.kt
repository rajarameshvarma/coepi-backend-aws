package org.coepi.api.base

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner

class LocalDynamoDB {

    fun startDynamoDb(){
        val localUrl= "http://localhost:9000"
        val region = "us-west-2"

        println("Trying to start dynamo db at $localUrl")
        System.setProperty("sqlite4java.library.path", "./build/libs")
        val server = ServerRunner.createServerFromCommandLineArgs(arrayOf("-inMemory", "-port", "9000"))
        server.start()

        val ddb = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(localUrl, region))
                .build()
        var ddbMapper = DynamoDBMapper(ddb)
    }
}