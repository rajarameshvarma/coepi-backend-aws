package org.coepi.api.dao

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner
import com.amazonaws.services.dynamodbv2.model.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.charset.Charset
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * These tests are only reproducible in a specific state and rely on DDB Web Service.
 * In other words they are badly written.
 *
 * TODO: Use DynamoDBLocal to make the tests re-producible on local devboxes.
 *
 * Until then, don't expect them to succeed, just use them as documentation
 */

@Disabled
class ReportsDaoTest {

    private val dao: ReportsDao = ReportsDao()
    val cenKeys = arrayOf("foo", "bar")
    val reportData = "foobar".toByteArray(Charset.defaultCharset())

    companion object{

        val LOCAL_URL= "http://localhost:9000";
        val REGION = "us-west-2"

        @BeforeAll
        @JvmStatic
        internal fun startDynamoDb(){
            System.setProperty("sqlite4java.library.path", "./build/libs")
            val server = ServerRunner.createServerFromCommandLineArgs(arrayOf("-inMemory", "-port", "9000"))
            server.start();

            val dynamoClient = AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(LOCAL_URL, REGION))
                    .build();
            val dynamoDb = DynamoDB(dynamoClient)

            val table = dynamoDb.createTable(
                    "Reports",
                    listOf(
                            KeySchemaElement("did", KeyType.HASH),
                            KeySchemaElement("reportTimestamp", KeyType.RANGE)
                    ),
                    listOf(
                            AttributeDefinition("did", ScalarAttributeType.N),
                            AttributeDefinition("reportTimestamp", ScalarAttributeType.S)
                    ),
                    ProvisionedThroughput(10L, 10L)
            )

            table.waitForActive()
        }
    }


    @Test
    fun addReport_sanity() {
        dao.addReport(cenKeys, reportData)

        val reports = dao.queryReports(Optional.empty(), Optional.empty())
        Assertions.assertThat(reports.isNotEmpty())
        println(reports.joinToString("\n"))
    }

    @Test
    fun queryReport_acrossMultipleDays() {
        dao.addReport(cenKeys, reportData, Instant.now().minus(30, ChronoUnit.HOURS).toEpochMilli())
        dao.addReport(cenKeys, reportData, Instant.now().minus(32, ChronoUnit.HOURS).toEpochMilli())
        dao.addReport(cenKeys, reportData, Instant.now().minus(38, ChronoUnit.HOURS).toEpochMilli())

        val reports = dao.queryReports(Optional.of(Instant.now().minus(2, ChronoUnit.DAYS)
                .toEpochMilli()), Optional.of(System.currentTimeMillis()))
        Assertions.assertThat(reports.isNotEmpty())
        println(reports.joinToString("\n"))
    }

    @Test
    fun queryReport_sameDay_lowerTimestamp() {
        val reports = dao.queryReports(Optional.of(1586124310923), Optional.empty())
        Assertions.assertThat(reports.isNotEmpty())
        println(reports.joinToString("\n"))
    }

    @Test
    fun queryReport_sameDay_multipleTimestamps() {
        val reports = dao.queryReports(Optional.of(1586124294148), Optional.of(1586124315988))
        Assertions.assertThat(reports.isNotEmpty())
        println(reports.joinToString("\n"))
    }

    @Test
    fun queryReport_acrossMultipleDays_multipleTimestamps() {
        val reports = dao.queryReports(Optional.of(1585989152678), Optional.of(1586124315982))
        Assertions.assertThat(reports.isNotEmpty())
        println(reports.joinToString("\n"))
    }
}
