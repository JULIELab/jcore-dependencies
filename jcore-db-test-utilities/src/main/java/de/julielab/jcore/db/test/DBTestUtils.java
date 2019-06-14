package de.julielab.jcore.db.test;

import de.julielab.costosys.Constants;
import de.julielab.costosys.dbconnection.DataBaseConnector;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
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
        return createTestCostosysConfig("src/test/resources/testconfig-"+System.nanoTime() + ".xml", schemaName, maxActiveConnections, postgres);
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

        FileHandler fh = new FileHandler(costosysconfig);
        String costosysConfig = targetPath;
        fh.save(costosysConfig);
        return costosysConfig;
    }

    /**
     * Imports the file at <code>datapath</code> into the empty database, creates a subset
     * named "testsubset" of the specified size by random selection, writes a test credentials file and configures
     * the CoStoSys to use it.
     * @param datapath The file or directory with test data to import into the postgres container.
     * @param dataTableSchema  The CoStoSys table schema for the imported data and the table it is imported into.
     * @param subsetTableSize The size of the test subset that will be created for the test data table.
     * @param postgres The org.testcontainer with the Postgres database.
     * @throws SQLException If a database operation failes.
     */
    public static String setupDatabase(String datapath, String dataTableSchema, int subsetTableSize, PostgreSQLContainer postgres) throws SQLException {
        DataBaseConnector dbc = getDataBaseConnector(postgres);
        String testsubset = setupDatabase(dbc, datapath, dataTableSchema, subsetTableSize, postgres);
        dbc.close();
        return testsubset;
    }

    /**
     * Imports the file at <code>datapath</code> into the empty database, creates a subset
     * named "testsubset" of the specified size by random selection, writes a test credentials file to "src/test/resources/hiddenConfig.txt" and configures
     * the CoStoSys to use it.
     * @param dbc A database connector connected to the postgres container.
     * @param datapath The file or directory with test data to import into the postgres container.
     * @param dataTableSchema  The CoStoSys table schema for the imported data and the table it is imported into.
     * @param subsetTableSize The size of the test subset that will be created for the test data table.
     * @param postgres The org.testcontainer with the Postgres database.
     * @throws SQLException If a database operation failes.
     */
    public static String setupDatabase(DataBaseConnector dbc, String datapath, String dataTableSchema, int subsetTableSize, PostgreSQLContainer postgres) throws SQLException {
        dbc.setActiveTableSchema(dataTableSchema);
        dbc.createTable(Constants.DEFAULT_DATA_TABLE_NAME, "Test data table for DBReaderTest.");
        dbc.importFromXMLFile(datapath, Constants.DEFAULT_DATA_TABLE_NAME);
        String testsubset = "testsubset";
        dbc.createSubsetTable(testsubset, Constants.DEFAULT_DATA_TABLE_NAME, "Test subset");
        dbc.initRandomSubset(subsetTableSize, testsubset, Constants.DEFAULT_DATA_TABLE_NAME);
        createAndSetHiddenConfig("src/test/resources/hiddenConfig.txt", postgres);
        return testsubset;
    }

    /**
     * Creates a database credentials configuration file for test purposes to the provided path.
     * @param path The path where the credentials file should be stored.
     * @param postgres The testcontainers.org postgres container from which the credentials are retrieved.
     */
    public static void createAndSetHiddenConfig(String path, PostgreSQLContainer postgres) {
        String hiddenConfigPath = path;
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
    }

    public static DataBaseConnector getDataBaseConnector(PostgreSQLContainer postgres) {
        return new DataBaseConnector(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }
}
