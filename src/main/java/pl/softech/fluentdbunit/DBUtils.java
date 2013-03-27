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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;

/**
 *
 * @author Sławomir Śledź <slawomir.sledz@sof-tech.pl>
 * @since 1.0
 */
public class DBUtils {

    public interface ITableResultSet {

        String getString(String columnName);

        Integer getInteger(String columnName);

        boolean next();
    }

    public interface IEntityFactory<T> {

        Collection<T> create(ITableResultSet rset);
    }

    private static class TableResultSetImpl implements ITableResultSet {

        private final Iterator<Map<String, Object>> it;
        private Map<String, Object> current;

        private TableResultSetImpl(Iterator<Map<String, Object>> it) {
            this.it = it;
        }

        @Override
        public boolean next() {

            if (it.hasNext()) {
                current = it.next();
                return true;
            }

            return false;
        }

        @Override
        public String getString(String columnName) {
            return current.get(columnName.toLowerCase()).toString();
        }

        @Override
        public Integer getInteger(String columnName) {

            Object value = current.get(columnName.toLowerCase());

            if (value instanceof Integer) {
                return (Integer) value;
            }

            return Integer.parseInt(value.toString());
        }
    };

    public static String dataSet2String(IDataSet ds) throws Exception {
        return dataSet2String(ds, "\n", 25);
    }

    public static String dataSet2String(IDataSet ds, int minColumnWidth) throws Exception {
        return dataSet2String(ds, "\n", minColumnWidth);
    }

    public static String dataSet2String(ITable t) throws Exception {
        return dataSet2String(t, "\n", 25);
    }

    public static String dataSet2String(ITable t, String rowDelimiter, int minColumnWidth) throws Exception {
        StringBuilder builder = new StringBuilder();

        int colls = t.getTableMetaData().getColumns().length;

        builder.append("Table: ").append(t.getTableMetaData().getTableName());
        builder.append(rowDelimiter);
        builder.append(rowDelimiter);

        int maxLen = minColumnWidth;
        String[] columns = new String[colls];
        for (int j = 0; j < colls; j++) {
            columns[j] = t.getTableMetaData().getColumns()[j].getColumnName();
            if (columns[j] != null && columns[j].toString().length() > maxLen) {
                maxLen = columns[j].toString().length();
            }
        }

        String format = "|";
        for (int j = 1; j <= colls; j++) {
            format += "%" + j + "$-" + maxLen + "s|";
        }
        format += rowDelimiter;

        builder.append(String.format(format, columns));

        for (int i = 0; i < t.getRowCount(); i++) {

            String[] args = new String[colls];
            for (int j = 0; j < colls; j++) {
                args[j] = t.getValue(i, columns[j]).toString();
                if (args[j] != null && args[j].toString().length() > maxLen) {
                    args[j] = args[j].toString().subSequence(0, maxLen - 5) + "[...]";
                }
            }
            builder.append(String.format(format, args));
        }

        return builder.toString();
    }

    public static String dataSet2String(IDataSet ds, String rowDelimiter, int minColumnWidth) throws Exception {

        StringBuilder builder = new StringBuilder();

        ITableIterator it = ds.iterator();

        while (it.next()) {
            ITable t = it.getTable();
            builder.append(dataSet2String(t, rowDelimiter, minColumnWidth));
            builder.append(rowDelimiter);
        }

        return builder.toString();
    }

    public static <T> Collection<T> table2Entity(ITable table, IEntityFactory<T> ef) throws Exception {

        Collection<Map<String, Object>> rows = new LinkedList<Map<String, Object>>();

        Column[] columns = table.getTableMetaData().getColumns();

        for (int i = 0; i < table.getRowCount(); i++) {

            Map<String, Object> columnName2Value = new HashMap<String, Object>();

            for (int j = 0; j < columns.length; j++) {
                String columnName = columns[j].getColumnName();
                columnName2Value.put(columnName.toLowerCase(), table.getValue(i, columnName));
            }
            rows.add(columnName2Value);
        }

        ITableResultSet rset = new TableResultSetImpl(rows.iterator());

        return ef.create(rset);

    }
}
