package com.talkingsmoke;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.talkingsmoke.service.ONNXInferenceService;

public class ONNXInferenceServiceUnitTest {

    private final ONNXInferenceService svc = new ONNXInferenceService();

    private double[] invokeNormalize(Object input) throws Exception {
        Method m = ONNXInferenceService.class.getDeclaredMethod("normalizeOutputToDoubleArray", Object.class);
        m.setAccessible(true);
        Object res = m.invoke(svc, input);
        return (double[]) res;
    }

    @Test
    public void normalizeFloatArray() throws Exception {
        float[] in = new float[] {1.0f, 2.0f, 3.5f};
        double[] out = invokeNormalize(in);
        assertThat(out).containsExactly(1.0d, 2.0d, 3.5d);
    }

    @Test
    public void normalizeFloat2DArrayTakesFirstRow() throws Exception {
        float[][] in = new float[][] { {0.5f, 0.25f}, {9.0f, 9.1f} };
        double[] out = invokeNormalize(in);
        assertThat(out).containsExactly(0.5d, 0.25d);
    }

    @Test
    public void normalizeDoubleArrayPassthrough() throws Exception {
        double[] in = new double[] {4.0d, 5.5d};
        double[] out = invokeNormalize(in);
        assertThat(out).isSameAs(in);
    }

    @Test
    public void normalizeNullReturnsNull() throws Exception {
        double[] out = invokeNormalize(null);
        assertThat(out).isNull();
    }

    @Test
    public void parsesBracketTuple() {
        String desc = "TensorInfo(name=... shape=[1, 384], type=float)";
        Integer v = svc.parseExpectedInnerDim(desc);
        assertThat(v).isEqualTo(384);
    }

    @Test
    public void parsesParenTuple() {
        String desc = "some info (1,512) other";
        Integer v = svc.parseExpectedInnerDim(desc);
        assertThat(v).isEqualTo(512);
    }

    @Test
    public void parsesDimKey() {
        String desc = "node.shape dim: 1024 more";
        Integer v = svc.parseExpectedInnerDim(desc);
        assertThat(v).isEqualTo(1024);
    }

    @Test
    public void returnsNullWhenNoMatch() {
        String desc = "no numeric tuple here";
        Integer v = svc.parseExpectedInnerDim(desc);
        assertThat(v).isNull();
    }
}
