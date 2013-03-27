Home Page
---------

[Fluent-dbunit](http://ssledz.github.com/fluent-dbunit)

Dependencies
----------

    <repositories>
        <repository>
            <id>mvn.repo</id>
            <url>https://github.com/ssledz/mvn-repo/raw/master</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>pl.softech</groupId>
            <artifactId>fluent-dbunit</artifactId>
            <version>1.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

Load
----

    DataSource ds = ...

    DBLoader.forDB(ds).cleanInsert(dataSet(table("author", //
                cols("id", "name", "lastname"), //
                row("1", "Orson", "Card"), //
                row("2", "Terry", "Pratchett"), //
                row("3", "Magdalena", "Kozak"), //
                row("4", "Miroslav", "Žamboch"), //
                row("5", "Terry", "Goodkind") //
                )));

Assert
------
      
    DBLoader.forDB(ds).assertQueryTable(table("author", //
                cols("name", "lastname"), //
                row("Orson Scott", "Card"), //
                row("Terry", "Pratchett")), //
                "SELECT name, lastname FROM author", "name");

Print
-----

    DBUtils.dataSet2String(DBExporter.forDB(ds).table("author").getDataSet())

Export
------

    File dir = ...

    DBExporter.MyDataSet dataSet = DBExporter.forDB(ds).table("author", "book");
    dataSet.write2Csv(new File(dir, "db-csv").getAbsolutePath());
    dataSet.write2Xml(new File(dir, "db.xml").getAbsolutePath());

Tranform
--------

    ITable authorTable = ...

    Collection<Author> authors = DBUtils.table2Entity(authorTable, new DBUtils.IEntityFactory<Author>() {
            public Collection<Author> create(DBUtils.ITableResultSet rset) {
                Collection<Author> authors = new LinkedList<Author>();
                while (rset.next()) {
                    authors.add(new Author(rset.getInteger("id"), rset.getString("name"), rset.getString("lastname")));
                }
                return authors;
            }
    });