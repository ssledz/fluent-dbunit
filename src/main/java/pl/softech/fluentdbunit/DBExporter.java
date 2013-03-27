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
import java.io.FileOutputStream;
import javax.sql.DataSource;
import org.dbunit.AbstractDatabaseTester;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.dataset.xml.FlatXmlDataSet;

/**
 *
 * @author Sławomir Śledź <slawomir.sledz@sof-tech.pl>
 * @since 1.0
 */
public class DBExporter {

    public static class Query {

        private final String table;
        private final String sql;

        private Query(String table, String sql) {
            this.table = table;
            this.sql = sql;
        }

        public static Query query(String table, String sql) {
            return new Query(table, sql);
        }

        public static Query[] queries(Query... query) {
            return query;
        }
    }

    public class MyDataSet {

        private final IDataSet dataSet;

        private MyDataSet(IDataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void write2Xml(String fileName) throws Exception {
            FlatXmlDataSet.write(dataSet, new FileOutputStream(new File(fileName)));
        }

        public void write2Csv(String dirName) throws Exception {
            CsvDataSetWriter.write(dataSet, new File(dirName));
        }

        public IDataSet getDataSet() {
            return dataSet;
        }
    }
    private final AbstractDatabaseTester databaseTester;

    private DBExporter(AbstractDatabaseTester databaseTester) {
        this.databaseTester = databaseTester;
    }

    public static DBExporter forDB(DataSource dataSource) {
        return new DBExporter(new DataSourceDatabaseTester(dataSource));
    }

    public static DBExporter forDB(String driverClassName, String url, String username, String password)
            throws ClassNotFoundException {
        return new DBExporter(new JdbcDatabaseTester(driverClassName, url, username, password));
    }

    public MyDataSet table(String... names) throws Exception {
        return table(new Query[0], names);
    }

    public MyDataSet table(Query... queries) throws Exception {
        return table(Query.queries(queries), new String[0]);
    }

    public MyDataSet table(Query[] queries, String... names) throws Exception {
        QueryDataSet partialDataSet = new QueryDataSet(databaseTester.getConnection());

        for (Query q : queries) {
            partialDataSet.addTable(q.table, q.sql);
        }

        for (String name : names) {
            partialDataSet.addTable(name);
        }
        return new MyDataSet(partialDataSet);
    }

    public MyDataSet all() throws Exception {
        return new MyDataSet(databaseTester.getConnection().createDataSet());
    }
}
