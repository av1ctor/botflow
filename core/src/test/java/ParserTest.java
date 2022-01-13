import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.HashMap;

import com.robotikflow.core.services.collections.services.CalendarScriptService;
import com.robotikflow.core.services.collections.services.DateScriptService;
import com.robotikflow.core.services.collections.services.DbScriptService;
import com.robotikflow.core.services.collections.services.MathScriptService;
import com.robotikflow.core.services.collections.services.NetScriptService;
import com.robotikflow.core.services.collections.services.UtilScriptService;
import com.robotikflow.core.services.formula.eval.AstExpression;
import com.robotikflow.core.services.formula.eval.EvalContext;
import com.robotikflow.core.services.formula.parser.Parser;
import com.robotikflow.core.services.formula.scanner.Scanner;

import org.junit.Before;
import org.junit.Test;

public class ParserTest
{
    private CalendarScriptService calendarioScriptService = new CalendarScriptService();
    private DateScriptService dateScriptService = new DateScriptService();
    private UtilScriptService utilScriptService = new UtilScriptService();
    private MathScriptService mathScriptService = new MathScriptService();
    private DbScriptService dbScriptService = new DbScriptService();
    private NetScriptService netScriptService = new NetScriptService();
    private EvalContext ctx = new EvalContext();

    @Before
    public void setup()
    {
		ctx.put("calendar", calendarioScriptService);
		ctx.put("date", dateScriptService);
		ctx.put("util", utilScriptService);
		ctx.put("math", mathScriptService);
		ctx.put("db", dbScriptService);
		ctx.put("net", netScriptService);
    }

    private AstExpression parseStr(String src) throws Exception
    {
        var scanner = new Scanner(new StringReader(src));
        var parser = new Parser(scanner);
        return (AstExpression)parser.parse().value;
    }

    @Test
    public void nullIsNull() throws Exception
    {
        var expr = parseStr("null");
        assertEquals(null, expr.eval(ctx));
    }

    @Test
    public void falseIsFalse() throws Exception
    {
        var expr = parseStr("false");
        assertEquals(false, expr.eval(ctx));
    }

    @Test
    public void trueIsTrue() throws Exception
    {
        var expr = parseStr("true");
        assertEquals(true, expr.eval(ctx));
    }

    @Test
    public void addIntegerBop() throws Exception
    {
        var expr = parseStr("1+2+3+4");
        assertEquals(1+2+3+4, expr.eval(ctx));
    }

    @Test
    public void addLongBop() throws Exception
    {
        var expr = parseStr("1L+2L+3L+4L");
        assertEquals(1L+2L+3L+4L, expr.eval(ctx));
    }

    @Test
    public void addDoubleBop() throws Exception
    {
        var expr = parseStr("1.0+2.0+3.0+4.0");
        assertEquals(1.0+2.0+3.0+4.0, expr.eval(ctx));
    }

    @Test
    public void addDoubleIntegerBop() throws Exception
    {
        var expr = parseStr("1.0+2+3.0+4");
        assertEquals(1.0+2+3.0+4, expr.eval(ctx));
    }

    @Test
    public void addLongIntegerBop() throws Exception
    {
        var expr = parseStr("1+2L+3+4L");
        assertEquals(1+2L+3+4L, expr.eval(ctx));
    }

    @Test
    public void addStringBop() throws Exception
    {
        var expr = parseStr("\"abc\" + \"def\"");
        assertEquals("abc" + "def", expr.eval(ctx));
    }

    @Test
    public void addStringSingleQuoteBop() throws Exception
    {
        var expr = parseStr("'abc' + 'def'");
        assertEquals("abc" + "def", expr.eval(ctx));
    }

    @Test
    public void subIntegerBop() throws Exception
    {
        var expr = parseStr("-1-2-3-4");
        var n = expr;
        assertEquals(-1-2-3-4, n.eval(ctx));
    }

    @Test
    public void subLongBop() throws Exception
    {
        var expr = parseStr("-1L-2L-3L-4L");
        var n = expr;
        assertEquals(-1L-2L-3L-4L, n.eval(ctx));
    }

    @Test
    public void subDoubleBop() throws Exception
    {
        var expr = parseStr("1.0-(-2.0)-3.0-4.0");
        assertEquals(1.0-(-2.0)-3.0-4.0, expr.eval(ctx));
    }

    @Test
    public void subDoubleIntegerBop() throws Exception
    {
        var expr = parseStr("1.0-2-3.0-(-4)");
        assertEquals(1.0-2-3.0-(-4), expr.eval(ctx));
    }

    @Test
    public void subLongIntegerBop() throws Exception
    {
        var expr = parseStr("1-2L-3-(-4L)");
        assertEquals(1-2L-3-(-4L), expr.eval(ctx));
    }

    @Test
    public void mulIntegerBop() throws Exception
    {
        var expr = parseStr("1*2*3*4");
        assertEquals(1*2*3*4, expr.eval(ctx));
    }

    @Test
    public void mulLongBop() throws Exception
    {
        var expr = parseStr("1L*2L*3L*4L");
        assertEquals(1L*2L*3L*4L, expr.eval(ctx));
    }

    @Test
    public void mulDoubleBop() throws Exception
    {
        var expr = parseStr("1.0*2.0*3.0*4.0");
        assertEquals(1.0*2.0*3.0*4.0, expr.eval(ctx));
    }

    @Test
    public void mulDoubleIntegerBop() throws Exception
    {
        var expr = parseStr("1.0*2*3.0*4");
        assertEquals(1.0*2*3.0*4, expr.eval(ctx));
    }

    @Test
    public void mulLongIntegerBop() throws Exception
    {
        var expr = parseStr("1L*2*3L*4");
        assertEquals(1L*2*3L*4, expr.eval(ctx));
    }

    @Test
    public void divIntegerBop() throws Exception
    {
        var expr = parseStr("100/2/3/4");
        assertEquals(100/2/3/4, expr.eval(ctx));
    }

    @Test
    public void divLongBop() throws Exception
    {
        var expr = parseStr("100L/2L/3L/4L");
        assertEquals(100L/2L/3L/4L, expr.eval(ctx));
    }

    @Test
    public void divDoubleBop() throws Exception
    {
        var expr = parseStr("100.0/2.0/3.0/4.0");
        assertEquals(100.0/2.0/3.0/4.0, expr.eval(ctx));
    }

    @Test
    public void divDoubleIntegerBop() throws Exception
    {
        var expr = parseStr("100.0/2/3.0/4");
        assertEquals(100.0/2/3.0/4, expr.eval(ctx));
    }

    @Test
    public void divLongIntegerBop() throws Exception
    {
        var expr = parseStr("100L/2/3L/4");
        assertEquals(100L/2/3L/4, expr.eval(ctx));
    }

    @Test
    public void mulAddDoubleBop() throws Exception
    {
        var expr = parseStr("100.0+10.0*2.0");
        assertEquals(100.0+10.0*2.0, expr.eval(ctx));
    }

    @Test
    public void orIntegerop() throws Exception
    {
        var expr = parseStr("1|2|3|4");
        assertEquals(1|2|3|4, expr.eval(ctx));
    }

    @Test
    public void orLongBop() throws Exception
    {
        var expr = parseStr("1L|2L|3L|4L");
        assertEquals(1L|2L|3L|4L, expr.eval(ctx));
    }

    @Test
    public void andIntegerBop() throws Exception
    {
        var expr = parseStr("1&2&3&4");
        assertEquals(1&2&3&4, expr.eval(ctx));
    }

    @Test
    public void andLongBop() throws Exception
    {
        var expr = parseStr("1L&2L&3L&4L");
        assertEquals(1L&2L&3L&4L, expr.eval(ctx));
    }

    @Test
    public void shlIntegerBop() throws Exception
    {
        var expr = parseStr("1<<2<<3<<4");
        assertEquals(1<<2<<3<<4, expr.eval(ctx));
    }

    @Test
    public void shlLongBop() throws Exception
    {
        var expr = parseStr("1L<<2L<<3L<<4L");
        assertEquals(1L<<2L<<3L<<4L, expr.eval(ctx));
    }

    @Test
    public void shrIntegerBop() throws Exception
    {
        var expr = parseStr("10000>>2>>3>>4");
        assertEquals(10000>>2>>3>>4, expr.eval(ctx));
    }

    @Test
    public void shrLongBop() throws Exception
    {
        var expr = parseStr("10000L>>2L>>3L>>4L");
        assertEquals(10000L>>2L>>3L>>4L, expr.eval(ctx));
    }

    @Test
    public void lorTrueBop() throws Exception
    {
        var expr = parseStr("1 > 2 || 2 > 1");
        assertEquals(1 > 2 || 2 > 1, expr.eval(ctx));
    }

    @Test
    public void lorFalseBop() throws Exception
    {
        var expr = parseStr("1 > 2 || 2 > 3");
        assertEquals(1 > 2 || 2 > 3, expr.eval(ctx));
    }

    @Test
    public void landTrueBop() throws Exception
    {
        var expr = parseStr("2 > 1 && 3 > 2");
        assertEquals(2 > 1 && 3 > 2, expr.eval(ctx));
    }

    @Test
    public void landFalseBop() throws Exception
    {
        var expr = parseStr("1 > 2 && 2 > 3");
        assertEquals(1 > 2 && 2 > 3, expr.eval(ctx));
    }

    @Test
    public void gtIntegerBop() throws Exception
    {
        var expr = parseStr("1 > -2");
        assertEquals(1 > -2, expr.eval(ctx));
    }

    @Test
    public void ltIntegerBop() throws Exception
    {
        var expr = parseStr("1 < 2");
        assertEquals(1 < 2, expr.eval(ctx));
    }

    @Test
    public void geIntegerBop() throws Exception
    {
        var expr = parseStr("1 >= 1");
        assertEquals(1 >= 1, expr.eval(ctx));
    }

    @Test
    public void leIntegerBop() throws Exception
    {
        var expr = parseStr("1 <= 1");
        assertEquals(1 <= 1, expr.eval(ctx));
    }

    @Test
    public void eqIntegerBop() throws Exception
    {
        var expr = parseStr("1234 = 1234");
        assertEquals(1234 == 1234, expr.eval(ctx));
        expr = parseStr("-1234 == -1234");
        assertEquals(-1234 == -1234, expr.eval(ctx));
    }

    @Test
    public void neIntegerBop() throws Exception
    {
        var expr = parseStr("1234 != 5678");
        assertEquals(1234 != 5678, expr.eval(ctx));
        expr = parseStr("-1234 <> -5678");
        assertEquals(-1234 != -5678, expr.eval(ctx));
    }

    @Test
    public void eqStringBop() throws Exception
    {
        var expr = parseStr("\"abc\" == \"abc\"");
        assertEquals("abc".equals("abc"), expr.eval(ctx));
    }

    @Test
    public void eqStringSingleQuoteBop() throws Exception
    {
        var expr = parseStr("'abc' == 'abc'");
        assertEquals("abc".equals("abc"), expr.eval(ctx));
    }

    @Test
    public void neStringBop() throws Exception
    {
        var expr = parseStr("\"abc\" <> \"abç\"");
        assertEquals(!"abc".equals("abç"), expr.eval(ctx));
    }

    @Test
    public void neStringSingleQuoteBop() throws Exception
    {
        var expr = parseStr("'abc' != 'abç'");
        assertEquals(!"abc".equals("abç"), expr.eval(ctx));
    }

    @Test
    public void gtDoubleBop() throws Exception
    {
        var expr = parseStr("1.0 > -2.0");
        assertEquals(1.0 > -2.0, expr.eval(ctx));
    }

    @Test
    public void ltDoubleBop() throws Exception
    {
        var expr = parseStr("1.0 < 2.0");
        assertEquals(1.0 < 2.0, expr.eval(ctx));
    }

    @Test
    public void geDoubleBop() throws Exception
    {
        var expr = parseStr("1.0 >= 1.0");
        assertEquals(1.0 >= 1.0, expr.eval(ctx));
    }

    @Test
    public void leDoubleBop() throws Exception
    {
        var expr = parseStr("1.0 <= 1.0");
        assertEquals(1.0 <= 1.0, expr.eval(ctx));
    }

    @Test
    public void eqDoubleBop() throws Exception
    {
        var expr = parseStr("1234.0 = 1234.0");
        assertEquals(1234.0 == 1234.0, expr.eval(ctx));
        expr = parseStr("-1234.0 == -1234.0");
        assertEquals(-1234.0 == -1234.0, expr.eval(ctx));
    }    

    @Test
    public void neDoubleBop() throws Exception
    {
        var expr = parseStr("1234.0 != 5678.0");
        assertEquals(1234.0 != 5678.0, expr.eval(ctx));
        expr = parseStr("-1234.0 <> -5678.0");
        assertEquals(-1234.0 != -5678.0, expr.eval(ctx));
    }

    @Test
    public void negIntegerUop() throws Exception
    {
        var expr = parseStr("-(-(-1))");
        assertEquals(-(-(-1)), expr.eval(ctx));
    }

    @Test
    public void negLongUop() throws Exception
    {
        var expr = parseStr("-(-(-1L))");
        assertEquals(-(-(-1L)), expr.eval(ctx));
    }

    @Test
    public void negDoubleUop() throws Exception
    {
        var expr = parseStr("-(-(-1.234))");
        assertEquals(-(-(-1.234)), expr.eval(ctx));
    }

    @Test
    public void notTrueUop() throws Exception
    {
        var expr = parseStr("!true");
        assertEquals(!true, expr.eval(ctx));
    }

    @Test
    public void notFalseUop() throws Exception
    {
        var expr = parseStr("!(1==2)");
        assertEquals(!(1==2), expr.eval(ctx));
    }

    @Test
    public void mathMaxInteger() throws Exception
    {
        var expr = parseStr("math.max(1,2)");
        assertEquals(Math.max(1, 2), expr.eval(ctx));
    }

    @Test
    public void mathMaxLong() throws Exception
    {
        var expr = parseStr("math.max(1L,2L)");
        assertEquals(Math.max(1L, 2L), expr.eval(ctx));
    }

    @Test
    public void mathMaxDouble() throws Exception
    {
        var expr = parseStr("math.max(2.0,1.0)");
        assertEquals(Math.max(2.0, 1.0), expr.eval(ctx));
    }

    @Test
    public void mathMaxMixed() throws Exception
    {
        var expr = parseStr("math.max(2,1.0)");
        assertEquals(Math.max(2, 1.0), expr.eval(ctx));

        expr = parseStr("math.max(2.0,1)");
        assertEquals(Math.max(2.0, 1), expr.eval(ctx));        
    }

    @Test
    public void mathMinInteger() throws Exception
    {
        var expr = parseStr("math.min(1,2)");
        assertEquals(Math.min(1, 2), expr.eval(ctx));
    }

    @Test
    public void mathMinLong() throws Exception
    {
        var expr = parseStr("math.min(1L,2L)");
        assertEquals(Math.min(1L, 2L), expr.eval(ctx));
    }

    @Test
    public void mathMinDouble() throws Exception
    {
        var expr = parseStr("math.min(2.0,1.0)");
        assertEquals(Math.min(2.0, 1.0), expr.eval(ctx));
    }

    @Test
    public void mathMinMixed() throws Exception
    {
        var expr = parseStr("math.min(2,1.0)");
        assertEquals(Math.min(2, 1.0), expr.eval(ctx));

        expr = parseStr("math.min(2.0,1)");
        assertEquals(Math.min(2.0, 1), expr.eval(ctx));        
    }    

    @Test
    public void calendarNow() throws Exception
    {
        var expr = parseStr("calendar.now().toISOString()");
        assertEquals(calendarioScriptService.now().toISOString().substring(0, 9), ((String)expr.eval(ctx)).substring(0, 9));
    }    

    @Test
    public void calendarFromYear() throws Exception
    {
        var expr = parseStr("calendar.from(2020).toISOString()");
        assertEquals(calendarioScriptService.from(2020).toISOString().substring(0, 9), ((String)expr.eval(ctx)).substring(0, 9));
    }    

    @Test
    public void calendarFromYearMonth() throws Exception
    {
        var expr = parseStr("calendar.from(2020, 10).toISOString()");
        assertEquals(calendarioScriptService.from(2020, 10).toISOString().substring(0, 9), ((String)expr.eval(ctx)).substring(0, 9));
    }    

    @Test
    public void calendarFromYearMonthDay() throws Exception
    {
        var expr = parseStr("calendar.from(2020, 10, 25).toISOString()");
        assertEquals(calendarioScriptService.from(2020, 10, 25).toISOString().substring(0, 9), ((String)expr.eval(ctx)).substring(0, 9));
    }    

    @Test
    public void dateIsDueTrue() throws Exception
    {
        var expr = parseStr("calendar.now().plusDays(1).isDue()");
        assertEquals(calendarioScriptService.now().plusDays(1).isDue(), expr.eval(ctx));
    }    

    @Test
    public void dateIsDueFalse() throws Exception
    {
        var expr = parseStr("calendar.now().plusDays(-1).isDue()");
        assertEquals(calendarioScriptService.now().plusDays(-1).isDue(), expr.eval(ctx));
    }    

    @Test
    public void dateIsBefore() throws Exception
    {
        var ctx2 = new EvalContext(ctx);
        var auth = new HashMap<String, Object>();
        auth.put("tokenExpiration", ZonedDateTime.now().plusDays(-1));
        ctx2.put("auth", auth);
        var expr = parseStr("date.from(auth.tokenExpiration).isBefore(calendar.now())");
        assertEquals(dateScriptService.from((ZonedDateTime)auth.get("tokenExpiration")).isBefore(calendarioScriptService.now()), expr.eval(ctx2));
    }    

    @Test
    public void dateIsAfter() throws Exception
    {
        var ctx2 = new EvalContext(ctx);
        var auth = new HashMap<String, Object>();
        auth.put("tokenExpiration", ZonedDateTime.now().plusDays(1));
        ctx2.put("auth", auth);
        var expr = parseStr("date.from(auth.tokenExpiration).isAfter(calendar.now())");
        assertEquals(dateScriptService.from((ZonedDateTime)auth.get("tokenExpiration")).isAfter(calendarioScriptService.now()), expr.eval(ctx2));
    }    
}
