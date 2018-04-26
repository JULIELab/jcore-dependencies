package de.julielab.jcore.db.test;

import de.julielab.xmlData.Constants;
import de.julielab.xmlData.dataBase.DataBaseConnector;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileBased;
import org.apache.commons.configuration2.io.FileHandler;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

public class DBTestUtils {
    /**
     * Writes a CoStoSys test configuration to <code>src/test/resources/testconfig.xml</code>.
     * @param schemaName The data table schema to use.
     * @param maxActiveConnections The DB connection pool size.
     * @param postgres The org.testcontainers postgres test container.
     * @return The path to the CoStoSys configuration file.
     * @throws ConfigurationException If the creation of the configuration file failed.
     */
    public static String createTestCostosysConfig(String schemaName, int maxActiveConnections, PostgreSQLContainer postgres) throws ConfigurationException {
        return createTestCostosysConfig("src/test/resources/testconfig.xml", schemaName, maxActiveConnections, postgres);
    }
    /**
     * Writes a CoStoSys test configuration to <code>targetPath</code>.
     * @param targetPath The path to write the configuration file to.
     * @param schemaName The data table schema to use.
     * @param maxActiveConnections The DB connection pool size.
     * @param postgres The org.testcontainers postgres test container.
     * @return The path to the CoStoSys configuration file.
     * @throws ConfigurationException If the creation of the configuration file failed.
     */
     public static String createTestCostosysConfig(String targetPath, String schemaName, int maxActiveConnections, PostgreSQLContainer postgres) throws ConfigurationException {
        XMLConfiguration costosysconfig = new XMLConfiguration();
        costosysconfig.setProperty("databaseConnectorConfiguration.DBSchemaInformation.activeTableSchema",schemaName);
        costosysconfig.setProperty("databaseConnectorConfiguration.DBConnectionInformation.activeDBConnection", postgres.getDatabaseName());
        costosysconfig.setProperty("databaseConnectorConfiguration.DBConnectionInformation.DBConnections.DBConnection[@name]", postgres.getDatabaseName());
        costosysconfig.setProperty("databaseConnectorConfiguration.DBConnectionInformation.DBConnections.DBConnection[@url]", postgres.getJdbcUrl());
        costosysconfig.setProperty("databaseConnectorConfiguration.DBConnectionInformation.maxActiveDBConnections", maxActiveConnections);

        FileHandler fh = new FileHandler((FileBased) costosysconfig);
        String costosysConfig = targetPath;
        fh.save(costosysConfig);
        return costosysConfig;
    }

    /**
     * Imports the file at <code>datapath</code> into the empty database, creates a subset
     * named "testsubset" of the specified size by random selection, writes a test credentials file and configures
     * the CoStoSys to use it.
     * @param postgres The org.testcontainer with the Postgres database.
     * @throws SQLException If a database operation failes.
     */
    public static String setupDatabase(String datapath, String dataTableSchema, int subsetTableSize, PostgreSQLContainer postgres) throws SQLException {
        DataBaseConnector dbc = new DataBaseConnector(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        dbc.setActiveTableSchema(dataTableSchema);
        dbc.createTable(Constants.DEFAULT_DATA_TABLE_NAME, "Test data table for DBReaderTest.");
        dbc.importFromXMLFile(datapath, Constants.DEFAULT_DATA_TABLE_NAME);
        String testsubset = "testsubset";
        dbc.createSubsetTable(testsubset, Constants.DEFAULT_DATA_TABLE_NAME, "Test subset");
        dbc.initRandomSubset(subsetTableSize, testsubset, Constants.DEFAULT_DATA_TABLE_NAME);
        String hiddenConfigPath = "src/test/resources/hiddenConfig.txt";
        try (BufferedWriter w = new BufferedWriter(new FileWriter(hiddenConfigPath))) {
            w.write(postgres.getDatabaseName());
            w.newLine();
            w.write(postgres.getUsername());
            w.newLine();
            w.write(postgres.getPassword());
            w.newLine();
            w.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.setProperty(Constants.HIDDEN_CONFIG_PATH, hiddenConfigPath);
        dbc.close();
        return testsubset;
    }
}
