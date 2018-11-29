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
#pragma rs java_package_name(ch.julia.rs)

float cx;
float cy;
float width;
float height;
int precision;

void root(const uchar4 *in, uchar4 *out, uint32_t x, uint32_t y) {
    float3 pixel;

    float fx=(float)((x/width)*2.f-1.f);
    float fy=(float)((y/height)*2.f-1.f);

    float t=0;
    int k=0;
    float kmapped=0;

     while(k<precision)
	 {
	    t = fx*fx-fy*fy+cx;
	    fy = 2*fx*fy+cy;
	    fx = t;
	    if (fx*fx+fy*fy >= 4) break;
	    k++;
	 }

    kmapped = ((float)k/(float)precision) * 255;

    pixel.z = kmapped / 50.f;
    pixel.y = kmapped / 30.f;
    pixel.x = kmapped / 10.f;

    *out = rsPackColorTo8888(pixel.x, pixel.y, pixel.z);
}