package net.mikaboshi;


import net.mikaboshi.csv.CSVFileUtilsTest;
import net.mikaboshi.csv.CSVIteratorTest;
import net.mikaboshi.csv.StandardCSVStrategyIteratorTest;
import net.mikaboshi.csv.StandardCSVStrategyTest;
import net.mikaboshi.csv.TSVTest;
import net.mikaboshi.io.FileIterableTest;
import net.mikaboshi.io.TeePrintWriterTest;
import net.mikaboshi.jdbc.ArrayToDbImporterTest;
import net.mikaboshi.jdbc.DmlExecutorTest;
import net.mikaboshi.jdbc.DmlExecutorTest2;
import net.mikaboshi.jdbc.InsertBuilderTest;
import net.mikaboshi.jdbc.QueryExecutorTest;
import net.mikaboshi.jdbc.ResultSetToCSVHandlerTest;
import net.mikaboshi.jdbc.ResultSetToMapListHandlerTest;
import net.mikaboshi.jdbc.SQLFormatterTest;
import net.mikaboshi.jdbc.count.CountResultSetHandlerTest;
import net.mikaboshi.jdbc.count.RecordCountUtilsTest;
import net.mikaboshi.jdbc.schema.MetadataWriterTest;
import net.mikaboshi.jdbc.schema.PrimaryKeyInfoTest;
import net.mikaboshi.jdbc.schema.SchemaUtilsTest;
import net.mikaboshi.log.SimpleFileLoggerTest;
import net.mikaboshi.property.PropertyFileLoaderTest;
import net.mikaboshi.property.PropertyFileStorerTest;
import net.mikaboshi.property.PropertyUtilsTest;
import net.mikaboshi.util.CodePointUtilsTest;
import net.mikaboshi.util.JsonFormatterTest;
import net.mikaboshi.util.ObjectDescriberTest;
import net.mikaboshi.util.MkArrayUtilsTest;
import net.mikaboshi.util.MkCollectionUtilsTest;
import net.mikaboshi.util.MkStringUtilsTest;
import net.mikaboshi.util.MultiCounterTest;
import net.mikaboshi.util.ResourceBundleWrapperTest;
import net.mikaboshi.util.SortedListTest;
import net.mikaboshi.validator.SimpleValidatorTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
	// csv
	StandardCSVStrategyTest.class,
	StandardCSVStrategyIteratorTest.class,
	CSVFileUtilsTest.class,
	TSVTest.class,
	CSVIteratorTest.class,
	
	// io
	FileIterableTest.class,
	TeePrintWriterTest.class,
	
	// util
	MkStringUtilsTest.class,
	MultiCounterTest.class,
	ResourceBundleWrapperTest.class,
	MkArrayUtilsTest.class,
	MkCollectionUtilsTest.class,
	ObjectDescriberTest.class,
	CodePointUtilsTest.class,
	JsonFormatterTest.class,
	SortedListTest.class,
	
	// log
	SimpleFileLoggerTest.class,
	
	// property
	PropertyUtilsTest.class,
	PropertyFileLoaderTest.class,
	PropertyFileStorerTest.class,
	
	// jdbc
	ArrayToDbImporterTest.class,
	DmlExecutorTest.class,
	DmlExecutorTest2.class,
	InsertBuilderTest.class,
	QueryExecutorTest.class,
	ResultSetToCSVHandlerTest.class,
	ResultSetToMapListHandlerTest.class,
	SQLFormatterTest.class,
	
	// jdbc.count
	RecordCountUtilsTest.class,
	CountResultSetHandlerTest.class,
	
	// jdbc.schema
	MetadataWriterTest.class,
	PrimaryKeyInfoTest.class,
	SchemaUtilsTest.class,
	
	// validator
	SimpleValidatorTest.class
})
public class AllTests {
}
