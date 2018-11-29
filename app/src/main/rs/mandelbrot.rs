/*
 * Copyright (C) 2012 The Android Open Source Project
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

#pragma version(1)
#pragma rs java_package_name(ch.mandelbrot.rs)

float x0;
float y0;
float rx;
float ry;
int maxIteration;

uchar *color;

void root(const uchar4 *in, uchar4 *out, uint32_t x, uint32_t y) {
    float3 pixel;

    float kmapped=0;

    float a0 = x0 + x * rx;
    float b0 = y0 + y * ry;
    float a = 0.0;
    float b = 0.0;
    int iteration = 0;

    while (iteration < maxIteration) {
       float atemp = a * a - b * b + a0;
       float btemp = 2 * a * b + b0;
       if (a == atemp && b == btemp) {
           iteration = maxIteration;
           break;
       }
       a = atemp;
       b = btemp;
       iteration = iteration + 1;
       if (fabs(a + b) > 16) {
            break;
       }
    }

    //kmapped = ((float)iteration/(float)maxIteration) * 255;

    //pixel.z = kmapped / 50.f;
    //pixel.y = kmapped / 30.f;
    //pixel.x = kmapped / 10.f;

    //out->a = 1.f;
    //out->b = color[iteration*3+0];
    //out->g = color[iteration*3+1];
    //out->r = color[iteration*3+2];

    //rsDebug("color : ", color[iteration*3+0]);

    *out = rsPackColorTo8888(color[(iteration*3+2)%60]/255.f, color[(iteration*3+1)%60]/255.f, color[(iteration*3+0)%60]/255.f);
}

