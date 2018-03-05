import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.validate.SqlConformance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CalciteSqlParserTest {

    private static Log logger = LogFactory.getLog(CalciteSqlParserTest.class);

    public static void main(String[] args) throws SqlParseException {

        String[] sqls = new String[] {
                "SELECT TOP number a FROM table_name",
//                "select  a from 1t",    //ERROR
//                "select f(x) from b",
//                "select rank() over(partition by a order by b,c) as x from t fetch first 10 rows only ",
//                "select rank() over(partition by a order by b,c) as x from t fetch first 10 rows only ",
//                "select a from t limit 1",
//                "select a from t limit 1,10", //ERROR
//                "create table qtemp/t_a as (select * from t) with data",//ERROR
//                "CREATE TABLE new_tbl LIKE orig_tbl", //ERROR
//                "DROP table a",   //ERROR
//                    "DROP table a if exists",   //ERROR
//                "DROP TEMPORARY table a",   //ERROR
//                "create primary table a", //ERROR
//                "insert into a select * from t",

//                "DELETE FROM Customers\n" +
//                        "WHERE CustomerName='Alfreds Futterkiste'",

//                "DELETE * FROM Customers\n" +
//                        "WHERE CustomerName='Alfreds Futterkiste'", //ERROR

//                "SELECT t.a,d,max(b) as max_b,sum(c) as sum_c\n" +
//                        "FROM t\n" +
//                        "WHERE t.a IS NOT NULL\n" +
//                        "  and b like '%'\n" +
//                        "  and (c > 1 or c < 1)\n" +
//                        "  and not ( c >= 1 and c <= 1)\n" +
//                        "  and c <> 1\n" +
//                        "  and c =1\n" +
//                        "  and c in (1,2,3)\n" +
//                        "  and c between 1 and 3\n" +
//                        "group by t.a,d\n" +
//                        "having max(b)>100 and t.a like 'a%'\n" +
//                        "order by max(t.b) desc, sum(c) asc",

//                "SELECT t.a,d,max(b) as max_b,sum(c) as sum_c\n" +
//                        "FROM t\n" +
//                        "WHERE t.a IS NOT NULL\n" +
//                        "  and b like '%'\n" +
//                        "  and (c > 1 or c < 1)\n" +
//                        "  and not ( c >= 1 and c <= 1)\n" +
//                        "  and c != 1\n" +
//                        "  and c =1\n" +
//                        "  and c in (1,2,3)\n" +
//                        "  and c between 1 and 3\n" +
//                        "group by t.a,d\n" +
//                        "having max(b)>100 and t.a like 'a%'\n" +
//                        "order by max(t.b) desc, sum(c) asc"    //ERROR

                //http://calcite.apache.org/docs/stream.html#tumbling-windows-improved
//                "SELECT STREAM\n" +
//                        "  HOP_END(rowtime, INTERVAL '1' HOUR, INTERVAL '3' HOUR) AS rowtime,\n" +
//                        "  COUNT(*) AS c,\n" +
//                        "  SUM(units) AS units\n" +
//                        "FROM Orders\n" +
//                        "GROUP BY HOP(rowtime, INTERVAL '1' HOUR, INTERVAL '3' HOUR)",


//                "SELECT STREAM TUMBLE_END(rowtime, INTERVAL '1' HOUR) AS rowtime,\n" +
//                        "  productId,\n" +
//                        "  COUNT(*) AS c,\n" +
//                        "  SUM(units) AS units\n" +
//                        "FROM Orders\n" +
//                        "GROUP BY TUMBLE(rowtime, INTERVAL '1' HOUR), productId",

//                "SELECT STREAM HOP_END(rowtime),\n" +
//                        "  productId,\n" +
//                        "  SUM(unitPrice * EXP((rowtime - HOP_START(rowtime)) SECOND / INTERVAL '1' HOUR))\n" +
//                        "   / SUM(EXP((rowtime - HOP_START(rowtime)) SECOND / INTERVAL '1' HOUR))\n" +
//                        "FROM Orders\n" +
//                        "GROUP BY HOP(rowtime, INTERVAL '1' SECOND, INTERVAL '1' HOUR),\n" +
//                        "  productId",

//                "SELECT STREAM rowtime, productId\n" +
//                        "FROM (\n" +
//                        "  SELECT TUMBLE_END(rowtime, INTERVAL '1' HOUR) AS rowtime,\n" +
//                        "    productId,\n" +
//                        "    COUNT(*) AS c,\n" +
//                        "    SUM(units) AS su\n" +
//                        "  FROM Orders\n" +
//                        "  GROUP BY TUMBLE(rowtime, INTERVAL '1' HOUR), productId)\n" +
//                        "WHERE c > 2 OR su > 10",

//                "SELECT STREAM *\n" +
//                        "FROM (\n" +
//                        "  SELECT STREAM rowtime,\n" +
//                        "    productId,\n" +
//                        "    units,\n" +
//                        "    AVG(units) OVER product (RANGE INTERVAL '10' MINUTE PRECEDING) AS m10,\n" +
//                        "    AVG(units) OVER product (RANGE INTERVAL '7' DAY PRECEDING) AS d7\n" +
//                        "  FROM Orders\n" +
//                        "  WINDOW product AS (\n" +
//                        "    ORDER BY rowtime\n" +
//                        "    PARTITION BY productId))\n" +
//                        "WHERE m10 > d7",  //ERROR

//                "SELECT STREAM rowtime,\n" +
//                        "  productId,\n" +
//                        "  units,\n" +
//                        "  SUM(units) OVER (PARTITION BY FLOOR(rowtime TO HOUR)) AS unitsSinceTopOfHour\n" +
//                        "FROM Orders",



        };


        SqlParser.Config config =  SqlParser.Config.DEFAULT;

        config = SqlParser.configBuilder().setUnquotedCasing(Casing.UNCHANGED)
                .setConformance(SqlConformance.STRICT_2003).build();



        for (String sql : sqls) {
            logger.info(String.format("\n%s\n", sql));
            SqlParser sqlParser = SqlParser.create(sql, config);
            SqlNode query = sqlParser.parseStmt();

            logger.info(String.format("\n%s\n------------------------", query.toString()));
        }
    }
}
