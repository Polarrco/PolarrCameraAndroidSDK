package co.polarr.polarrcamerademo;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import co.polarr.renderer.PolarrRender;
import co.polarr.renderer.entities.DrawingItem;
import co.polarr.renderer.filters.Basic;

/**
 * Created by Colin on 2017/11/18.
 * Camera Demo
 */

public class CameraRenderView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private static final int GRID_SIZE = 3;
    private static final int GRID_WIDTH = 320;
    private static final int GRID_HEIGHT = 480;
    private static final boolean DEBUG_DEMO_TEXTURE2D_INPUT = !true;
    private Context mContext;

    /**
     * Camera and SurfaceTexture
     */
    private Camera mCamera;
    private SurfaceTexture mSurfaceTexture;

    //private final FBORenderTarget mRenderTarget = new FBORenderTarget();
    private final OESTexture mCameraTexture = new OESTexture();
    //    private final Shader mOffscreenShader = new Shader();
    private PolarrRender polarrRender = new PolarrRender();
    private int mOutputTexture;
    private int mGridOutputTexture;
    private int mWidth, mHeight;
    private boolean updateTexture = false;
    private float[] mOrientationM = new float[16];

    private long[] frameList = new long[100];
    private long lastFrameTime;
    private List<DrawingItem> drawingItems;

    {
        Arrays.fill(frameList, -1);
    }

    private int frameIndex = 0;

    private long[] renderDuringList = new long[100];

    {
        Arrays.fill(renderDuringList, -1);
    }

    private int renderDuringIndex = 0;


    public CameraRenderView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CameraRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(CameraRenderView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
        updateTexture = true;
        requestRender();
    }

    @Override
    public synchronized void onSurfaceCreated(GL10 gl, EGLConfig config) {
        polarrRender.initRender(getResources(), 2, 2, true, DEBUG_DEMO_TEXTURE2D_INPUT ? PolarrRender.TEXTURE_2D : PolarrRender.EXTERNAL_OES);
    }

    @Override
    public synchronized void onSurfaceChanged(GL10 gl, int width, int height) {
        // force set the surface size
//        width = 1080;
//        height = 1440;
        mWidth = width;
        mHeight = height;

        mOutputTexture = genOutputTexture();
        mGridOutputTexture = genOutputTexture();
        polarrRender.setOutputTexture(mOutputTexture);

        long startTime = System.currentTimeMillis();
        polarrRender.updateSize(mWidth, mHeight);
        Log.d("updateSize", (System.currentTimeMillis() - startTime) + "ms");

        //generate camera texture------------------------
        mCameraTexture.init();
        if (DEBUG_DEMO_TEXTURE2D_INPUT) {
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            int inputTexture = textures[0];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTexture);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTexture);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
            bitmap.recycle();

            polarrRender.setInputTexture(inputTexture);
        } else {
            polarrRender.setInputTexture(mCameraTexture.getTextureId());
        }

        //set up surfacetexture------------------
        SurfaceTexture oldSurfaceTexture = mSurfaceTexture;
        mSurfaceTexture = new SurfaceTexture(mCameraTexture.getTextureId());
        mSurfaceTexture.setOnFrameAvailableListener(this);
        if (oldSurfaceTexture != null) {
            oldSurfaceTexture.release();
        }

        //set camera para-----------------------------------
        int camera_width = 0;
        int camera_height = 0;

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        mCamera = Camera.open();
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        Camera.Parameters param = mCamera.getParameters();
        List<Camera.Size> psize = param.getSupportedPreviewSizes();
        if (psize.size() > 0) {
            int i;
            for (i = 0; i < psize.size(); i++) {
                if (psize.get(i).width < width || psize.get(i).height < height)
                    break;
            }
            if (i > 0)
                i--;
            param.setPreviewSize(psize.get(i).width, psize.get(i).height);

            camera_width = psize.get(i).width;
            camera_height = psize.get(i).height;

        }

        //get the camera orientation and display dimension------------
        if (mContext.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            Matrix.setRotateM(mOrientationM, 0, 90.0f, 0f, 0f, 1f);
        } else {
            Matrix.setRotateM(mOrientationM, 0, 0.0f, 0f, 0f, 1f);
        }
        Matrix.scaleM(mOrientationM, 0, 1, -1, 1);
        param.setPictureFormat(PixelFormat.JPEG);
        param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        //start camera-----------------------------------------
        mCamera.setParameters(param);
        mCamera.startPreview();


        //start render---------------------
        requestRender();
    }

    @Override
    public synchronized void onDrawFrame(GL10 gl) {
        if (lastFrameTime != 0) {
            frameList[frameIndex++] = (System.currentTimeMillis() - lastFrameTime);
            frameIndex %= frameList.length;
        }
        lastFrameTime = System.currentTimeMillis();

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (mSurfaceTexture == null) {
            return;
        }
        //render the texture to FBO if new frame is available
        if (updateTexture) {
            mSurfaceTexture.updateTexImage();
            updateTexture = false;
        }

        if (drawingItems != null) {
            long startTime = System.currentTimeMillis();

            polarrRender.drawFiltersFrame(drawingItems, mGridOutputTexture);

            GLES20.glViewport(0, 0, mWidth, mHeight);

            Basic filter = Basic.getInstance(getResources());
            filter.setInputTextureId(mGridOutputTexture);

            if (DEBUG_DEMO_TEXTURE2D_INPUT) {
                Matrix.scaleM(filter.getMatrix(), 0, 1, -1, 1);
            } else {
                // update the matrix for camera orientation
                Matrix.setIdentityM(filter.getMatrix(), 0);
                Matrix.multiplyMM(filter.getMatrix(), 0, filter.getMatrix(), 0, mOrientationM, 0);
            }
            filter.draw();
            renderDuringList[renderDuringIndex++] = (System.currentTimeMillis() - startTime);
            renderDuringIndex %= renderDuringList.length;
        } else {
            long startTime = System.currentTimeMillis();
            polarrRender.updateInputTexture();
            polarrRender.drawFrame();
            GLES20.glViewport(0, 0, mWidth, mHeight);
            // demo draw screen
            Basic filter = Basic.getInstance(getResources());
            filter.setInputTextureId(mOutputTexture);
            filter.setNeedClear(false);

            if (DEBUG_DEMO_TEXTURE2D_INPUT) {
                Matrix.scaleM(filter.getMatrix(), 0, 1, -1, 1);
            } else {
                // update the matrix for camera orientation
                Matrix.setIdentityM(filter.getMatrix(), 0);
                Matrix.multiplyMM(filter.getMatrix(), 0, filter.getMatrix(), 0, mOrientationM, 0);
            }

            filter.draw();

            renderDuringList[renderDuringIndex++] = (System.currentTimeMillis() - startTime);
            renderDuringIndex %= renderDuringList.length;
        }

        if (frameIndex % 60 == 0) {
            printDuring();
        }
    }

    private void printDuring() {
        int total = 0;
        for (long during : frameList) {
            if (during == -1) {
                return;
            }
            total += during;
        }
        total /= frameList.length;
        Log.d("Frame_TIME", String.format(Locale.ENGLISH, "%d ms. %.2f fps", total, 1000f / total));

        total = 0;
        for (long during : renderDuringList) {
            if (during == -1) {
                return;
            }
            total += during;
        }
        total /= renderDuringList.length;
        Log.d("RENDER_TIME", total + "ms");
    }

    public void onDestroy() {
        updateTexture = false;
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
        }

        mCamera = null;
    }

    public void updateFilter(final String filterId) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                drawingItems = null;
                polarrRender.fastUpdateFilter(filterId);

                Arrays.fill(frameList, -1);
            }
        });
    }

    public void setTestFilters(final List<String> filterIds) {
        // sync render thread
        queueEvent(new Runnable() {
            @Override
            public void run() {
                drawingItems = new ArrayList<>();
                int index = 0;
                int padding = 20;
                int width = 300;
                int height = 500;
                int colums = 3;
                for (String filterId : filterIds) {
                    int raw = index / colums;
                    int col = index % colums;

                    DrawingItem drawingItem = new DrawingItem();
                    drawingItem.filterId = filterId;
                    drawingItem.rect = new Rect(col * (width + padding), raw * (height + padding), col * (width + padding) + width, raw * (height + padding) + height);

                    drawingItems.add(drawingItem);

                    index++;
                }

                Arrays.fill(frameList, -1);
            }
        });
    }

    public void takePhoto(final OnCaptureCallback onCaptureCallback) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(readTexture(mOutputTexture, mWidth, mHeight));

                onCaptureCallback.onPhoto(bitmap);
            }
        });
    }

    private void resize(int width, int height) {
        polarrRender.updateSize(width, height);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOutputTexture);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, width, height,
                0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private int genOutputTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        int texture = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, mWidth, mHeight,
                0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        return texture;
    }

    private static ByteBuffer readTexture(int texId, int width, int height) {
        int channels = 4;
        ByteBuffer ib = ByteBuffer.allocate(width * height * channels);
        int[] fFrame = new int[1];
        GLES20.glGenFramebuffers(1, fFrame, 0);
        bindFrameTexture(fFrame[0], texId);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
        unBindFrameBuffer();
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        return ib;
    }

    private static void bindFrameTexture(int frameBufferId, int textureId) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureId, 0);
    }

    private static void unBindFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public interface OnCaptureCallback {
        void onPhoto(Bitmap bitmap);
    }
}