/*
 * Copyright 2013 Sławomir Śledź <slawomir.sledz@sof-tech.pl>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.softech.fluentdbunit;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.sql.DataSource;
import org.dbunit.AbstractDatabaseTester;
import org.dbunit.Assertion;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;

/**
 *
 * @author Sławomir Śledź <slawomir.sledz@sof-tech.pl>
 * @since 1.0
 */
public class DBLoader {

	/**
	 * DBUnit date format.
	 */
	private static final SimpleDateFormat DB_UNIT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * DBUnit time format.
	 */
	private static final SimpleDateFormat DB_UNIT_TIME_FORMAT = new SimpleDateFormat("hh:mm:ss");

	/**
	 * DBUnit timestamp format.
	 */
	private static final SimpleDateFormat DB_UNIT_TIMESTAMP_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss.SSSSSSSSS");

	/**
	 * Simple tester.
	 */
	private final AbstractDatabaseTester databaseTester;

	private IDatabaseConnection conn;

	/**
	 * Create loader.
	 * 
	 * @param databaseTester
	 */
	private DBLoader(AbstractDatabaseTester databaseTester) {
		this.databaseTester = databaseTester;
	}

	/**
	 * Deete data than insert.
	 * 
	 * @param dataSet
	 *            data.
	 * @throws Exception
	 */
	public DBLoader cleanInsert(IDataSet dataSet) throws Exception {
		conn = databaseTester.getConnection();
		DatabaseOperation.CLEAN_INSERT.execute(conn, dataSet);
		conn.close();
		return this;
	}

	/**
	 * Create columns names array.
	 * 
	 * @param names
	 * @return
	 */
	public static String[] cols(String... names) {
		return names;
	}

	/**
	 * Create {@link IDataSet} from {@link ITable}.
	 * 
	 * @param tables
	 * @return
	 * @throws AmbiguousTableNameException
	 */
	public static IDataSet dataSet(ITable... tables) throws AmbiguousTableNameException {
		return new DefaultDataSet(tables);
	}

	/**
	 * Create DBUnit-valid date {@link String} from {@link Date}.
	 * 
	 * @param date
	 * @return
	 */
	public static String date(Date date) {
		return DB_UNIT_DATE_FORMAT.format(date);
	}

	/**
	 * Create loader.
	 * 
	 * @param driverClassName
	 * @param url
	 * @param username
	 * @param password
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static DBLoader forDB(String driverClassName, String url, String username, String password)
			throws ClassNotFoundException {
		return new DBLoader(new JdbcDatabaseTester(driverClassName, url, username, password));
	}

	/**
	 * Create loader.
	 * 
	 * @param dataSource
	 * @return
	 */
	public static DBLoader forDB(DataSource dataSource) {
		return new DBLoader(new DataSourceDatabaseTester(dataSource));
	}

	/**
	 * Load from Csv.
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static IDataSet fromCsv(String fileName) throws Exception {
		return new CsvDataSet((new File(fileName)));
	}
	
	/**
	 * Load from flat XML.
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static IDataSet fromFlatXml(String fileName) throws Exception {
		return new FlatXmlDataSetBuilder().build(new File(fileName));
	}

	/**
	 * Load from XML with DTD.
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static IDataSet fromXls(String fileName) throws Exception {
		return new XlsDataSet(new FileInputStream(new File(fileName)));
	}

	/**
	 * Load from xls.
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static IDataSet fromXml(String fileName) throws Exception {
		return new XmlDataSet(new FileInputStream(new File(fileName)));
	}

	/**
	 * Map data to row.
	 * 
	 * @param rowData
	 * @return
	 */
	public static String[] row(String... rowData) {
		return rowData;
	}

	/**
	 * Create {@link ITable} with name, columns and data.
	 * 
	 * @param name
	 * @param cols
	 * @param rows
	 * @return
	 * @throws DataSetException
	 */
	public static ITable table(String name, String[] cols, String[]... rows) throws DataSetException {
		return addRows(defineTable(name, cols), rows);
	}

	/**
	 * Create DBUnit-valid time {@link String} from {@link Date}.
	 * 
	 * @param date
	 * @return
	 */
	public static String time(Date date) {
		return DB_UNIT_TIME_FORMAT.format(date);
	}

	/**
	 * Create DBUnit-valid timestamp {@link String} from {@link Date}.
	 * 
	 * @param date
	 * @return
	 */
	public static String timestamp(Date date) {
		return DB_UNIT_TIMESTAMP_FORMAT.format(date);
	}

	/**
	 * Add rows to tabel.
	 * 
	 * @param table
	 * @param rows
	 * @return
	 * @throws DataSetException
	 */
	private static ITable addRows(DefaultTable table, String[]... rows) throws DataSetException {
		for (String[] row : rows) {
			table.addRow(row);
		}
		return table;
	}

	/**
	 * Create table.
	 * 
	 * @param name
	 * @param cols
	 * @return
	 */
	private static DefaultTable defineTable(String name, String[] cols) {
		return new DefaultTable(name, makeColumns(cols));
	}

	/**
	 * Map columns names to {@link Column} array.
	 * 
	 * @param cols
	 * @return
	 */
	private static Column[] makeColumns(String[] cols) {
		Column[] columns = new Column[cols.length];
		for (int i = 0; i < cols.length; i++) {
			columns[i] = new Column(cols[i], DataType.UNKNOWN);
		}
		return columns;
	}

	public DBLoader assertTable(ITable expectedTable, String... sortBy) throws Exception {

		String tableName = expectedTable.getTableMetaData().getTableName();
		IDatabaseConnection conn = databaseTester.getConnection();
		ITable currentTable = conn.createDataSet().getTable(tableName);
		conn.close();
		currentTable = DefaultColumnFilter.includedColumnsTable(currentTable, expectedTable.getTableMetaData()
				.getColumns());

		if (sortBy.length > 0) {
			currentTable = sortedTable(currentTable, sortBy);
			expectedTable = sortedTable(expectedTable, sortBy);
		}

		Assertion.assertEquals(expectedTable, currentTable);

		return this;

	}

	private static SortedTable sortedTable(ITable table, String... columns) throws DataSetException {

		SortedTable st = new SortedTable(table, columns);
		st.setUseComparable(true);

		return st;
	}

	public DBLoader assertQueryTable(ITable expectedTable, String query, String... sortBy) throws Exception {

		String tableName = expectedTable.getTableMetaData().getTableName();

		IDatabaseConnection conn = databaseTester.getConnection();
		ITable currentTable = conn.createQueryTable(tableName, query);
		conn.close();
		currentTable = DefaultColumnFilter.includedColumnsTable(currentTable, expectedTable.getTableMetaData()
				.getColumns());

		if (sortBy.length > 0) {
			currentTable = sortedTable(currentTable, sortBy);
			expectedTable = sortedTable(expectedTable, sortBy);
		}

		Assertion.assertEquals(expectedTable, currentTable);

		return this;
	}
	
}
