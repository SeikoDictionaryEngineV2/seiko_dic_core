package io.github.seikodictionaryenginev2.base.dic.v2;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.github.seikodictionaryenginev2.base.util.DictionaryUtil;
import io.github.seikodictionaryenginev2.base.util.calc.ComputeText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author kagg886
 * @date 2023/8/28 21:14
 * 针对报错的表达式，需要扔进这里进行反复测试！
 **/
public class ComputeTest {
    @Test
    void testJSON() {
        ComputeText t = new ComputeText("""
                {
                    "name": "jack",
                    "age": 1,
                    "list": [
                        1,
                        2,
                        3,
                        4
                    ]
                }
                """);
        Object a = t.eval(null);
        assertEquals(JSONObject.class, a.getClass());
    }

    @Test
    void testRef() {
        ComputeText t = new ComputeText("${a.b}");

        Object a = t.eval(new JSONObject() {{
            put("a", new JSONObject() {{
                put("b", 1);
            }});
        }});
        assertEquals(1, a);
    }

    @Test
    void testMath() {
        ComputeText t = new ComputeText("$[1+2]");

        Object a = t.eval(null);
        assertEquals("3", a);
    }

    @Test
    void testFMT_A() {
        ComputeText t = new ComputeText("abc${a.b$[1+2]}");

        Object a = t.eval(new JSONObject() {{
            put("a", new JSONObject() {{
                put("b3", "d");
            }});
        }});
        assertEquals("abcd", a);
    }

    @Test
    void testFMT_B() {
        ComputeText t = new ComputeText("abc$[1+${a.b}]");

        Object a = t.eval(new JSONObject() {{
            put("a", new JSONObject() {{
                put("b", 2);
            }});
        }});
        assertEquals("abc3", a);
    }

    @Test
    void testFMT_AContainsFMT_B() {
        ComputeText t = new ComputeText("abc${a.b$[1+${d}]}");

        Object a = t.eval(new JSONObject() {{
            put("a", new JSONObject() {{
                put("b3", "d");
            }});
            put("d", 2);
        }});
        assertEquals("abcd", a);
    }

    @Test
    void testFMT_BContainsFMT_A() {
        ComputeText t = new ComputeText("abc$[1+${a.$[1+2]}]");

        Object a = t.eval(new JSONObject() {{
            put("a", new JSONObject() {{
                put("3", 2);
            }});
        }});
        assertEquals("abc3", a);
    }

    @Test
    void testFMTA_Edge() {
        ComputeText t = new ComputeText("${a.b$[1+2]}");

        Object a = t.eval(new JSONObject() {{
            put("a", new JSONObject() {{
                put("b3", "d");
            }});
        }});
        assertEquals("d", a);
    }

    @Test
    void testFMTB_Edge() {
        ComputeText t = new ComputeText("$[1+${a.b}]");

        Object a = t.eval(new JSONObject() {{
            put("a", new JSONObject() {{
                put("b", 2);
            }});
        }});
        assertEquals("3", a);
    }

    @Test
    void testCrash() {
        try {
            ComputeText t = new ComputeText("${a.b.c");
            Object a = t.eval(new JSONObject() {{
                put("a", new JSONObject() {{
                    put("b", new JSONObject() {{
                        put("c", 1);
                    }});
                }});
            }});
            throw new RuntimeException();
        } catch (IllegalArgumentException e) {
            assertEquals("参数解析失败!原因:发现未闭合的表达式", e.getMessage());
        }
    }

    @Test
    void testNullPtr() {
        ComputeText t = new ComputeText("${a.b.c}");
        Object a = t.eval(new JSONObject() {{
            put("a", new JSONObject() {{
                put("b", new JSONObject() {{

                }});
            }});
        }});
        assertNull(a);

        try {
            t = new ComputeText("${a.b.c}");
            a = t.eval(new JSONObject() {{
                put("a", new JSONObject() {{

                }});
            }});
        } catch (NullPointerException e) {
            assertEquals("表达式计算有误：a.b的结果为null，不可以继续取属性c",e.getMessage());
        }
    }

    @Test
    void testArrayCast() {
        ComputeText t;
        Object a;
        try {
            t = new ComputeText("${a(0)}");
            a = t.eval(new JSONObject() {{
                put("a", new JSONObject() {{

                }});
            }});
        } catch (IllegalStateException e) {
            assertEquals("a的类型不是列表，无法对其运用()进行取值",e.getMessage());
        }

        try {
            t = new ComputeText("${abc${a}(0)}");
            a = t.eval(new JSONObject() {{
                put("a",1);
                put("abc1",new JSONObject(){{

                }});
            }});
        } catch (IllegalStateException e) {
            assertEquals("abc${a}的类型不是列表，无法对其运用()进行取值",e.getMessage());
        }
    }

}
