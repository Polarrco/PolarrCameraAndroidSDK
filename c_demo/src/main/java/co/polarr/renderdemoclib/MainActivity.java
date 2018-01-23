/*
 * Copyright 2013 The Android Open Source Project
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

package co.polarr.renderdemoclib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import co.polarr.render.PolarrRenderJni;

public class MainActivity extends Activity {
    private ImageView imageView;

    private int w = 0;
    private int h = 0;
    private int stride = 0;
    private int scanline = 0;
    private byte[] yuvData;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_fullscreen);

        imageView = (ImageView) findViewById(R.id.imageView);

        new Thread() {
            @Override
            public void run() {
                prepareYUVDemoData(getApplicationContext(), 0);

                PolarrRenderJni.init(w, h, stride, scanline, true);
                yuvData = PolarrRenderJni.updateYUVData(yuvData);
                PolarrRenderJni.release();

                showYuv(yuvData);
            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void prepareYUVDemoData(Context context, int demoIndex) {
        String fileName = "";
        switch (demoIndex) {
            case 0: {
                fileName = "yuv.dat";
                w = 960;
                h = 720;
                stride = 960;
                scanline = 720;
            }
            break;
            case 1: {
                fileName = "2970group_4608x3456_idx72_master.yuv";
                w = 4608;
                h = 3456;
                stride = 4608;
                scanline = 3456;
            }
            break;
            case 2: {
                fileName = "2952group_1856x1408_idx48_slave.yuv";
                w = 1840;
                h = 1380;
                stride = 1856;
                scanline = 1408;
            }
            break;
            case 3: {
                fileName = "2952group_3264x2496_idx48_master.yuv";
                w = 3264;
                h = 2448;
                stride = 3264;
                scanline = 2496;
            }
            break;
            case 4: {
                fileName = "2970group_2624x1984_idx72_slave.yuv";
                w = 2592;
                h = 1940;
                stride = 2624;
                scanline = 1984;
            }
            break;
        }

        yuvData = new byte[stride * scanline * 3 / 2];
        try {
            InputStream is = context.getAssets().open(fileName);
            is.read(yuvData);
            is.close();
        } catch (IOException e) {
            System.out.println("IoException:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showYuv(byte[] yuvData) {
        Allocation bmData = null;
        bmData = renderScriptNV21ToRGBA888(
                this,
                w,
                h,
                yuvData);

        Bitmap stitchBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bmData.copyTo(stitchBmp);

        stitchBmp = getRotatedImage(stitchBmp, 90);
        final Bitmap finalStitchBmp = stitchBmp;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(finalStitchBmp);
            }
        });
    }

    public Allocation renderScriptNV21ToRGBA888(Context context, int width, int height, byte[] nv21) {
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(nv21);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        return out;
    }

    private static Bitmap getRotatedImage(Bitmap bitmap, int degrees) {
        if (bitmap != null && degrees != 0) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Matrix m = new Matrix();
            m.postRotate(degrees, w / 2, h / 2);
            Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
            if (bm != bitmap) {
                bitmap.recycle();
            }
            bitmap = bm;
        }

        return bitmap;
    }
}
