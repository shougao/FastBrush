#pragma version(1)
#pragma rs java_package_name(co.adrianblan.fastbrush)
#pragma rs_fp_relaxed

// Script globals
float BRUSH_BASE_LENGTH;
float SEGMENTS_PER_BRISTLE;

float3 brushPosition;

float planarDistanceFromHandle;
float upperControlPointLength;
float lowerControlPointLength;

float cosHorizontalAngle;
float sinHorizontalAngle;

float* inBristlePositionTop;
float* inBristlePositionBottom;
float* outBristlePosition;

rs_script script;

void init() {
}

void root(uchar4 *in, uint32_t x) {

    int outIndex = x * 2 * 3 * SEGMENTS_PER_BRISTLE;

    float3 bristlePositionTop;
    bristlePositionTop.x = inBristlePositionTop[x * 3];
    bristlePositionTop.y = inBristlePositionTop[x * 3 + 1];
    bristlePositionTop.z = inBristlePositionTop[x * 3 + 2];
    bristlePositionTop += brushPosition;

    float3 bristlePositionBottom;
    bristlePositionBottom.x = inBristlePositionBottom[x * 3];
    bristlePositionBottom.y = inBristlePositionBottom[x * 3 + 1];
    bristlePositionBottom.z = inBristlePositionBottom[x * 3 + 2];
    bristlePositionBottom += brushPosition;

    float bottom = bristlePositionBottom.z;

    if(bottom < 0) {
        bottom = 0;
    }

    float3 interpolatedPosition = bristlePositionTop;
    float scale;
    float firstFactor;
    float secondFactor;
    float thirdFactor;
    float fourthFactor;

    float length = distance(bristlePositionTop, bristlePositionBottom);

    for(int i = 1; i <= SEGMENTS_PER_BRISTLE; i++) {

        outBristlePosition[outIndex] = interpolatedPosition.x;
        outBristlePosition[outIndex + 1] = interpolatedPosition.y;
        outBristlePosition[outIndex + 2] = interpolatedPosition.z;
        outIndex += 3;

        scale = ((float) i / SEGMENTS_PER_BRISTLE) * (length / BRUSH_BASE_LENGTH);
        firstFactor = (1 - scale) * (1 - scale) * (1 - scale);
        secondFactor = 3 * (1 - scale) * (1 - scale) * scale;
        thirdFactor = 3 * (1 - scale) * scale * scale;
        fourthFactor = scale * scale * scale;

        interpolatedPosition.x =
                firstFactor
                        * bristlePositionTop.x
                + secondFactor
                        * (bristlePositionTop.x - (bristlePositionTop.x - bristlePositionBottom.x)
                        * upperControlPointLength)
                + thirdFactor
                        * (bristlePositionBottom.x
                        + cosHorizontalAngle * planarDistanceFromHandle
                        - cosHorizontalAngle * lowerControlPointLength)
                + fourthFactor
                        * (bristlePositionBottom.x
                        + cosHorizontalAngle * planarDistanceFromHandle);

        interpolatedPosition.y =
                firstFactor
                    * bristlePositionTop.y
                + secondFactor
                    * (bristlePositionTop.y - (bristlePositionTop.y - bristlePositionBottom.y)
                    * upperControlPointLength)
                + thirdFactor
                    * (bristlePositionBottom.y
                    + sinHorizontalAngle * planarDistanceFromHandle
                    - sinHorizontalAngle * lowerControlPointLength)
                + fourthFactor
                    * (bristlePositionBottom.y
                    + sinHorizontalAngle * planarDistanceFromHandle);

        interpolatedPosition.z =
                firstFactor
                    * bristlePositionTop.z
                + secondFactor
                    * (bristlePositionTop.z - (bristlePositionTop.z - bottom)
                    * upperControlPointLength)
                + thirdFactor
                    * bottom
                + fourthFactor
                    * bottom;

        outBristlePosition[outIndex] = interpolatedPosition.x;
        outBristlePosition[outIndex + 1] = interpolatedPosition.y;
        outBristlePosition[outIndex + 2] = interpolatedPosition.z;
        outIndex += 3;
    }
}

void compute (rs_allocation in) {
    rs_allocation outIgnored;
    rsForEach(script, in, outIgnored);
}