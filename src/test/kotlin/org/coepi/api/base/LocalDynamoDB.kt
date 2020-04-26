package org.coepi.api.base

import com.amazonaws.AmazonServiceException
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner
import com.amazonaws.services.dynamodbv2.model.*
import org.coepi.api.dao.ReportsDaoTest

class LocalDynamoDB {

    fun startDynamoDb(){
        val localUrl= "http://localhost:9000";
        val region = "us-west-2"

        println("Trying to start dynamo db at $localUrl")
        System.setProperty("sqlite4java.library.path", "./build/libs")
        val server = ServerRunner.createServerFromCommandLineArgs(arrayOf("-inMemory", "-port", "9000"))
        server.start();

        val ddb = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(localUrl, region))
                .build();
        var ddbMapper = DynamoDBMapper(ddb);
        createTable(ddb, ddbMapper)

    }

    /**
     * TODO: Make it generic to create table based on class name passed to this method.
     */
    private fun createTable(ddb: AmazonDynamoDB, ddbMapper: DynamoDBMapper){
        val request = ddbMapper.generateCreateTableRequest(ReportsTableTest::class.java)
                .withProvisionedThroughput(ProvisionedThroughput(10, 10))
        try{
            ddb.createTable(request)
            println("Created Reports table for test")
            //fireTestQuery(ddb) // TODO: Invoke helper method to check the vales in the initialized table
        } catch (ex: AmazonServiceException){
            println(ex.errorMessage)
        }
    }

    private fun fireTestQuery(ddb: AmazonDynamoDB){
        val key = HashMap<String, AttributeValue>();
        val request = GetItemRequest()
                .withKey(key)
                .withTableName("Report")
        try{
            val item = ddb.getItem(request)
            if(item != null){
                println(item)
            }
        }catch (ex: AmazonServiceException){
            println(ex.errorMessage)
        }
    }
}