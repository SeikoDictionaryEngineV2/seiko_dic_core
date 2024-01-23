package io.github.seikodictionaryenginev2.base.dic.v2;

import io.github.seikodictionaryenginev2.base.util.DictionaryUtil;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author kagg886
 * @Date 2024/1/23 上午10:50
 * @description:
 */

public class BooleanParserTest {
    @Test
    void testBasicBoolean() {
        test("true",true);
        test("false",false);

        test("a==a",true);
        test("a!=a",false);

        test("3>2",true);
        test("3<2",false);

        test("3>=2",true);
        test("3<=2",false);

        test("true && true",true);
        test("true && false",false);

        test("true || true",true);
        test("true || false",true);

        test("4==3&&1==1||2==2",true);
        test("4==3&&(1==1&&2==2)",false);

        test("${a}==2",new HashMap<>() {{
            put("a",1);
        }},false);
    }

    void test(String express, boolean expert) {
        assertEquals(expert,DictionaryUtil.evalBooleanExpression(express,null));
    }
    void test(String express, Map<String,Object> runnable, boolean expert) {
        assertEquals(expert,DictionaryUtil.evalBooleanExpression(express,runnable));
    }
}
