package io.github.seikodictionaryenginev2.base.dic.v2;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.github.seikodictionaryenginev2.base.util.DictionaryUtil;
import io.github.seikodictionaryenginev2.base.util.calc.ComputeText;
import org.junit.jupiter.api.Test;

/**
 * @author kagg886
 * @date 2023/8/28 21:14
 **/
public class ComputeTest {
    //abc[1+2] -> (abc%s, [1+2])

    //[1+{a}] -> ([1+%s],{a})
    //abc[1+{a}] -> (abc%s, ([1+%s],{a}))

    //abc{a[1+{a(3)}]} -> (abc%s, ({a%s}, ([1+%s], {a(3)})))

    ///abc[1+2]def{a([0])} ---> (abc%sdef%s, [1+2], (a(%s), [0]))
    @Test
    void computeMath() {
        JSONObject object = JSON.parseObject("{\"a\":{\"b\":\"3\"},\"c\":[1,2,3,4,5]}");
        ComputeText text = new ComputeText(object.toString());
        System.out.println(text);
    }

    @Test
    void computeBoolean() {
        JSONObject object = JSON.parseObject("{\"a\":{\"b\":\"3\"},\"c\":[1,2,3,4,5]}");
        System.out.println(DictionaryUtil.evalBooleanExpression("{c(2)} == [{c(4)} - 2]",object));
    }

}
